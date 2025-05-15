#!/bin/bash

# Set the directory containing input files
INPUT_DIR="edgeCases"

for file in "$INPUT_DIR"/*; do
    echo "Running trafficSimulation.jar with input file: $file"
    java -jar trafficSimulation.jar "$file"
    echo "--------------------------------------------------"
done