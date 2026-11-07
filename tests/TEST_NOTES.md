# Restaurant Rush — Reproducible test notes

## Stage 1 (corrected version)

The base setting uses 18 turns, two tables and six scheduled Regular arrivals.
The outcomes below were verified after correcting Stage 1 payment timing.

## Compile (Git Bash)

```bash
mkdir -p build
javac -d build $(find src -name '*.java')
```

## Victory run

```bash
java -cp build restaurantrush.ui.RestaurantRushCLI < tests/victory-input.txt
```

Expected: **4 paid**, **$72.00 revenue**, **68.0 average satisfaction**,
**0 abandoned**, **VICTORY**. This file contains a valid decision sequence for
all 18 turns; the earlier victory input was not compatible with the corrected
payment timing.

## Defeat run

```bash
java -cp build restaurantrush.ui.RestaurantRushCLI < tests/defeat-input.txt
```

Expected: **0 paid**, **$0.00 revenue**, **0.0 average satisfaction**,
**2 abandoned**, **DEFEAT**.

## Requirement-specific regression checks

```bash
javac -cp build -d build tests/Stage1RuleChecks.java
java -cp build Stage1RuleChecks
```

Checks:
1. Serving on turn 4 -> READY_TO_PAY on turn 5 -> payment on turn 6; the
   **customer and order** both become PAID and the table is freed.
2. Attempting to cook an already READY order or serve an already-served customer
   is rejected without changing turn, customer or served count.
3. Abandoning a table before cooking completes cancels its order without revenue.

All checks use deterministic actions and standard Java assertions via explicit
`AssertionError`s; no additional test framework is required.


## Stage 2 — Customer behaviour and integration

Run the following commands against the **Stage 2** source (`feature/stage-2-ey`
or the eventual `stage-2` tag), not the original Stage 1 tag. The Stage 1
victory script is not guaranteed to work with Stage 2 food preferences.

### Compile

```bash
mkdir -p build
javac -d build $(find src -name '*.java')
```

### Stage 2 rule checks

```bash
javac -cp build -d build tests/Stage2RuleChecks.java
java -cp build Stage2RuleChecks
```

Verified output: `Stage 2 rule checks: PASS (factory, polymorphism, discounts, preferences)`.
The checks cover VIP on turn 7, Critic on turn 13, customer-specific decay rates,
food preferences and VIP billing discount. VIP Burger billing is **$13.50**.

### Stage 2 victory run

```bash
java -cp build restaurantrush.ui.RestaurantRushCLI < tests/victory-stage2-input.txt
```

Verified after Turn 18: **4 paid**, **$67.50 revenue**, **70.3 average
satisfaction**, **0 abandoned**, **VICTORY**.

### Stage 2 defeat run

```bash
java -cp build restaurantrush.ui.RestaurantRushCLI < tests/defeat-input.txt
```

Verified after Turn 18: **0 paid**, **$0.00 revenue**, **2 abandoned**, **DEFEAT**.

The Stage 2 victory replay visibly includes a VIP arrival on turn 7 and a
Critic arrival on turn 13. When the VIP Burger is paid for, revenue increases
by **$13.50**, demonstrating that the discount is applied in gameplay.

## Stage 3 — Integrated game and final QA

Run these checks against the final Stage 3 code, **not** the historical
`stage-1` or `stage-2` tags:

```bash
javac -d build $(find src -name '*.java')
javac -cp build -d build tests/Stage1RuleChecks.java tests/Stage2RuleChecks.java tests/Stage3RuleChecks.java tests/Stage3OutputChecks.java
java -cp build Stage1RuleChecks
java -cp build Stage2RuleChecks
java -cp build Stage3RuleChecks
java -cp build Stage3OutputChecks
```

The Stage 3 output checks guard the Host → Waiter → Chef prompt order,
announcement of a customer's order, and the final display of **N/A** when no
customer has paid. The earlier Stage 1/2 tests preserve their historical rules.

### Full-game scripts

```bash
java -cp build restaurantrush.ui.RestaurantRushCLI < tests/victory-stage3-input.txt
java -cp build restaurantrush.ui.RestaurantRushCLI < tests/defeat-stage3-input.txt
java -cp build restaurantrush.ui.RestaurantRushCLI < tests/validation-stage3-input.txt
```

- Victory: 18 turns, **4 paid**, **$68.40** revenue, **80.3** average satisfaction.
- Defeat: 18 turns, **0 paid**, **$0.00** revenue, **N/A** average satisfaction, **2 abandoned**.
- Validation: 18 turns with invalid-input retries, ending in DEFEAT.

### Shell launcher behaviour

```bash
bash run.sh < tests/victory-stage3-input.txt
```

When input is redirected, `run.sh` must complete without an extra Enter and
return exit code **0** following a successful game run. When run interactively
from a terminal, it waits for Enter after presenting the final results.
The Windows `run.bat` launcher should stop rather than run stale class files
if compilation fails; this failure path requires verification on Windows.

## Additional Stage 3 scenarios

Replay on the final Stage 3 code:

    java -cp build restaurantrush.ui.RestaurantRushCLI < tests/victory-stage3-input_1.txt
    java -cp build restaurantrush.ui.RestaurantRushCLI < tests/defeat-slow-stage3-input.txt

Expected: the victory run ends with 5 paid, $80.10 revenue, 78.8 average satisfaction, 0 abandoned (VICTORY). The defeat run ends with 4 paid, $71.10 revenue, 57.8 average satisfaction, 0 abandoned (DEFEAT), because its customers wait longer and pay at satisfaction 52, 52, 75 and 52.
