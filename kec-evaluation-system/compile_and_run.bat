@echo off
REM ============================================================================
REM Kongu Engineering College (Autonomous) - Regulation 2024 (R2024)
REM Student Academic & Continuous Evaluation System
REM Build and Run Script (Command Prompt)
REM ============================================================================

set "PROJECT_DIR=%~dp0"
cd /d "%PROJECT_DIR%"

if not exist "bin" mkdir "bin"

echo [1/2] Compiling Java source files...
javac -d bin -cp "lib/*" src\kec\model\*.java src\kec\db\*.java src\kec\engine\*.java src\kec\dao\*.java src\kec\app\*.java

if %ERRORLEVEL% NEQ 0 (
    echo [ERROR] Compilation failed!
    pause
    exit /b %ERRORLEVEL%
)

echo [2/2] Compilation successful!
echo.
echo Select program to run:
echo  1. Interactive Menu Application (MainApp)
echo  2. Automated Verification & Demonstration Suite (DemoRunner)
echo.
set /p choice="Enter choice (1 or 2): "

if "%choice%"=="2" (
    echo Starting Automated Verification Suite...
    java -cp "bin;lib/*" kec.app.DemoRunner
) else (
    echo Starting Interactive KEC Evaluation System...
    java -cp "bin;lib/*" kec.app.MainApp
)

pause
