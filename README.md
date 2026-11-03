# CT2003 Restaurant Rush — Stages 1 and 2

A deterministic, offline Java command-line restaurant game developed in three
stages. Stage 1 establishes basic restaurant operations. Stage 2 adds VIP and
Critic customer behaviours. Stage 3 (Host, ComboMeal and Happy Hour) is not yet
integrated into this branch. See **Stage history** for tag status.

## Run

On Git Bash/Linux/macOS: `bash run.sh`
On Windows: `run.bat`

Requires a Java JDK. No external libraries or internet access are needed.

## Stage 1 gameplay

- 18 turns, two tables, one Waiter task/turn, one Chef preparation unit/turn,
  and one automatic Cashier payment/turn.
- Regular customers arrive at the start of turns 1, 4, 7, 10, 13 and 16.
- The Waiter seats, takes orders or serves a previously READY order. The Chef may
  cook an order taken that turn. Regular customers deterministically order Pasta
  ($18, two preparation units).
- Satisfaction decreases by 8 per turn while waiting/seated, never below zero.
  An abandoned customer leaves without paying, and unfinished orders are cancelled.
- Serving on turn T leads to EATING on T, READY_TO_PAY on T+1 and payment on T+2
  at the earliest. The Cashier pays at most one customer per turn, oldest ready
  to pay first. Payment marks both the customer and order PAID and frees the table.
- Invalid actions are rejected without using a staff action or advancing the turn.
- Victory after all 18 turns requires **4 paid customers**, **$45 revenue** and
  **average paid-customer satisfaction of 60**.

## Stage 2 — Customer behaviour extension

Stage 2 keeps the same 18-turn game, two tables, payment lifecycle and
victory/defeat conditions, but changes two of the six arrivals:

| Arrival turn | Customer | Satisfaction loss while waiting/seated | Discount | Food choice |
| --- | --- | ---: | ---: | --- |
| 1, 4, 10, 16 | Regular | 8 | 0% | Pasta |
| 7 | VIP | 5 | 10% | Burger |
| 13 | Critic | 12 | 0% | Salad |

`CustomerFactory` creates the relevant subclass. The existing abstract `Customer`
class declares `getDecayRate()`, `getDiscountRate()` and `chooseFood()`; each
subclass supplies its behaviour through overriding. `BillCalculator` uses the
runtime customer object's discount rate when calculating a bill. For example,
a VIP Burger costs $15.00 × (1 − 0.10) = **$13.50**.

### Stage 1 → Stage 2 file changes

- **Added:** `VIPCustomer.java` (VIP behaviour) and `CriticCustomer.java`
  (Critic behaviour).
- **Changed:** `CustomerFactory.java` (arrival-specific subtype creation),
  `BillCalculator.java` (customer-specific discounts), and
  `RestaurantRushCLI.java` (Stage 2 welcome heading).
- **Added test evidence:** `tests/Stage2RuleChecks.java` and
  `tests/victory-stage2-input.txt`.
- **Unchanged:** `Customer.java`, `RegularCustomer.java`, `CustomerState.java`
  and the corrected Stage 1 game lifecycle; no existing files deleted.

Development commits on `feature/stage-2-ey`:
`1aa8d2d` (VIP), `41df6a8` (Critic), `8eb3267` (factory),
`3a0837e` (discount integration and tests). Add the final Stage 1 → Stage 2
GitHub compare link once `stage-2` is tagged.

## Reproducible test evidence

See `tests/TEST_NOTES.md` for stage-specific victory/defeat input scripts and
focused rule checks. The Stage 1 victory script is not a valid Stage 2 replay:
Stage 2 changes customers' food choices and preparation schedules.

## Stage history

The initial annotated `stage-1` tag points to commit `0944e68`, the
**pre-fix** Stage 1 implementation. Stage 1 corrections were merged in PR #12,
but the shared tag has not yet been reconciled. Do not silently force-move it
while teammates may be using it.

Stage 2 is implemented and tested on branch `feature/stage-2-ey`; the `stage-2`
tag should be created on its final runnable merged commit. Stage 3 and its
`stage-3` tag are still pending. The final README should include compare links
between the verified tags after all three are published.
