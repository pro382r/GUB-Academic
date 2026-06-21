# 1. Sum of Odd and Even Numbers from a Set of Numbers

n = int(input("How many numbers: "))

odd_sum = 0
even_sum = 0

for i in range(n):
    num = int(input("Enter number: "))

    if num % 2 == 0:
        even_sum += num
    else:
        odd_sum += num

print("Sum of Even Numbers =", even_sum)
print("Sum of Odd Numbers =", odd_sum)