#!/bin/bash

main() {
    echo "Enter the number of blocks: "
    read b

    declare -a bl

    echo "Enter the $b block sizes: "
    for((i=0;i<$b;i++))
    do
        read bl[$i]
    done

    echo "Enter number of processes: "
    read p

    declare -a pr

    echo "Enter the $p process sizes: "
    for((i=0;i<$p;i++))
    do
        read pr[$i]
    done

    printf "\n%-12s%-16s%-12s%-14s%-13s%-14s\n" "Process" "Process size" "Block no." "Block size" "Allocated" "Inter. Frag."

    m=$(( b < p ? b : p ))
    k=0
    tif=0

    for((i=0;i<$m;i++))
    do
        if [ ${pr[$i]} -gt ${bl[$((i-k))]} ]; then
            printf "%-12d%-16d%-12d%-14d%-13s%-14s\n" $((i+1)) ${pr[$i]} $((i+1-k)) ${bl[$i-$k]} "No" "---"
            k=$((k+1))
        else
            frag=$(( ${bl[$i-$k]} - ${pr[$i]} ))
            printf "%-12d%-16d%-12d%-14d%-13s%-14d\n" $((i+1)) ${pr[$i]} $((i+1-k)) ${bl[$i-$k]} "Yes" $frag
            tif=$((tif + frag))
        fi
    done

    echo ""
    echo "Total internal fragmentation: $tif"
    echo "==========================END==========================="
    echo "
 ____            _                       
|  _ \ ___  __ _| |__   ___   ___  _ __  
| |_) / _ \/ _\ | '_ \ / _ \ / _ \| '_ \ 
|  _ <  __/ (_| | | | | (_) | (_) | | | |
|_| \_\___|\__,_|_| |_|\___/ \___/|_| |_|
"
}

