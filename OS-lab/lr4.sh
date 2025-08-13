main() {

read -p "Enter number of processes: " n
for ((i=0;i<n;i++)); do
    read -p "Enter Burst Time for process $((i+1)): " bt[i]
    rem_bt[i]=${bt[i]}
done
read -p "Enter Time Quantum: " tq

time=0
done=0
wt_sum=0
tat_sum=0

while (( done < n )); do
    for ((i=0;i<n;i++)); do
        (( rem_bt[i] > 0 )) || continue
        if (( rem_bt[i] <= tq )); then
            (( time += rem_bt[i], rem_bt[i]=0, tat[i]=time, done++ ))
        else
            (( time += tq, rem_bt[i] -= tq ))
        fi
    done
done

printf "\nProcess\tBT\tWT\tTAT\n"
for ((i=0;i<n;i++)); do
    wt[i]=$(( tat[i] - bt[i] ))
    (( wt_sum += wt[i], tat_sum += tat[i] ))
    printf "P%d\t%s\t%s\t%s\n" $((i+1)) "${bt[i]}" "${wt[i]}" "${tat[i]}"
done

printf "\nAverage Waiting Time: %.2f\n" "$(echo "scale=4; $wt_sum / $n" | bc -l)"
printf "Average Turnaround Time: %.2f\n" "$(echo "scale=4; $tat_sum / $n" | bc -l)"

echo " "
echo "==========================END==========================="
echo "
██████╗ ███████╗ █████╗ ██╗  ██╗ ██████╗  ██████╗ ███╗   ██╗
██╔══██╗██╔════╝██╔══██╗██║  ██║██╔═══██╗██╔═══██╗████╗  ██║
██████╔╝█████╗  ███████║███████║██║   ██║██║   ██║██╔██╗ ██║
██╔══██╗██╔══╝  ██╔══██║██╔══██║██║   ██║██║   ██║██║╚██╗██║
██║  ██║███████╗██║  ██║██║  ██║╚██████╔╝╚██████╔╝██║ ╚████║
╚═╝  ╚═╝╚══════╝╚═╝  ╚═╝╚═╝  ╚═╝ ╚═════╝  ╚═════╝ ╚═╝  ╚═══╝
"
}

main