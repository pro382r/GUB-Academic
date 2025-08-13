#!/bin/bash

# Script to simulate LRU Page Replacement Algorithm

echo "Enter the number of frames:"
read frames_capacity

echo "Enter the number of pages in the reference string:"
read num_pages

echo "Enter the page reference string (space-separated integers):"
read -a page_string

# Initialize frames as an array (to maintain order: index 0 is LRU)
declare -a frames

# Associative array for quick lookup if a page is in frames
declare -A page_in_frames

# Counter for page faults
page_faults=0

# Function to print current frames
print_frames() {
    echo -n "Current frames: "
    for frame in "${frames[@]}"; do
        echo -n "$frame "
    done
    echo ""
}

for page in "${page_string[@]}"; do
    if [[ ${page_in_frames[$page]} ]]; then
        # Page hit: Remove page from its position and move to end (most recently used)
        new_frames=()
        for f in "${frames[@]}"; do
            if [[ $f != $page ]]; then
                new_frames+=("$f")
            fi
        done
        new_frames+=("$page")
        frames=("${new_frames[@]}")
        echo "Page $page: Hit"
    else
        # Page fault
        ((page_faults++))
        if [[ ${#frames[@]} == $frames_capacity ]]; then
            # Frames full: Evict LRU (first element)
            lru_page=${frames[0]}
            unset page_in_frames[$lru_page]
            # Shift frames left
            frames=("${frames[@]:1}")
        fi
        # Add new page to end
        frames+=("$page")
        page_in_frames[$page]=1
        echo "Page $page: Fault"
    fi
    print_frames
done

echo "Total page faults: $page_faults"