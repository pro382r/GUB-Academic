read -p "Enter the number of frames: " fc
read -p "Enter the page reference string (space-separated integers): " -a ps
frames=()
declare -A inf
pf=0
for p in "${ps[@]}"; do
  if [[ ${inf[$p]} ]]; then
    nf=()
    for f in "${frames[@]}"; do
      [[ $f != $p ]] && nf+=("$f")
    done
    nf+=("$p")
    frames=("${nf[@]}")
    echo "Page $p: Hit"
  else
    ((pf++))
    [[ ${#frames[@]} == $fc ]] && {
      lru=${frames[0]}
      unset inf[$lru]
      frames=("${frames[@]:1}")
    }
    frames+=("$p")
    inf[$p]=1
    echo "Page $p: Fault"
  fi
  echo -n "Current frames: "
  for f in "${frames[@]}"; do echo -n "$f "; done
  echo
done
echo "Total page faults: $pf"