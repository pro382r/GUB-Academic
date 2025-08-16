#!/bin/bash

> matrix_solution.txt

MAX_THREADS=$(nproc)
THREAD_COUNT=0

wait_for_threads() { while (( THREAD_COUNT >= MAX_THREADS )); do wait -n; ((THREAD_COUNT--)); done; }

readNumber() {
    local prompt=$1 min=$2 max=${3:-9999}
    while true; do
        read -p "$prompt" val
        [[ $val =~ ^-?[0-9]+$ && $val -ge $min && $val -le $max ]] && echo $val && return
        echo "Error: Enter an integer between $min and $max."
    done
}

generateRandomMatrix() {
    local rows=$1 cols=$2 matrix=()
    for ((i = 0; i < rows * cols; i++)); do matrix+=($((RANDOM % 10))); done
    echo "${matrix[@]}"
}

format_number() { awk -v n="$1" 'BEGIN { printf n==int(n) ? "%9d" : "%9.2f", n }'; }

printMatrix() {
    local rows=$1 cols=$2 name=$3 matrix=("${@:4}")
    echo "$name ($rows x $cols):"
    for ((i = 0; i < rows; i++)); do
        for ((j = 0; j < cols; j++)); do format_number "${matrix[i*cols+j]}"; done
        echo
    done
    echo
}

writeMatrixToFile() {
    printMatrix "$@" >> matrix_solution.txt
}

inverse2x2() {
    local name=$1 a=$2 b=$3 c=$4 d=$5 det=$((a*d - b*c))
    ((det == 0)) && { echo "Matrix $name not invertible (det=0)"; return 1; }
    local inv=($(awk -v a="$a" -v b="$b" -v c="$c" -v d="$d" -v det="$det" \
        'BEGIN { printf "%.2f %.2f %.2f %.2f", d/det, -b/det, -c/det, a/det }'))
    printMatrix 2 2 "Inverse ($name)" "${inv[@]}"
    writeMatrixToFile 2 2 "Inverse ($name)" "${inv[@]}"
}

inverse3x3() {
    local name=$1 m=("${@:2}") det=$(awk -v a="${m[0]}" -v b="${m[1]}" -v c="${m[2]}" \
        -v d="${m[3]}" -v e="${m[4]}" -v f="${m[5]}" -v g="${m[6]}" -v h="${m[7]}" -v i="${m[8]}" \
        'BEGIN { print a*(e*i-f*h) - b*(d*i-f*g) + c*(d*h-e*g) }')
    (( $(echo "$det == 0" | bc) )) && { echo "Matrix $name not invertible (det=0)"; return 1; }
    local inv=($(awk -v a="${m[0]}" -v b="${m[1]}" -v c="${m[2]}" -v d="${m[3]}" -v e="${m[4]}" \
        -v f="${m[5]}" -v g="${m[6]}" -v h="${m[7]}" -v i="${m[8]}" -v det="$det" 'BEGIN {
        printf "%.2f %.2f %.2f %.2f %.2f %.2f %.2f %.2f %.2f",
        (e*i-f*h)/det, -(b*i-c*h)/det, (b*f-c*e)/det, -(d*i-f*g)/det, (a*i-c*g)/det,
        -(a*f-c*d)/det, (d*h-e*g)/det, -(a*h-b*g)/det, (a*e-b*d)/det }'))
    printMatrix 3 3 "Inverse ($name)" "${inv[@]}"
    writeMatrixToFile 3 3 "Inverse ($name)" "${inv[@]}"
}

determinant() {
    local name=$1 rows=$2 m=("${@:3}") det
    if [ $rows -eq 2 ]; then
        det=$((m[0]*m[3] - m[1]*m[2]))
    else
        det=$(awk -v a="${m[0]}" -v b="${m[1]}" -v c="${m[2]}" -v d="${m[3]}" -v e="${m[4]}" \
            -v f="${m[5]}" -v g="${m[6]}" -v h="${m[7]}" -v i="${m[8]}" \
            'BEGIN { print a*(e*i-f*h) - b*(d*i-f*g) + c*(d*h-e*g) }')
    fi
    echo "Det($name): $det" | tee -a matrix_solution.txt
}

multiplyMatricesThreaded() {
    local rowsA=$1 colsA=$2 rowsB=$3 colsB=$4 matrixA=("${@:5:$((rowsA*colsA))}")
    local matrixB=("${@:$((5+rowsA*colsA)):$((rowsB*colsB))}")
    local result=($(for ((i=0; i<rowsA*colsB; i++)); do echo 0; done)) tmp=$(mktemp)
    for ((i = 0; i < rowsA; i++)); do
        wait_for_threads
        ((THREAD_COUNT++))
        (
            row=()
            for ((j = 0; j < colsB; j++)); do
                sum=0
                for ((k = 0; k < colsA; k++)); do
                    sum=$((sum + matrixA[i*colsA+k] * matrixB[k*colsB+j]))
                done
                row+=("$sum")
            done
            echo "${row[@]}" >> "$tmp"
        ) &
    done
    wait
    readarray -t lines < "$tmp"
    for ((i = 0; i < rowsA; i++)); do
        IFS=' ' read -r -a vals <<< "${lines[i]}"
        for ((j = 0; j < colsB; j++)); do result[i*colsB+j]=${vals[j]}; done
    done
    rm "$tmp"
    echo "${result[@]}"
}

while true; do
    echo -e "\nMatrix Calculator\n1. Calculate\n2. Exit"
    op=$(readNumber "Option: " 1 2)
    [ $op -eq 2 ] && break

    rowsA=$(readNumber "Rows A: " 1)
    colsA=$(readNumber "Cols A: " 1)
    rowsB=$(readNumber "Rows B: " 1)
    colsB=$(readNumber "Cols B: " 1)

    echo -e "1. Random\n2. Manual\n3. Exit"
    fill=$(readNumber "Fill: " 1 3)
    [ $fill -eq 3 ] && break
    if [ $fill -eq 1 ]; then
        matrixA=($(generateRandomMatrix $rowsA $colsA))
        matrixB=($(generateRandomMatrix $rowsB $colsB))
    else
        matrixA=(); for ((i=0; i<rowsA*colsA; i++)); do matrixA+=($(readNumber "A[$((i/colsA))][$((i%colsA))]: " -9999)); done
        matrixB=(); for ((i=0; i<rowsB*colsB; i++)); do matrixB+=($(readNumber "B[$((i/colsB))][$((i%colsB))]: " -9999)); done
    fi

    printMatrix $rowsA $colsA "Matrix A" "${matrixA[@]}"
    printMatrix $rowsB $colsB "Matrix B" "${matrixB[@]}"
    writeMatrixToFile $rowsA $colsA "Matrix A" "${matrixA[@]}"
    writeMatrixToFile $rowsB $colsB "Matrix B" "${matrixB[@]}"

    echo -e "1. Add\n2. Subtract\n3. Multiply\n4. Inverse\n5. Det\n6. Exit"
    op=$(readNumber "Operation: " 1 6)
    [ $op -eq 6 ] && break

    case $op in
        1|2)
            [ $rowsA -ne $rowsB ] || [ $colsA -ne $colsB ] && { echo "Error: Dimensions mismatch"; continue; }
            result=()
            for ((i=0; i<rowsA*colsA; i++)); do
                [ $op -eq 1 ] && result+=($((matrixA[i] + matrixB[i]))) || result+=($((matrixA[i] - matrixB[i])))
            done
            printMatrix $rowsA $colsA "Result" "${result[@]}"
            writeMatrixToFile $rowsA $colsA "Result" "${result[@]}"
            ;;
        3)
            [ $colsA -ne $rowsB ] && { echo "Error: Invalid dimensions"; continue; }
            result=($(multiplyMatricesThreaded $rowsA $colsA $rowsB $colsB "${matrixA[@]}" "${matrixB[@]}"))
            printMatrix $rowsA $colsB "Result" "${result[@]}"
            writeMatrixToFile $rowsA $colsB "Result" "${result[@]}"
            ;;
        4|5)
            echo -e "1. Matrix A\n2. Matrix B"
            choice=$(readNumber "For: " 1 2)
            if [ $choice -eq 1 ]; then
                [ $rowsA -ne $colsA ] || ! [[ $rowsA =~ ^[2-3]$ ]] && { echo "Error: 2x2 or 3x3 only"; continue; }
                [ $op -eq 4 ] && ([ $rowsA -eq 2 ] && inverse2x2 "A" "${matrixA[@]}" || inverse3x3 "A" "${matrixA[@]}") \
                || determinant "A" $rowsA "${matrixA[@]}"
            else
                [ $rowsB -ne $colsB ] || ! [[ $rowsB =~ ^[2-3]$ ]] && { echo "Error: 2x2 or 3x3 only"; continue; }
                [ $op -eq 4 ] && ([ $rowsB -eq 2 ] && inverse2x2 "B" "${matrixB[@]}" || inverse3x3 "B" "${matrixB[@]}") \
                || determinant "B" $rowsB "${matrixB[@]}"
            fi
            ;;
    esac
    echo "Generated: $(date)" >> matrix_solution.txt
done
