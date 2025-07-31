// incomplete

#include <bits/stdc++.h>
using namespace std;

int main() {
    /*int T;
    cin >> T;
    while (T--) {*/
        int bn, i, j;

        cout << "Enter the number of block: ";
        cin >> bn;

        vector<int>blc(bn);

        for(i = 0; i < bn; i++){
            cout << "Block " << i+1 << " size: " ;
            cin >> blc[i];
        }
