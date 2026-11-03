@echo off
if not exist build mkdir build
javac -d build -sourcepath src src/restaurantrush/ui/RestaurantRushCLI.java
java -cp build restaurantrush.ui.RestaurantRushCLI
pause