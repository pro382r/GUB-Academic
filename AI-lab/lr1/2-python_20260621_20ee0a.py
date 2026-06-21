# 2. Sum of Numbers Between 50 and 100 Divisible by 3 but Not by 5

total = 0

for i in range(50, 101):
    if (i % 3 == 0) and (i % 5 != 0):
        total += i

print("Sum =", total)