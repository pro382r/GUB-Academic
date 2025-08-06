#!/bin/bash

> matrix_solution.txt

# Thread control variables
MAX_THREADS=$(nproc)  # Number of CPU cores available
THREAD_COUNT=0

# Function to wait for threads to complete when max threads are reached
wait_for_threads() {
    while (( THREAD_COUNT >= MAX_THREADS )); do
        wait -n
        ((THREAD_COUNT--))
    done
}

readNumber() {
    local prompt=$1
    local min_val=$2
    local max_val=${3:-2147483647}
    local value

    while true; do
        read -p "$prompt" value
        if [[ $value =~ ^-?[0-9]+$ ]]; then
            if (( value >= min_val && value <= max_val )); then
                echo $value
                return
            else
                echo "Error: Input must be between $min_val and $max_val."
            fi
        else
            echo "Error: Invalid input. Please enter an integer."
        fi
    done
}

generateRandomMatrix() {
    local rows=$1
    local cols=$2
    local matrix=()
    for ((i = 0; i < rows * cols; i++)); do
        matrix+=($((RANDOM % 10)))
    done
    echo "${matrix[@]}"
}

format_number() {
    awk -v num="$1" 'BEGIN {
        if (num == int(num)) {
            printf "%9d", num
        } else {
            printf "%9.2f", num
        }
    }'
}

printMatrix() {
    local rows=$1
    local cols=$2
    local name=$3
    shift 3
    local matrix=("$@")
    echo "$name ($rows x $cols):"
    for ((i = 0; i < rows; i++)); do
        for ((j = 0; j < cols; j++)); do
            idx=$((i * cols + j))
            format_number "${matrix[idx]}"
        done
        echo
    done
    echo
}

writeMatrixToFile() {
    local rows=$1
    local cols=$2
    local name=$3
    shift 3
    local matrix=("$@")

    echo "$name ($rows x $cols):" >> matrix_solution.txt
    for ((i = 0; i < rows; i++)); do
        for ((j = 0; j < cols; j++)); do
            idx=$((i * cols + j))
            format_number "${matrix[idx]}" >> matrix_solution.txt
        done
        echo >> matrix_solution.txt
    done
    echo >> matrix_solution.txt
}

inverse2x2() {
    local matrix_name=$1
    shift 1
    local a=$1 b=$2 c=$3 d=$4
    local det=$((a * d - b * c))
    if [ "$det" -eq 0 ]; then
        echo "Matrix $matrix_name is not invertible (det=0)"
        return 1
    fi

    local inv=( $(awk -v a="$a" -v b="$b" -v c="$c" -v d="$d" -v det="$det" 'BEGIN {
        printf "%.6f %.6f %.6f %.6f", d/det, -b/det, -c/det, a/det
    }') )

    echo "Inverse of Matrix $matrix_name:"
    printMatrix 2 2 "Inverse Matrix ($matrix_name)" "${inv[@]}"
    writeMatrixToFile 2 2 "Inverse Matrix ($matrix_name)" "${inv[@]}"
}

inverse3x3() {
    local matrix_name=$1
    shift 1
    local m=("$@")

    local a=${m[0]} b=${m[1]} c=${m[2]}
    local d=${m[3]} e=${m[4]} f=${m[5]}
    local g=${m[6]} h=${m[7]} i=${m[8]}

    local det=$(awk -v a="$a" -v b="$b" -v c="$c" -v d="$d" -v e="$e" -v f="$f" -v g="$g" -v h="$h" -v i="$i" 'BEGIN {
        print a*(e*i - f*h) - b*(d*i - f*g) + c*(d*h - e*g)
    }')

    if awk "BEGIN {exit !($det == 0)}"; then
        echo "Matrix $matrix_name is not invertible (det=0)"
        return 1
    fi

    local cofactor=( $(awk -v a="$a" -v b="$b" -v c="$c" -v d="$d" -v e="$e" -v f="$f" -v g="$g" -v h="$h" -v i="$i" 'BEGIN {
        printf "%.6f %.6f %.6f ",  (e*i - f*h), -(d*i - f*g),  (d*h - e*g)
        printf "%.6f %.6f %.6f ", -(b*i - c*h),  (a*i - c*g), -(a*h - b*g)
        printf "%.6f %.6f %.6f",   (b*f - c*e), -(a*f - c*d),  (a*e - b*d)
    }') )

    local adjoint=()
    for ((col = 0; col < 3; col++)); do
        for ((row = 0; row < 3; row++)); do
            adjoint+=( "${cofactor[$((row * 3 + col))]}" )
        done
    done

    local inverse=()
    for val in "${adjoint[@]}"; do
        inverse+=( $(awk -v val="$val" -v d="$det" 'BEGIN {printf "%.6f", val / d}') )
    done

    echo "Inverse of Matrix $matrix_name:"
    printMatrix 3 3 "Inverse Matrix ($matrix_name)" "${inverse[@]}"
    writeMatrixToFile 3 3 "Inverse Matrix ($matrix_name)" "${inverse[@]}"
}

calculateDeterminant2x2() {
    local matrix_name=$1
    shift 1
    local a=$1 b=$2 c=$3 d=$4
    local det=$((a * d - b * c))
    echo "Determinant of Matrix $matrix_name: $det"
    echo "Determinant of Matrix $matrix_name: $det" >> matrix_solution.txt
}

calculateDeterminant3x3() {
    local matrix_name=$1
    shift 1
    local m=("$@")
    local a=${m[0]} b=${m[1]} c=${m[2]}
    local d=${m[3]} e=${m[4]} f=${m[5]}
    local g=${m[6]} h=${m[7]} i=${m[8]}

    local det=$(awk -v a="$a" -v b="$b" -v c="$c" -v d="$d" -v e="$e" -v f="$f" -v g="$g" -v h="$h" -v i="$i" 'BEGIN {
        print a*(e*i - f*h) - b*(d*i - f*g) + c*(d*h - e*g)
    }')
    echo "Determinant of Matrix $matrix_name: $det"
    echo "Determinant of Matrix $matrix_name: $det" >> matrix_solution.txt
}

# Threaded matrix multiplication
multiplyMatricesThreaded() {
    local rowsA=$1
    local colsA=$2
    local rowsB=$3
    local colsB=$4
    local matrixA=("${@:5:$((rowsA * colsA))}")
    local matrixB=("${@:$((5 + rowsA * colsA))}")
    local result_matrix=()

    # Initialize result matrix with zeros
    for ((i = 0; i < rowsA * colsB; i++)); do
        result_matrix+=(0)
    done

    # Create a temporary file for thread output
    local temp_file=$(mktemp)

    # Multiply matrices using threads (one thread per row of result)
    for ((i = 0; i < rowsA; i++)); do
        wait_for_threads
        ((THREAD_COUNT++))
        
        (
            row_results=()
            for ((j = 0; j < colsB; j++)); do
                sum=0
                for ((k = 0; k < colsA; k++)); do
                    sum=$((sum + matrixA[i*colsA+k] * matrixB[k*colsB+j]))
                done
                row_results+=("$sum")
            done
            
            # Write results to temp file with lock
            (
                flock -x 200
                for ((j = 0; j < colsB; j++)); do
                    result_matrix[i*colsB+j]=${row_results[j]}
                done
                printf "%s\n" "${row_results[@]}" >> "$temp_file"
            ) 200>"$temp_file.lock"
        ) &
    done

    wait  # Wait for all threads to complete

    # Read results from temp file
    local line_num=0
    while read -r line; do
        IFS=' ' read -ra values <<< "$line"
        for ((j = 0; j < colsB; j++)); do
            result_matrix[line_num*colsB+j]=${values[j]}
        done
        ((line_num++))
    done < "$temp_file"

    # Clean up temp files
    rm -f "$temp_file" "$temp_file.lock"

    # Return the result matrix
    echo "${result_matrix[@]}"
}

# === MAIN ===
while true; do
    echo "========================================"
    echo "     Bash Matrix Calculator (Threads)"
    echo "========================================"
    echo "1. Start New Calculation"
    echo "2. Exit"
    main_menu_option=$(readNumber "Select option: " 1 2)

    if [ "$main_menu_option" -eq 2 ]; then
        echo "Exiting program."
        break
    fi

    echo "--- Matrix A Dimensions ---"
    rowsA=$(readNumber "Enter number of rows for Matrix A: " 1)
    colsA=$(readNumber "Enter number of columns for Matrix A: " 1)

    echo "--- Matrix B Dimensions ---"
    rowsB=$(readNumber "Enter number of rows for Matrix B: " 1)
    colsB=$(readNumber "Enter number of columns for Matrix B: " 1)

    echo "1. Auto Generate Random Values"
    echo "2. Manually Input Values"
    echo "3. Exit Program"
    fill_option=$(readNumber "Select option: " 1 3)
    if [ "$fill_option" -eq 3 ]; then
        echo "Exiting program."
        break
    fi

    if [ "$fill_option" -eq 1 ]; then
        matrixA=($(generateRandomMatrix $rowsA $colsA))
        matrixB=($(generateRandomMatrix $rowsB $colsB))
    else
        echo "Enter values for Matrix A:"
        matrixA=()
        for ((i = 0; i < rowsA; i++)); do
            for ((j = 0; j < colsA; j++)); do
                val=$(readNumber "Matrix A [$i][$j]: " -9999)
                matrixA+=($val)
            done
        done

        echo "Enter values for Matrix B:"
        matrixB=()
        for ((i = 0; i < rowsB; i++)); do
            for ((j = 0; j < colsB; j++)); do
                val=$(readNumber "Matrix B [$i][$j]: " -9999)
                matrixB+=($val)
            done
        done
    fi

    printMatrix $rowsA $colsA "Matrix A" "${matrixA[@]}"
    printMatrix $rowsB $colsB "Matrix B" "${matrixB[@]}"
    writeMatrixToFile $rowsA $colsA "Matrix A" "${matrixA[@]}"
    writeMatrixToFile $rowsB $colsB "Matrix B" "${matrixB[@]}"

    echo "1. Addition"
    echo "2. Subtraction"
    echo "3. Multiplication (Threaded)"
    echo "4. Inverse of Matrix A and B (2x2 or 3x3)"
    echo "5. Determinant of Matrix A and B (2x2 or 3x3)"
    echo "6. Exit Program"
    op=$(readNumber "Choose operation: " 1 6)

    if [ "$op" -eq 6 ]; then
        echo "Exiting program."
        break
    fi

    result_matrix=()
    case $op in
        1|2)
            if [ $rowsA -ne $rowsB ] || [ $colsA -ne $colsB ]; then
                echo "Error: Dimensions must match for addition/subtraction."
                continue
            fi
            for ((i = 0; i < rowsA * colsA; i++)); do
                if [ $op -eq 1 ]; then
                    result_matrix+=($((matrixA[i] + matrixB[i])))
                else
                    result_matrix+=($((matrixA[i] - matrixB[i])))
                fi
            done
            printMatrix $rowsA $colsA "Result Matrix" "${result_matrix[@]}"
            writeMatrixToFile $rowsA $colsA "Result Matrix" "${result_matrix[@]}"
            ;;
        3)
            if [ $colsA -ne $rowsB ]; then
                echo "Error: Matrix A columns must equal Matrix B rows for multiplication."
                continue
            fi
            
            echo "Calculating multiplication using $MAX_THREADS threads..."
            result_matrix=($(multiplyMatricesThreaded $rowsA $colsA $rowsB $colsB "${matrixA[@]}" "${matrixB[@]}"))
            
            printMatrix $rowsA $colsB "Result Matrix" "${result_matrix[@]}"
            writeMatrixToFile $rowsA $colsB "Result Matrix" "${result_matrix[@]}"
            ;;
        4)
            echo "Calculate inverse for which matrix?"
            echo "1. Matrix A"
            echo "2. Matrix B"
            inverse_matrix_choice=$(readNumber "Select option: " 1 2)

            if [ "$inverse_matrix_choice" -eq 1 ]; then
                if [ "$rowsA" -eq 2 ] && [ "$colsA" -eq 2 ]; then
                    inverse2x2 "A" "${matrixA[@]}"
                elif [ "$rowsA" -eq 3 ] && [ "$colsA" -eq 3 ]; then
                    inverse3x3 "A" "${matrixA[@]}"
                else
                    echo "Only 2x2 or 3x3 matrices supported for inversion."
                fi
            else
                if [ "$rowsB" -eq 2 ] && [ "$colsB" -eq 2 ]; then
                    inverse2x2 "B" "${matrixB[@]}"
                elif [ "$rowsB" -eq 3 ] && [ "$colsB" -eq 3 ]; then
                    inverse3x3 "B" "${matrixB[@]}"
                else
                    echo "Only 2x2 or 3x3 matrices supported for inversion."
                fi
            fi
            ;;
        5)
            echo "Calculate determinant for which matrix?"
            echo "1. Matrix A"
            echo "2. Matrix B"
            determinant_matrix_choice=$(readNumber "Select option: " 1 2)

            if [ "$determinant_matrix_choice" -eq 1 ]; then
                if [ "$rowsA" -eq 2 ] && [ "$colsA" -eq 2 ]; then
                    calculateDeterminant2x2 "A" "${matrixA[@]}"
                elif [ "$rowsA" -eq 3 ] && [ "$colsA" -eq 3 ]; then
                    calculateDeterminant3x3 "A" "${matrixA[@]}"
                else
                    echo "Only 2x2 or 3x3 matrices supported for determinant calculation."
                fi
            else
                if [ "$rowsB" -eq 2 ] && [ "$colsB" -eq 2 ]; then
                    calculateDeterminant2x2 "B" "${matrixB[@]}"
                elif [ "$rowsB" -eq 3 ] && [ "$colsB" -eq 3 ]; then
                    calculateDeterminant3x3 "B" "${matrixB[@]}"
                else
                    echo "Only 2x2 or 3x3 matrices supported for determinant calculation."
                fi
            fi
            ;;
    esac
    echo "Generated on: $(date)" >> matrix_solution.txt
done
#threads optimized
