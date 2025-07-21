//FCFS and SJF Scheduling Algorithm.


#include<bits/stdc++.h>
using namespace std;

int main(){

int i, n, x=0, y=0, z, z1;
cout << "--------------FCFS-------------- \nEnter the number of process: \n";
cin >> n;

vector<int> ar1(n);

cout << "Enter " << n << " Brust time: " << endl;
for(i=0; i<n; i++){
  cin >> ar1[i];
}

cout << "\nWaiting time: \n";
for(i=0; i<n; i++){
  x = x + ar1[i-1];
  cout << x << endl;
  y = y+x;
}

cout << "\nTurn around time: \n";
for(i=0; i<n; i++){
  z = z + ar1[i];
  cout << z << endl;
  z1= z1+z;
}

float j=0.0f,k=0.0f;

j = static_cast<float>(y) /n;
k = static_cast<float>(z1)/n;

cout << "Avg waiting time = " << j << endl;
cout << "Avg turn around time = " << k << endl;




cout << "\n\n--------------SJF-------------- \n";

vector<int>ars(n);
int a1, h, a2=0, a3=0, b1, b2;
for(i=0; i<n; i++){
  ars[i] = ar1[i];
}

sort( ars.begin(), ars.end() );

vector<bool> printed(n, false);

cout << "Burst time sorting order: \n";
for(i=0; i<n; i++){
  for(h=0; h<n; h++){
    if(ars[i]==ar1[h] && !printed[h]){
        cout << "P" << h << " = " << ars[i] << endl;
        printed[h] = true;
        break;
    }
  }
}


cout << "\nWaiting time: \n";
for(i=0; i<n; i++){
  a2 = a2 + ars[i-1];
  cout << a2 << endl;
  a3 = a3+a2;
}

cout << "\nTurn around time: \n";
for(i=0; i<n; i++){
  b1 = b1 + ars[i];
  cout << b1 << endl;
  b2= b2+b1;
}

float j1=0.0f,k1=0.0f;

j1 = static_cast<float>(a3) /n;
k1 = static_cast<float>(b2)/n;

cout << "Avg waiting time = " << j1 << endl;
cout << "Avg turn around time = " << k1 << endl;

return 0;
}
