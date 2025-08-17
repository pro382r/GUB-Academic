read -p "Enter number of frames: " cap #&& read -p "Enter number of pages: " n
read -p "Enter reference string: " -a pages
declare -a frames
declare -A in_frame
faults=0
show_frames() { echo "Frames: ${frames[*]:-empty}"; }
for p in "${pages[@]}"; do
    if [[ ${in_frame[$p]} ]]; then
        frames=(${frames[@]/$p/} "$p")
        echo "Page $p: Hit"
        ((hit++))
    else
        ((faults++))
        if ((${#frames[@]} == cap)); then
            unset in_frame[${frames[0]}]
            frames=("${frames[@]:1}")
        fi
        frames+=("$p")
        in_frame[$p]=1
        echo "Page $p: Fault"
    fi
    show_frames
done
echo -e "Total page faults: $faults \nTotal page hits: $hit"

  echo -e "\n=========================END=========================="
echo "
██████╗ ███████╗ █████╗ ██╗  ██╗ ██████╗  ██████╗ ███╗   ██╗
██╔══██╗██╔════╝██╔══██╗██║  ██║██╔═══██╗██╔═══██╗████╗  ██║
██████╔╝█████╗  ███████║███████║██║   ██║██║   ██║██╔██╗ ██║
██╔══██╗██╔══╝  ██╔══██║██╔══██║██║   ██║██║   ██║██║╚██╗██║
██║  ██║███████╗██║  ██║██║  ██║╚██████╔╝╚██████╔╝██║ ╚████║
╚═╝  ╚═╝╚══════╝╚═╝  ╚═╝╚═╝  ╚═╝ ╚═════╝  ╚═════╝ ╚═╝  ╚═══╝
"
