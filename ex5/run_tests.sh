#!/bin/bash

# Configuration
COMPILER="COMPILER"
INPUT_DIR="input"
EXPECTED_DIR="tests/expected"
OUTPUT_DIR="tests/output"
MIPS_OUT="$OUTPUT_DIR/mips.s"
TEMP_OUT="$OUTPUT_DIR/actual.txt"

# Colors for output
GREEN='\033[0;32m'
RED='\033[0;31m'
NC='\033[0m'

# Ensure directories exist
mkdir -p "$OUTPUT_DIR"

# Build the compiler first
echo "Building compiler..."
make all > /dev/null 2>&1

if [ $? -ne 0 ]; then
    echo -e "${RED}Failed to build compiler!${NC}"
    exit 1
fi

# List of tests to run
TESTS=("TEST_27" "TEST_28" "TEST_29" "TEST_30")

echo "Running field access tests..."
echo "--------------------------------"

for T in "${TESTS[@]}"; do
    echo -n "Running $T... "
    
    # 1. Run compiler
    # Find the full filename in input directory
    IN_FILE=$(ls "$INPUT_DIR"/${T}_*.txt 2>/dev/null | head -n 1)
    if [ -z "$IN_FILE" ]; then
        echo -e "${RED}Input file not found!${NC}"
        continue
    fi
    
    java -jar "$COMPILER" "$IN_FILE" "$MIPS_OUT" > /dev/null 2>&1
    
    if [ $? -ne 0 ]; then
        echo -e "${RED}Compilation failed!${NC}"
        continue
    fi
    
    # 2. Run SPIM
    # We filter out the 'Loaded:' line which varies by environment
    spim -f "$MIPS_OUT" 2>&1 | grep -v "Loaded:" > "$TEMP_OUT"
    
    # 3. Compare with expected
    EXPECTED_FILE="$EXPECTED_DIR/$T.txt"
    
    # Check if expected file exists
    if [ ! -f "$EXPECTED_FILE" ]; then
        echo -e "${RED}Expected output file missing!${NC}"
        continue
    fi
    
    # Diff ignoring whitespace changes
    diff -b "$TEMP_OUT" "$EXPECTED_FILE" > /dev/null
    
    if [ $? -eq 0 ]; then
        echo -e "${GREEN}PASSED${NC}"
    else
        echo -e "${RED}FAILED${NC}"
        echo "Differences:"
        diff -u "$EXPECTED_FILE" "$TEMP_OUT"
    fi
done

echo "--------------------------------"
echo "Tests complete."
