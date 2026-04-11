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
make > /dev/null 2>&1

if [ $? -ne 0 ]; then
    echo -e "${RED}Failed to build compiler!${NC}"
    exit 1
fi

echo "Running tests..."
echo "--------------------------------"

# Run ALL .txt files in INPUT_DIR
for IN_FILE in "$INPUT_DIR"/*.txt; do
    FILENAME=$(basename "$IN_FILE")
    # Determine the test ID (e.g., TEST_01 from TEST_01_Print_Primes.txt)
    # Match the pattern TEST_XX
    TEST_ID=$(echo "$FILENAME" | grep -oE "TEST_[0-9]+")
    
    if [ -z "$TEST_ID" ]; then
        TEST_ID="${FILENAME%.txt}"
    fi

    echo "Running $FILENAME..."

    # 1. Run compiler
    if [[ "$FILENAME" == *"TEST_31"* ]]; then
        # Special case for TEST_31 (VTable check)
        echo "  [Compiler] java -jar $COMPILER $IN_FILE $MIPS_OUT"
        java -jar "$COMPILER" "$IN_FILE" "$MIPS_OUT" > "$TEMP_OUT" 2>&1
        grep "VTABLE LAYOUT" "$TEMP_OUT" > "$OUTPUT_DIR/vtable_out.txt" 2>/dev/null
        ACTUAL_COMPARE="$OUTPUT_DIR/vtable_out.txt"
        EXPECTED_FILE="$EXPECTED_DIR/TEST_31.txt"
    else
        echo "  [Compiler] java -jar $COMPILER $IN_FILE $MIPS_OUT"
        java -jar "$COMPILER" "$IN_FILE" "$MIPS_OUT" > /dev/null 2>&1
        if [ $? -ne 0 ]; then
            echo -e "  ${RED}Compilation failed!${NC}"
            continue
        fi
        
        # 2. Run SPIM
        # We filter out the 'Loaded:' line which varies by environment
        echo "  [SPIM] spim -f $MIPS_OUT"
        spim -f "$MIPS_OUT" 2>&1 | grep -v "Loaded:" | tee "$TEMP_OUT" | sed 's/^/    /'
        ACTUAL_COMPARE="$TEMP_OUT"
        
        # Look for expected file: exact name or ID based name
        EXPECTED_FILE="$EXPECTED_DIR/$FILENAME"
        if [ ! -f "$EXPECTED_FILE" ]; then
            EXPECTED_FILE="$EXPECTED_DIR/$TEST_ID.txt"
        fi
    fi

    # 3. Compare with expected if it exists
    if [ -f "$EXPECTED_FILE" ]; then
        # Diff ignoring whitespace changes
        diff -b "$ACTUAL_COMPARE" "$EXPECTED_FILE" > /dev/null
        if [ $? -eq 0 ]; then
            echo -e "  Result: ${GREEN}PASSED${NC}"
        else
            echo -e "  Result: ${RED}FAILED${NC}"
            echo "  Differences:"
            diff -u "$EXPECTED_FILE" "$ACTUAL_COMPARE" | sed 's/^/    /'
        fi
    else
        echo -e "  Result: ${GREEN}EXECUTED (No expected file)${NC}"
    fi
done

echo "--------------------------------"
echo "Tests complete."

