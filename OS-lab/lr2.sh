main(){
echo "--------------Hybrid Scheduling (Priority > SJF > FCFS)--------------"
echo -n "Enter the number of processes: "
read n

declare -a processes
declare -a burst
declare -a priority

for ((i=0; i<n; i++)); do
    processes[i]="P$((i+1))"
done

echo "Enter the burst times for each process:"
for ((i=0; i<n; i++)); do
    echo -n "${processes[i]}: "
    read burst[i]
done

# Input Priorities
echo "Enter the priorities for each process (lower number = higher priority):"
for ((i=0; i<n; i++)); do
    echo -n "${processes[i]}: "
    read priority[i]
done

# Sort by Priority > SJF > FCFS
for ((i=0; i<n-1; i++)); do
    for ((j=i+1; j<n; j++)); do
        if (( priority[i] > priority[j] )) || 
           (( priority[i] == priority[j] && burst[i] > burst[j] )); then
            # Swap priority
            temp=${priority[i]}
            priority[i]=${priority[j]}
            priority[j]=$temp
            # Swap burst
            temp=${burst[i]}
            burst[i]=${burst[j]}
            burst[j]=$temp
            # Swap process name
            temp=${processes[i]}
            processes[i]=${processes[j]}
            processes[j]=$temp
        fi
    done
done

declare -a waiting_time
declare -a turnaround_time
wt=0
tt=0
total_wt=0
total_tt=0

for ((i=0; i<n; i++)); do
    waiting_time[i]=$wt
    tt=$((wt + burst[i]))
    turnaround_time[i]=$tt
    wt=${turnaround_time[i]}
    total_wt=$((total_wt + waiting_time[i]))
    total_tt=$((total_tt + turnaround_time[i]))
done

echo -e "\nTable: Final Scheduling Result"
printf "%-10s %-12s %-10s %-15s %-17s\n" "Process" "Burst Time" "Priority" "Waiting Time" "Turnaround Time"
for ((i=0; i<n; i++)); do
    printf "%-10s %-12s %-10s %-15s %-17s\n" "${processes[i]}" "${burst[i]}" "${priority[i]}" "${waiting_time[i]}" "${turnaround_time[i]}"
done

avg_wt=$(echo "scale=2; $total_wt / $n" | bc)
avg_tt=$(echo "scale=2; $total_tt / $n" | bc)

echo -e "\nAverage Waiting Time: $avg_wt"
echo "Average Turnaround Time: $avg_tt"

echo "==========================END==========================="
echo "
 ____            _                       
|  _ \ ___  __ _| |__   ___   ___  _ __  
| |_) / _ \/ _\ | '_ \ / _ \ / _ \| '_ \ 
|  _ <  __/ (_| | | | | (_) | (_) | | | |
|_| \_\___|\__,_|_| |_|\___/ \___/|_| |_|
"
}

main
