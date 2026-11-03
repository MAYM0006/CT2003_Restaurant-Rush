#!/bin/bash
mkdir -p build
javac -d build $(find src -name "*.java")
java -cp build restaurantrush.ui.RestaurantRushCLI
