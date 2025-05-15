@echo off

REM Set the directory containing input files
set INPUT_DIR="normalCases"

for %%f in (%INPUT_DIR%\*) do (
    echo Running trafficSimulation.jar with input file: %%f
    java -jar trafficSimulation.jar "%%f"
    echo --------------------------------------------------
)

pause