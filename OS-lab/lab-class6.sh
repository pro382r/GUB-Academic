#FIFO algorithm

#!/bin/bash
read -p "Enter number of pages: " n
read -p "Enter pages: " -a pages
read -p "Enter number of frames: " f

memory=()
for ((i=0; i<f; i++)); do memory[i]=-1; done
pageFault=0
idx=0

echo "Page Replacement Process:"
for ((i=0; i<n; i++)); do
  p=${pages[i]}
  found=0
  for ((j=0; j<f; j++)); do
    [[ ${memory[j]} -eq $p ]] && found=1 && break
  done
  if (( found == 0 )); then
    memory[idx]=$p
    ((pageFault++))
    ((idx++))
    (( idx == f )) && idx=0
  fi
  printf '%s\n' "$(printf '\t%s' "${memory[@]}")$([[ $found == 0 ]] && echo -e "\tPage Fault No: $pageFault")"
done
echo "Total Page Faults: $pageFault"