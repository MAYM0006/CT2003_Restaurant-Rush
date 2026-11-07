# CT2003 Restaurant Rush — Stages 1, 2 and 3

Restaurant Rush is an offline, deterministic Java command-line restaurant-management game built incrementally for CT2003 Object-Oriented Design and Programming. The repository contains the completed Stage 3 implementation, which extends the earlier stages with customer specialisations, a Host, ComboMeals and Happy Hour.

The game always runs for **18 turns**. Your goal is to serve and collect payment from at least **four customers**, earn **at least $45.00**, and achieve an **average satisfaction of at least 60.0** among paid customers. All three conditions are checked **after Turn 18**.

## Requirements and running the game

- A Java JDK with `java` and `javac` available on your PATH. The integrated code has been compiled and tested with Java 21.
- No third-party libraries, build systems or internet connection required.
- Run the commands from the project root (the directory containing `src/`, `tests/`, `run.sh` and `run.bat`).

**Option A — supplied launch scripts**

- Git Bash, macOS or Linux: `bash run.sh`
- Windows Command Prompt: `run.bat`

**Option B — compile and run manually (Git Bash)**

```bash
mkdir -p build
javac -d build $(find src -name '*.java')
java -cp build restaurantrush.ui.RestaurantRushCLI
```

If you downloaded the repository as a ZIP from GitHub, extract it first, then open a terminal in the extracted project folder. The entry point is `restaurantrush.ui.RestaurantRushCLI`.

## How to play Stage 3

Each turn begins with an optional **Happy Hour** decision, followed by arrivals and the display of the waiting queue, tables and outstanding orders. Choose the Waiter's, Chef's and Host's actions when prompted:

| Role | Available action | Turn limit |
| --- | --- | --- |
| Waiter | Wait, seat one customer, take one order, or serve one ready order | One task |
| Chef | Wait or advance one order's preparation | One preparation unit |
| Host (Stage 3) | Wait or seat one additional waiting customer at a free table | One seating action |
| Cashier | Automatically pays the oldest eligible customer | At most one payment |

There are only **two tables**. The Waiter and Host can seat two different waiting customers in the same turn if separate tables are free. The Chef can work on an order taken in that turn. Choices that violate a gameplay rule are rejected without advancing the turn; the player can re-enter the choices.

**Order and payment lifecycle:** an order is taken, prepared, becomes `READY`, is served, and enters `EATING`. If it is served on turn T, the customer becomes `READY_TO_PAY` on T+1 and can be paid **no earlier than T+2**. Payment marks the order and customer paid, adds the *locked* bill amount to revenue and frees the table. Customers who run out of satisfaction while waiting or seated leave without paying, and any unfinished order is cancelled.

## Stage-by-stage development

### Stage 1 — Basic restaurant operations

- 18 turns, two tables, six scheduled customer arrivals (turns **1, 4, 7, 10, 13 and 16**).
- All customers are `RegularCustomer`, losing **8 satisfaction points per waiting/seated turn** and ordering **Pasta** ($18.00; two preparation units).
- Waiter, Chef and automatic Cashier operations; order lifecycle, payment timing, FIFO payment priority, abandonment and invalid-action handling.
- Victory requirements remain the same throughout the three stages.

### Stage 2 — VIP and Critic behaviours

`CustomerFactory` creates different `Customer` subclasses at the scheduled arrivals. Each subclass overrides `getDecayRate()`, `getDiscountRate()` and `chooseFood()`, so the game controller calls common methods without checking the customer's concrete type.

| Arrival turn | Customer type | Decay per turn | Customer discount | Stage 2 order |
| --- | --- | ---: | ---: | --- |
| 1, 4, 10, 16 | Regular | 8 | 0% | Pasta ($18.00) |
| 7 | VIP | 5 | 10% | Burger ($15.00 → $13.50) |
| 13 | Critic | 12 | 0% | Salad ($9.00) |

Stage 2 preserves the corrected Stage 1 payment and order lifecycle.

### Stage 3 — Host, ComboMeal and Happy Hour

- **Host:** adds an independent seating action, limited by the same two physical tables.
- **ComboMeal:** combines two `MenuItem` values. The combined price is discounted by **10%** and rounded to cents; preparation units are added together. The **Regular customer arriving on Turn 4** deterministically orders a **Salad + Burger Combo**: ($9.00 + $15.00) × 90% = **$21.60**, requiring **two Chef preparation units**. Other Regular customers continue choosing Pasta. VIP and Critic preferences remain unchanged.
- **Happy Hour:** can be activated **once per game**, at the beginning of a turn, and lasts for **that turn and the following turn**. At the start of each active turn, waiting or seated customers recover **15 satisfaction points**, capped at 100. Orders *taken* during Happy Hour receive a further **20% price discount**; previous orders do not receive it retroactively.
- **Locked billing:** the order's final bill is calculated **when the Waiter takes the order**, applying the ComboMeal's built-in discount (if any), then the customer-specific discount (such as VIP), then the Happy Hour discount (if applicable), and rounding to cents. Paying after Happy Hour ends does not change an already-locked price.
- **Selection interface:** `MenuSelection` lets `MenuItem` and `ComboMeal` expose a common name, price and preparation-unit API, so `Order` can hold either type.

## Tests and expected results

First compile the source and all three standalone rule-check suites:

```bash
mkdir -p build
javac -d build $(find src -name '*.java')
javac -cp build -d build tests/Stage1RuleChecks.java tests/Stage2RuleChecks.java tests/Stage3RuleChecks.java
java -cp build Stage1RuleChecks
java -cp build Stage2RuleChecks
java -cp build Stage3RuleChecks
```

Verified results on the integrated Stage 3 code:

```text
Stage 1 rule checks: PASS (3 scenarios)
Stage 2 rule checks: PASS (factory, polymorphism, discounts, preferences)
Stage 3 rule checks: PASS (combo, host, Happy Hour, pricing, lifecycle)
```

Run the prepared **Stage 3** 18-turn CLI scenarios from the project root:

```bash
java -cp build restaurantrush.ui.RestaurantRushCLI < tests/victory-stage3-input.txt
java -cp build restaurantrush.ui.RestaurantRushCLI < tests/defeat-stage3-input.txt
java -cp build restaurantrush.ui.RestaurantRushCLI < tests/validation-stage3-input.txt
```

| Scenario | Verified outcome |
| --- | --- |
| Victory | **VICTORY**, 4 paid customers, **$68.40** revenue, **80.3** average paid-customer satisfaction, 0 abandoned; all 18 turns completed |
| Defeat | **DEFEAT**, 0 paid, $0.00 revenue, 2 abandoned; all 18 turns completed |
| Input validation | Completed all 18 turns with invalid-input handling exercised; ends in DEFEAT because the scripted actions do not target victory |

The Stage 3 rule checks additionally exercise ComboMeal preparation and payment, Host seating, invalid-action atomicity, Happy Hour duration and one-time activation, satisfaction recovery, discount ordering and non-retroactive locked bills. No external testing framework is required. See `tests/TEST_NOTES.md` for the earlier Stage 1 and Stage 2 snapshots and their regression notes.

**Important:** Stage 1 and Stage 2 victory input scripts are designed for their respective stage versions. Use the `*-stage3-input.txt` scripts when replaying gameplay on the current Stage 3 CLI, because its prompts include the Host and Happy Hour decisions.

## Repository structure

```text
src/restaurantrush/
  controller/    GameController and turn processing
  customer/      Customer hierarchy and CustomerFactory
  domain/        MenuItem, ComboMeal, MenuSelection, Order, Table and billing
  exception/     InvalidGameActionException
  staff/         Staff hierarchy, including Host
  ui/            RestaurantRushCLI and ActionEnums
tests/            Standalone stage checks, scripted inputs and test notes
run.sh           Git Bash/macOS/Linux launch script
run.bat          Windows launch script
```

## Development history and stage tags

Development was integrated through GitHub pull requests; the key work includes Stage 1 lifecycle corrections (**PR #12**), Stage 2 customer features (**PR #13**) and additional Stage 2 billing changes (**PR #14**), followed by Stage 3 controller/domain (**PR #15**), Host (**PR #16**), initial CLI (**PR #17**), customer recovery (**PR #18**) and the final Stage 3 integration fixes.

Repository: https://github.com/MAYM0006/CT2003_Restaurant-Rush

- `stage-2` identifies the merged, runnable Stage 2 snapshot (commit `93df7b9`).
- `stage-1` exists, but its final target must be verified with the team: an earlier local copy pointed to the original Stage 1 commit (`0944e68`), before the fixes in PR #12. Do not force-move a shared tag without coordination.
- Create or verify `stage-3` **after** the completed integration changes are confirmed on `main` and final acceptance checks are complete.

Once all three tags point to the intended **runnable** snapshots, verify the Stage 1 → Stage 2 and Stage 2 → Stage 3 GitHub comparisons for the submission and oral presentation.
