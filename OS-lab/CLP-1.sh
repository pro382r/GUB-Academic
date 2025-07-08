echo "Enter a and b "
read a b

c=$(echo "$a * $b * 0.5" | bc)

echo "Area of triangle is $c"
