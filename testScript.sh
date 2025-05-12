#!/bin/bash

# Set the directory containing input files
INPUT_DIR="input_files"

# Loop through all files in the input directory
for file in "$INPUT_DIR"/*; do
    echo "Running MyProgram.jar with input file: $file"
    java -jar MyProgram.jar "$file"
    echo "--------------------------------------------------"
done