# ============================================================================
# Kongu Engineering College (Autonomous) - Regulation 2024 (R2024)
# Student Academic & Continuous Evaluation System
# Build and Run Script (PowerShell)
# ============================================================================

$projectDir = Split-Path -Parent $MyInvocation.MyCommand.Path
Set-Location $projectDir

if (!(Test-Path "bin")) {
    New-Item -ItemType Directory -Path "bin" | Out-Null
}

Write-Host "[1/2] Compiling Java source files..." -ForegroundColor Cyan
$sources = (Get-ChildItem -Path "src" -Filter "*.java" -Recurse).FullName
& javac -d "bin" -cp "lib/*" $sources

if ($LASTEXITCODE -ne 0) {
    Write-Host "[ERROR] Java compilation failed!" -ForegroundColor Red
    exit $LASTEXITCODE
}

Write-Host "[2/2] Compilation successful!" -ForegroundColor Green
Write-Host ""
Write-Host "Select execution mode:" -ForegroundColor Yellow
Write-Host " [1] Interactive Menu Application (MainApp)"
Write-Host " [2] Automated Verification & Demonstration Suite (DemoRunner)"
$choice = Read-Host "Enter choice (1 or 2, default is 1)"

if ($choice -eq "2") {
    Write-Host "Starting Automated Verification Suite..." -ForegroundColor Cyan
    & java -cp "bin;lib/*" kec.app.DemoRunner
} else {
    Write-Host "Starting Interactive KEC Evaluation System..." -ForegroundColor Cyan
    & java -cp "bin;lib/*" kec.app.MainApp
}
