@echo off
setlocal
cd /d "%~dp0" || exit /b 1
if not exist build mkdir build
javac -d build -sourcepath src src/restaurantrush/ui/RestaurantRushCLI.java
if errorlevel 1 (
    echo [ERROR] Compilation failed; game not started.
    pause
    exit /b 1
)
java -cp build restaurantrush.ui.RestaurantRushCLI
set "game_exit=%errorlevel%"
pause
exit /b %game_exit%
