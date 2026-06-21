# 8. Count Even and Odd Numbers in a List

numbers = [10, 15, 20, 25, 30, 35, 40]

even_count = 0
odd_count = 0

for num in numbers:
    if num % 2 == 0:
        even_count += 1
    else:
        odd_count += 1

print("Even Numbers =", even_count)
print("Odd Numbers =", odd_count)