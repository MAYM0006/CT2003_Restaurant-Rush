#!/bin/bash

cd "$(dirname "$0")" || exit 1

mkdir -p build
javac -d build $(find src -name "*.java") || exit 1

java -cp build restaurantrush.ui.RestaurantRushCLI
game_status=$?

# Keep the result visible when launched interactively, but do not require
# additional input from scripted or CI runs (where stdin reaches EOF).
if [[ -t 0 ]]; then
    echo
    read -r -p "Press ENTER to close the game..." || true
fi

exit "$game_status"
