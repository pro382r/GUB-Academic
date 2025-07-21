main(){

echo "--------------FCFS--------------"
echo -n "Enter the number of processes: "
read n

ar1=()

echo "Enter $n burst times:"
for ((i=0; i<n; i++)); do
    read ar1[i]
done

# FCFS Calculations
x=0
y=0
z=0
z1=0

echo -e "\nWaiting time:"
for ((i=0; i<n; i++)); do
    if ((i > 0)); then
        x=$((x + ar1[i-1]))
    fi
    echo "$x"
    y=$((y + x))
done

echo -e "\nTurnaround time:"
for ((i=0; i<n; i++)); do
    z=$((z + ar1[i]))
    echo "$z"
    z1=$((z1 + z))
done

j=$(echo "scale=2; $y / $n" | bc)
k=$(echo "scale=2; $z1 / $n" | bc)

echo "Avg waiting time = $j"
echo "Avg turnaround time = $k"

echo -e "\n\n--------------SJF--------------"

ars=("${ar1[@]}")
sorted_ars=($(printf '%s\n' "${ars[@]}" | sort -n))

printed=()
for ((i=0; i<n; i++)); do
    printed[i]=false
done

echo "Burst time sorting order:"
for ((i=0; i<n; i++)); do
    for ((h=0; h<n; h++)); do
        if (( sorted_ars[i] == ar1[h] )) && [[ ${printed[h]} == false ]]; then
            echo "P$h = ${sorted_ars[i]}"
            printed[h]=true
            break
        fi
    done
done

a2=0
a3=0
b1=0
b2=0

echo -e "\nWaiting time:"
for ((i=0; i<n; i++)); do
    if ((i > 0)); then
        a2=$((a2 + sorted_ars[i-1]))
    fi
    echo "$a2"
    a3=$((a3 + a2))
done

echo -e "\nTurnaround time:"
for ((i=0; i<n; i++)); do
    b1=$((b1 + sorted_ars[i]))
    echo "$b1"
    b2=$((b2 + b1))
done

j1=$(echo "scale=2; $a3 / $n" | bc)
k1=$(echo "scale=2; $b2 / $n" | bc)

echo "Avg waiting time = $j1"
echo "Avg turnaround time = $k1"

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
