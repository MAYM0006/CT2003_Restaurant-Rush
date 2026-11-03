# CT2003 Restaurant Rush — Stage 1

A deterministic, offline Java command-line restaurant game. This repository's current
`main` baseline implements **Stage 1** only. Stages 2 (VIP/Critic) and 3
(Host/ComboMeal/Happy Hour) are planned as separate, tagged, runnable additions.

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

## Reproducible test evidence

See `tests/TEST_NOTES.md` for victory/defeat input scripts and focused rule checks.

## Stage history

The initial `stage-1` tag currently points to the pre-fix Stage 1 commit.
After merging these fixes, coordinate an updated verified Stage 1 tag.
Stages 2 and 3 will be tagged on their respective runnable commits.
