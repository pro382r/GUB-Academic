
main(){
 echo "------------------Find the sum of odd and even------------------"
 echo "Enter numbers separated by spaces : "
 read -r -a Ar1
 p=0
 q=0
 arsiz=$(echo "${#Ar1[@]}" | bc)
 
 for (( i=0; i<$arsiz; i++)); do
  #echo "Element $i: ${Ar1[i]}"
  c="${Ar1[i]}"
  if (( $c%2==0 )); then
   p=$(echo "$p+$c"| bc)
  else
   q=$(echo "$q+$c"| bc)
  fi
 done
 
 echo "Sum of odd is $q and Sum of even is $p"
 
 
 echo "------------------Check valid triangle------------------"
 echo "Input 3 edge: "
 read t1 t2 t3
 
 #t4=$(( t1 > t2 ? (t1 > t3 ? t1 : t2) : (t2 > t3 ? t2 : t3) ))
 
 if (( $t1+$t2>$t3 && $t1+$t3>$t2 && $t2+$t3>$t1 )); then
  echo "Valid triangle."
 else echo "Not valid triangle."
 fi
 
 
 echo "------------------Sum 50-100: Divisible by 3, Not 5------------------"
 sum1=0
 for (( i=50; i<101; i++ )); do
  if (( i%3==0 && i%5!=0)); then
   sum1=$(echo "$sum1+$i" | bc)
  #-----
  fi
 done
 echo "The sum is = $sum1"
 
 
 echo "------------------Find factorial using loop------------------"
 read -p "Enter a number= " fc1
 fc2=$fc1
 for (( i=fc1-1; i>0; i-- )); do
  fc2=$(echo "$fc2 * $i" | bc)
 done
 echo "Factorial = $fc2"
 
 
 echo "------------------Generate Fibonacci series------------------"
 read -p "Enter a number= " fn
 arr2=()
 arr2+=(0)
 arr2+=(1)
 echo "Fibonacci =
0
1"
 for (( i=2; i<fn; i++ )); do
  fn1="${arr2[i-2]}"
  fn2="${arr2[i-1]}"
  fn3=$(echo "$fn1 + $fn2" | bc)
  arr2+=($fn3)
  echo "$fn3 "
 done
 
 echo "============================END==============================="
 
 echo "
 ____            _                       
|  _ \ ___  __ _| |__   ___   ___  _ __  
| |_) / _ \/ _\ | '_ \ / _ \ / _ \| '_ \ 
|  _ <  __/ (_| | | | | (_) | (_) | | | |
|_| \_\___|\__,_|_| |_|\___/ \___/|_| |_|
"
}
main

