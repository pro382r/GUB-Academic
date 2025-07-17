echo "Enter array process time: "
read -a p1

p1s=${#p1[@]}

sortp1=($(for num in "${p1[@]}"; do echo "$num"; done | sort -n))

echo "Waiting time : "

for ((i=0; i<p1s; i++)); do
    k=$(($k+${p1[i]}))
    echo "Process at index $k"
done
