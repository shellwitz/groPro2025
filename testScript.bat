@echo off

REM Set the directory containing input files
set INPUT_DIR=normalCases

REM Loop through all files in the input directory
for %%f in (%INPUT_DIR%\*) do (
    echo Running MyProgram.jar with input file: %%f
    java -jar C:\Users\debel\Workspace\groPro2025\groPro2025\out\artifacts\groPro2025_jar\groPro2025.jar "%%f"
    echo --------------------------------------------------
)

pause