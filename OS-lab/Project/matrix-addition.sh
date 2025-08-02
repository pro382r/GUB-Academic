#30 % done

#!/bin/bash

# Function to read a matrix from user input
read_matrix() {
    local name=$1
    local rows=$2
    local cols=$3

    echo "Enter values for Matrix $name ($rows x $cols):"
    declare -A matrix

    for ((i=0; i<rows; i++)); do
        for ((j=0; j<cols; j++)); do
            read -p "$name[$i,$j]: " value
            matrix[$i,$j]=$value
        done
    done

    # Return the matrix as a space-separated string
    local result=""
    for ((i=0; i<rows; i++)); do
        for ((j=0; j<cols; j++)); do
            result+=" ${matrix[$i,$j]}"
        done
    done
    echo "$result"
}

# Function to add two matrices
add_matrices() {
    local matrixA=($1)
    local matrixB=($2)
    local rows=$3
    local cols=$4

    declare -A result

    for ((i=0; i<rows; i++)); do
        for ((j=0; j<cols; j++)); do
            index=$((i * cols + j))
            result[$i,$j]=$(( ${matrixA[$index]} + ${matrixB[$index]} ))
        done
    done

    # Format the result matrix as a string
    local output=""
    for ((i=0; i<rows; i++)); do
        for ((j=0; j<cols; j++)); do
            output+=" ${result[$i,$j]}"
        done
    done
    echo "$output"
}

# Function to print a matrix
print_matrix() {
    local matrix=($1)
    local rows=$2
    local cols=$3
    local name=$4

    echo "$name:"
    for ((i=0; i<rows; i++)); do
        for ((j=0; j<cols; j++)); do
            index=$((i * cols + j))
            printf "%4d" "${matrix[$index]}"
        done
        echo
    done
}

# Main script
echo "MATRIX ADDITION (A + B = C)"
read -p "Enter number of rows: " rows
read -p "Enter number of columns: " cols

# Read matrices A and B
matrixA=$(read_matrix "A" "$rows" "$cols")
matrixB=$(read_matrix "B" "$rows" "$cols")

# Compute matrix C (A + B)
matrixC=$(add_matrices "$matrixA" "$matrixB" "$rows" "$cols")

# Print all matrices
echo
print_matrix "$matrixA" "$rows" "$cols" "Matrix A"
print_matrix "$matrixB" "$rows" "$cols" "Matrix B"
print_matrix "$matrixC" "$rows" "$cols" "Matrix C (A + B)"

# Save results to a file
output_file="matrix_result.txt"
{
    echo "MATRIX OPERATION RESULT"
    echo "-----------------------"
    echo "Matrix A:"
    print_matrix "$matrixA" "$rows" "$cols" "A" | tail -n +2
    echo
    echo "Matrix B:"
    print_matrix "$matrixB" "$rows" "$cols" "B" | tail -n +2
    echo
    echo "Matrix C (A + B):"
    print_matrix "$matrixC" "$rows" "$cols" "C" | tail -n +2
    echo
    echo "Generated on: $(date)"
} > "$output_file"

echo
echo "Result saved to $output_file"
