#!/bin/bash

cd "$(dirname "$0")" || exit 1

mkdir -p build
javac -d build $(find src -name "*.java") || exit 1

java -cp build restaurantrush.ui.RestaurantRushCLI

echo
read -r -p "Press ENTER to close the game..."
