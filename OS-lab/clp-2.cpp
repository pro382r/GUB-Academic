//Hybrid scheduling algorithm

#include<bits/stdc++.h>
using namespace std;

int main(){
int i,j,k,x,y,z,a,b,c,n,wt=0, tt=0;

cout << "enter process number: " << endl;
cin >> n;

vector<int>ar1(n);//process brust time
vector<int>ar2(n);//priority
vector<int>ar3(n);
vector<int>ar4(n);
vector<int>ar5(n);//wt

cout << "enter brust time: \n";
for(i=0; i<n; i++){
  cout << "P" << i << ": ";
 cin >> ar1[i];
 ar4[i]=ar1[i];

}

cout << "enter priority: \n";

for(i=0; i<n; i++){
 cin >> ar2[i];
 ar3[i]=ar2[i];

}

sort(ar2.begin(), ar2.end());
sort(ar4.begin(), ar4.end());


cout << "proc  burst   priority   waiting time   turnaround time" << endl;

for(i=0; i<n; i++){
 for(j=0; j<n; j++){
  if(ar2[i]==ar3[j]){
  ar5[i]=ar1[j];
  wt=wt+ar5[i-1];
  tt = tt + ar1[j];
  cout << "P"<<j<< "    " << ar1[j] << "        " << ar3[j]<< "              " << wt << "            " << tt << endl;
  }
 }
}

return 0;
}
