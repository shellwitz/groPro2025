@echo off

REM Set the directory containing input files
set INPUT_DIR=input_files

REM Loop through all files in the input directory
for %%f in (%INPUT_DIR%\*) do (
    echo Running MyProgram.jar with input file: %%f
    java -jar MyProgram.jar "%%f"
    echo --------------------------------------------------
)

pause