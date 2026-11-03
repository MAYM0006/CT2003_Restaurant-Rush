# Stage 1 — Reproducible test notes

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
