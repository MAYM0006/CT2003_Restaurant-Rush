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

Each turn begins with an optional **Happy Hour** decision, followed by arrivals and the display of the waiting queue, tables and outstanding orders. Choose the **Host → Waiter → Chef** actions in that order when prompted:

| Role | Available action | Turn limit |
| --- | --- | --- |
| Waiter | Wait, seat one customer, take one order, or serve one ready order | One task |
| Chef | Wait or advance one order's preparation | One preparation unit |
| Host (Stage 3) | Wait or seat one additional waiting customer at a free table | One seating action |
| Cashier | Automatically pays the oldest eligible customer | At most one payment |

There are only **two tables**. The Waiter and Host can seat two different waiting customers in the same turn if separate tables are free. The Host may seat a customer before the Waiter takes that customer's order, and the Chef can start preparing it during the same turn. The CLI announces the selected dish, required preparation units and locked bill when an order is placed. Choices that violate a gameplay rule are rejected without advancing the turn; the player can re-enter the choices.

**Order and payment lifecycle:** an order is taken, prepared, becomes `READY`, is served, and enters `EATING`. If it is served on turn T, the customer becomes `READY_TO_PAY` on T+1 and can be paid **no earlier than T+2**. Payment marks the order and customer paid, adds the *locked* bill amount to revenue and frees the table. Customers who run out of satisfaction while waiting or seated enter `ABANDONED` status, leave without paying, and any unfinished order is cancelled.

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
javac -cp build -d build tests/Stage1RuleChecks.java tests/Stage2RuleChecks.java tests/Stage3RuleChecks.java tests/Stage3OutputChecks.java
java -cp build Stage1RuleChecks
java -cp build Stage2RuleChecks
java -cp build Stage3RuleChecks
java -cp build Stage3OutputChecks
```

Verified results on the integrated Stage 3 code:

```text
Stage 1 rule checks: PASS (3 scenarios)
Stage 2 rule checks: PASS (factory, polymorphism, discounts, preferences)
Stage 3 rule checks: PASS (combo, host, Happy Hour, pricing, lifecycle)
Stage 3 output checks: PASS (end results, order announcement, prompt order)
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

**Additional Stage 3 scenarios:** two more scripted 18-turn runs on the final code.

| Scenario | Input file | Verified outcome |
| --- | --- | --- |
| Victory (efficient service) | `tests/victory-stage3-input_1.txt` | **VICTORY**, 5 paid, **$80.10**, **78.8** average satisfaction, 0 abandoned |
| Near-miss defeat (slow service) | `tests/defeat-slow-stage3-input.txt` | **DEFEAT**, 4 paid, **$71.10**, **57.8** average satisfaction, 0 abandoned: the average ends just under the 60.0 goal |

The Stage 3 rule checks additionally exercise ComboMeal preparation and payment, Host seating, invalid-action atomicity, Happy Hour duration and one-time activation, satisfaction recovery, discount ordering and non-retroactive locked bills. No external testing framework is required. See `tests/TEST_NOTES.md` for the earlier Stage 1 and Stage 2 snapshots and their regression notes.

**Launcher verification:** `bash run.sh` pauses for Enter only in an interactive terminal. For scripted replays, `bash run.sh < tests/victory-stage3-input.txt` finishes without waiting for extra input and returns the game process exit status. `run.bat` checks compilation before launching the game. When no customer has paid, both turn summaries and final results show average satisfaction as **N/A**.

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

## Development history and stage transition evidence

Repository: https://github.com/MAYM0006/CT2003_Restaurant-Rush

The project was built in successive runnable versions, merged through GitHub pull requests. The following tables identify **each changed or new file** across the relevant snapshots, including the reason for the change. No files were deleted in either transition.

### Stage 1 baseline and lifecycle corrections

- Original Stage 1: commit [`0944e68`](https://github.com/MAYM0006/CT2003_Restaurant-Rush/commit/0944e68).
- Corrected Stage 1: commit [`f6a962f`](https://github.com/MAYM0006/CT2003_Restaurant-Rush/commit/f6a962f), after [PR #12](https://github.com/MAYM0006/CT2003_Restaurant-Rush/pull/12).
- [Compare original versus corrected Stage 1](https://github.com/MAYM0006/CT2003_Restaurant-Rush/compare/0944e68...f6a962f).

The correction preserves a separately runnable Stage 1 before introducing VIP or Critic customers. It repairs READY_TO_PAY timing and automatic payment, prevents repeated serving, tracks the order's paid state, improves invalid-action handling, and updates Stage 1 test evidence. The remote `stage-1` tag was verified on 7 November 2026 as pointing to corrected Stage 1 commit `f6a962f` (the historical local pre-fix tag is no longer the remote release).

### Transition 1 → 2: specialised customer behaviour

- **From:** corrected Stage 1, commit `f6a962f`.
- **To:** tagged Stage 2, commit [`93df7b9`](https://github.com/MAYM0006/CT2003_Restaurant-Rush/commit/93df7b9).
- **Git comparison:** [Stage 1 → Stage 2 (file changes)](https://github.com/MAYM0006/CT2003_Restaurant-Rush/compare/f6a962f...93df7b9).
- **Key pull requests:** [#13 customer subclasses and tests](https://github.com/MAYM0006/CT2003_Restaurant-Rush/pull/13), [#14 further billing work](https://github.com/MAYM0006/CT2003_Restaurant-Rush/pull/14).

| Change | File | Reason |
| --- | --- | --- |
| New | `src/restaurantrush/customer/VIPCustomer.java` | Define a VIP's slower satisfaction decay, 10% discount and Burger preference through overriding. |
| New | `src/restaurantrush/customer/CriticCustomer.java` | Define faster satisfaction decay and distinct Salad preference through overriding. |
| Modified | `src/restaurantrush/customer/CustomerFactory.java` | Select VIP on Turn 7 and Critic on Turn 13 while preserving six arrivals. |
| Modified | `src/restaurantrush/domain/BillCalculator.java` | Apply each customer's discount when calculating the payable amount. |
| Modified | `src/restaurantrush/ui/RestaurantRushCLI.java` | Identify the Stage 2 version in the welcome banner. |
| New | `tests/Stage2RuleChecks.java` | Reproduce customer-factory, polymorphism and discount checks. |
| New | `tests/victory-stage2-input.txt` | Provide a reproducible 18-turn Stage 2 victory sequence. |
| Modified | `tests/TEST_NOTES.md` | Document Stage 2 testing evidence and revised procedures. |
| Modified | `README.md` | Explain Stage 2 behaviour, execution and testing. |

**Deleted files:** None. The Stage 1 payment and lifecycle fixes remain intact in Stage 2.

### Transition 2 → 3: Host, ComboMeal and Happy Hour

- **From:** tagged Stage 2, commit `93df7b9`.
- **To:** tagged Stage 3, commit [`0961702`](https://github.com/MAYM0006/CT2003_Restaurant-Rush/commit/0961702) (includes the final QA corrections).
- **Git comparison:** [Stage 2 → Stage 3 (file changes)](https://github.com/MAYM0006/CT2003_Restaurant-Rush/compare/93df7b9...0961702).
- **Key pull requests:** [#15 controller/domain](https://github.com/MAYM0006/CT2003_Restaurant-Rush/pull/15), [#16 Host](https://github.com/MAYM0006/CT2003_Restaurant-Rush/pull/16), [#17 initial CLI](https://github.com/MAYM0006/CT2003_Restaurant-Rush/pull/17), [#18 customer recovery](https://github.com/MAYM0006/CT2003_Restaurant-Rush/pull/18), [#19 Stage 3 integration](https://github.com/MAYM0006/CT2003_Restaurant-Rush/pull/19), [#20 README](https://github.com/MAYM0006/CT2003_Restaurant-Rush/pull/20), [#21 QA close-out](https://github.com/MAYM0006/CT2003_Restaurant-Rush/pull/21), [#22 launcher exit prompt](https://github.com/MAYM0006/CT2003_Restaurant-Rush/pull/22), [#23 final QA corrections](https://github.com/MAYM0006/CT2003_Restaurant-Rush/pull/23).

| Change | File | Reason |
| --- | --- | --- |
| New | `src/restaurantrush/staff/Host.java` | Add an independent one-seat-per-turn Host role. |
| New | `src/restaurantrush/domain/ComboMeal.java` | Model two-component combos with discounted price and combined preparation units. |
| New | `src/restaurantrush/domain/MenuSelection.java` | Give MenuItem and ComboMeal a common orderable-item API. |
| Modified | `src/restaurantrush/ui/ActionEnums.java` | Introduce Host seating choices. |
| Modified | `src/restaurantrush/ui/RestaurantRushCLI.java` | Add Host and Happy Hour prompts, display the ordered dish immediately in the same turn, and use Host → Waiter → Chef prompt order. |
| Modified | `src/restaurantrush/controller/GameController.java` | Integrate Host actions, Happy Hour's turn-start recovery and once-only activation, order-price locking, and order-placement display data; preserve payment timing and record abandonment. |
| Modified | `src/restaurantrush/customer/Customer.java` | Restore +15 satisfaction for eligible waiting/seated customers, capped at 100. |
| Modified | `src/restaurantrush/customer/CustomerFactory.java` | Register a deterministic ComboMeal customer on Turn 4. |
| Modified | `src/restaurantrush/customer/RegularCustomer.java` | Use polymorphic food selection to return either Pasta or the chosen combo. |
| Modified | `src/restaurantrush/customer/CustomerState.java` | Record an explicit `ABANDONED` lifecycle state. |
| Modified | `src/restaurantrush/domain/BillCalculator.java` | Combine VIP and Happy Hour discounts, maintaining Stage 2 API compatibility. |
| Modified | `src/restaurantrush/domain/MenuItem.java` | Implement the common `MenuSelection` interface. |
| Modified | `src/restaurantrush/domain/Order.java` | Accept a menu item or combo and lock the discounted bill at order placement. |
| New | `tests/Stage3RuleChecks.java` | Test ComboMeal, Host, Happy Hour, payment lifecycle, same-turn coordination and abandonment. |
| New | `tests/Stage3OutputChecks.java` | Guard correct end-game N/A display, immediate order announcement, and Host → Waiter → Chef prompt sequence against regressions. |
| Modified | `tests/TEST_NOTES.md` | Add final Stage 3 scenario results, output checks, and launcher regression instructions. |
| New | `tests/victory-stage3-input.txt` | Reproduce a Stage 3 victory, including ordered Stage 3 input prompts. |
| New | `tests/defeat-stage3-input.txt` | Reproduce a full 18-turn defeat. |
| New | `tests/validation-stage3-input.txt` | Test invalid input and full-turn continuation without an incorrect action consuming capacity. |
| Modified | `src/restaurantrush/customer/VIPCustomer.java` | Add JavaDoc describing the VIP-specific customer behaviour for the final design. |
| Modified | `src/restaurantrush/customer/CriticCustomer.java` | Add JavaDoc documenting the Critic-specific service behaviour. |
| Modified | `src/restaurantrush/domain/Table.java` | Document table ownership and the freeing of a table after payment or abandonment. |
| Modified | `src/restaurantrush/exception/InvalidGameActionException.java` | Document the exception used when rejecting invalid manager choices. |
| Modified | `src/restaurantrush/staff/Staff.java` | Document shared one-action-per-turn capacity and resetting it. |
| Modified | `src/restaurantrush/staff/Waiter.java` | Document the Waiter's one-task-per-turn responsibility. |
| Modified | `src/restaurantrush/staff/Chef.java` | Document the Chef's one-unit-per-turn responsibility. |
| Modified | `src/restaurantrush/staff/Cashier.java` | Document automatic payment responsibilities. |
| Modified | `run.sh` | Use the project directory, stop on compilation failure, keep the final result visible when launched interactively, and return the game exit status correctly in scripted runs. |
| Modified | `run.bat` | On Windows, launch from the script directory, stop if compilation fails, and retain the game exit status while allowing the player to read the final result. |
| Modified | `README.md` | Document the final features, reproducible tests and stage-by-stage design changes. |

**Deleted files:** None. The original Stage 1 and 2 versions remain in Git history as separately runnable snapshots.

### Tags and reproducibility

Each stage is a separate, independently runnable snapshot identified by a Git tag. The tags were verified on **7 November 2026**; each was extracted, compiled and run on its own.

| Stage | Tag | Commit | Victory input | Defeat input |
| --- | --- | --- | --- | --- |
| 1 | `stage-1` | [`f6a962f`](https://github.com/MAYM0006/CT2003_Restaurant-Rush/commit/f6a962f) | `tests/victory-input.txt` | `tests/defeat-input.txt` |
| 2 | `stage-2` | [`93df7b9`](https://github.com/MAYM0006/CT2003_Restaurant-Rush/commit/93df7b9) | `tests/victory-stage2-input.txt` | `tests/defeat-input.txt` |
| 3 | `stage-3` | [`0961702`](https://github.com/MAYM0006/CT2003_Restaurant-Rush/commit/0961702) | `tests/victory-stage3-input.txt` | `tests/defeat-stage3-input.txt` |

Verified results of the scripted runs at each tag:

| Stage | Scenario | Paid | Revenue | Average paid satisfaction | Abandoned | Result |
| --- | --- | ---: | ---: | ---: | ---: | --- |
| 1 | Victory | 4 | $72.00 | 68.0 | 0 | VICTORY |
| 1 | Defeat | 0 | $0.00 | 0.0 | 2 | DEFEAT |
| 2 | Victory | 4 | $67.50 | 70.3 | 0 | VICTORY |
| 2 | Defeat | 0 | $0.00 | 0.0 | 2 | DEFEAT |
| 3 | Victory | 4 | $68.40 | 80.3 | 0 | VICTORY |
| 3 | Defeat | 0 | $0.00 | N/A | 2 | DEFEAT |

Stage 1 and Stage 2 display an average of 0.0 when nobody has paid; the N/A display was added in Stage 3.

To reproduce a stage, check out its tag, compile and replay its inputs (each stage uses its own input files because the prompts differ):

```bash
git checkout stage-1          # or stage-2, stage-3
mkdir -p build
javac -d build $(find src -name '*.java')
java -cp build restaurantrush.ui.RestaurantRushCLI < tests/victory-input.txt
git checkout main             # return to the latest version
```
