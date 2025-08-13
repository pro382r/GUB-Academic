read -p "Enter number of frames: " FRAMES
if ! [[ "$FRAMES" =~ ^[0-9]+$ ]] || [ "$FRAMES" -le 0 ]; then
  echo "Invalid frame count."
  exit 1
fi

read -p "Enter space-separated page reference string: " -a REF

for ((i=0;i<FRAMES;i++)); do
  frames[i]=-1
done

declare -A last_used   # last_used[page]=time_index
hits=0
misses=0

printf "%-6s %-6s | " "Step" "Page"
for ((i=0;i<FRAMES;i++)); do
  printf "F%-2d " $((i+1))
done
printf "| %-5s %-7s\n" "Hit?" "Evict"

echo "-------------------------------------------------------------"

time_index=0
for page in "${REF[@]}"; do
  if ! [[ "$page" =~ ^-?[0-9]+$ ]]; then
    echo "Skipping non-numeric token: $page"
    continue
  fi

  ((time_index++))

  # Check hit
  hit=0
  evicted="-"
  for ((i=0;i<FRAMES;i++)); do
    if [ "${frames[i]}" == "$page" ]; then
      hit=1
      last_used[$page]=$time_index
      break
    fi
  done

  if [ $hit -eq 1 ]; then
    ((hits++))
  else
    ((misses++))
    # Find empty frame first
    placed=0
    for ((i=0;i<FRAMES;i++)); do
      if [ "${frames[i]}" == "-1" ]; then
        frames[i]=$page
        last_used[$page]=$time_index
        placed=1
        break
      fi
    done

    if [ $placed -eq 0 ]; then
      # Evict least recently used page
      lru_index=-1
      lru_time=999999999
      for ((i=0;i<FRAMES;i++)); do
        p="${frames[i]}"
        lu="${last_used[$p]}"
        if [ -z "$lu" ]; then
          lu=-1
        fi
        if [ "$lu" -lt "$lru_time" ]; then
          lru_time="$lu"
          lru_index="$i"
        fi
      done
      evicted="${frames[$lru_index]}"
      frames[$lru_index]="$page"
      last_used[$page]=$time_index
    fi
  fi

  # Print row
  printf "%-6d %-6s | " "$time_index" "$page"
  for ((i=0;i<FRAMES;i++)); do
    if [ "${frames[i]}" == "-1" ]; then
      printf "%-3s " "-"
    else
      printf "%-3s " "${frames[i]}"
    fi
  done

  if [ $hit -eq 1 ]; then
    printf "| %-5s %-7s\n" "Yes" "-"
  else
    printf "| %-5s %-7s\n" "No" "$evicted"
  fi
done

echo "-------------------------------------------------------------"
echo "Total References : $time_index"
echo "Hits             : $hits"
echo "Misses           : $misses"
if [ $time_index -gt 0 ]; then
  hit_ratio=$(awk "BEGIN { printf \"%.4f\", $hits/$time_index }")
  miss_ratio=$(awk "BEGIN { printf \"%.4f\", $misses/$time_index }")
  echo "Hit Ratio        : $hit_ratio"
  echo "Miss Ratio       : $miss_ratio"
fi
