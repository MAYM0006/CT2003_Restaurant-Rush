# Stage 1 Test Notes

Both runs are deterministic: customers arrive on turns 1, 4, 7, 10, 13, 16 and nothing is random.

## How to run
    javac -d build <all .java files under src>
    Get-Content tests\victory-input.txt | java -cp build restaurantrush.ui.RestaurantRushCLI

## Victory run (tests/victory-input.txt)
Each customer is seated, ordered, cooked twice, served, then paid.
Expected: 5 paid, revenue $90.00, average satisfaction 69.6, 0 abandoned, VICTORY.

## Defeat run (tests/defeat-input.txt)
Waiter and Chef wait on every turn, so no one is seated.
Expected: 0 paid, revenue $0.00, average satisfaction 0.0, 2 abandoned, DEFEAT.