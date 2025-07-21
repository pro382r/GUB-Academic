
#include<bits/stdc++.h>
using namespace std;

int main(){

int i, j=0, k=0, n, s=0;
int bs = 300;
cin >> n;

vector<int> ar1(n);

for(i=0; i<n; i++){
  cin >> ar1[i];
}

for(i=0; i<n; i++){
  if(ar1[i] <= bs){
    j = bs - ar1[i];
    cout << "Yes    " << j << endl;
    s = s + j;
    k++;
  }else {
    cout << "No     ---\n";
  }
}

cout << "Total internal Fragment size : " << s << endl;
cout << "Number of using block = " << k << endl;

return 0;
}

