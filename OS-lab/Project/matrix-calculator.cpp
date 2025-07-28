#include <iostream>
#include <vector>
#include <iomanip>
#include <cstdlib>
#include <ctime>
#include <fstream>

using namespace std;

void formatNumber(double num) {
    if (num == static_cast<int>(num)) {
        cout << setw(5) << static_cast<int>(num);
    } else {
        cout << setw(7) << fixed << setprecision(2) << num;
    }
}

void printMatrix(int rows, int cols, const string& name, const vector<double>& matrix) {
    cout << name << " (" << rows << " x " << cols << "):" << endl;
    for (int i = 0; i < rows; i++) {
        for (int j = 0; j < cols; j++) {
            int idx = i * cols + j;
            formatNumber(matrix[idx]);
            cout << " ";
        }
        cout << endl;
    }
    cout << endl;
}

void writeMatrixToFile(int rows, int cols, const string& name, const vector<double>& matrix) {
    ofstream outFile("matrix_solution.txt", ios::app);
    outFile << name << " (" << rows << " x " << cols << "):" << endl;
    for (int i = 0; i < rows; i++) {
        for (int j = 0; j < cols; j++) {
            int idx = i * cols + j;
            outFile << fixed << setprecision(2) << matrix[idx] << " ";
        }
        outFile << endl;
    }
    outFile << endl;
    outFile.close();
}

double determinant2x2(double a, double b, double c, double d) {
    return a * d - b * c;
}

void inverse2x2(double a, double b, double c, double d) {
    double det = determinant2x2(a, b, c, d);
    if (det == 0) {
        cout << "Matrix is not invertible (det=0)" << endl;
        return;
    }
    vector<double> inv = { d / det, -b / det, -c / det, a / det };
    cout << "Inverse of Matrix A:" << endl;
    printMatrix(2, 2, "Inverse Matrix", inv);
    writeMatrixToFile(2, 2, "Inverse Matrix", inv);
}

double determinant3x3(const vector<double>& m) {
    return m[0] * (m[4] * m[8] - m[5] * m[7]) -
           m[1] * (m[3] * m[8] - m[5] * m[6]) +
           m[2] * (m[3] * m[7] - m[4] * m[6]);
}

void inverse3x3(const vector<double>& m) {
    double det = determinant3x3(m);
    if (det == 0) {
        cout << "Matrix is not invertible (det=0)" << endl;
        return;
    }
    vector<double> cofactor = {
        m[4] * m[8] - m[5] * m[7], - (m[3] * m[8] - m[5] * m[6]), m[3] * m[7] - m[4] * m[6],
        - (m[1] * m[8] - m[2] * m[7]), m[0] * m[8] - m[2] * m[6], - (m[0] * m[7] - m[1] * m[6]),
        m[1] * m[5] - m[2] * m[4], - (m[0] * m[5] - m[2] * m[3]), m[0] * m[4] - m[1] * m[3]
    };
    
    vector<double> inverse(9);
    for (int i = 0; i < 9; i++) {
        inverse[i] = cofactor[i] / det;
    }
    
    cout << "Inverse of Matrix A:" << endl;
    printMatrix(3, 3, "Inverse Matrix", inverse);
    writeMatrixToFile(3, 3, "Inverse Matrix", inverse);
}

int main() {
    srand(static_cast<unsigned int>(time(0)));
    while (true) {
        cout << "========================================" << endl;
        cout << "          C++ Matrix Calculator" << endl;
        cout << "========================================" << endl;
        cout << "1. Start New Calculation" << endl;
        cout << "2. Exit" << endl;
        int mainMenuOption;
        cin >> mainMenuOption;

        if (mainMenuOption == 2) {
            cout << "Exiting program." << endl;
            break;
        }

        int rowsA, colsA, rowsB, colsB;
        cout << "--- Matrix A Dimensions ---" << endl;
        cout << "Enter number of rows for Matrix A: ";
        cin >> rowsA;
        cout << "Enter number of columns for Matrix A: ";
        cin >> colsA;

        cout << "--- Matrix B Dimensions ---" << endl;
        cout << "Enter number of rows for Matrix B: ";
        cin >> rowsB;
        cout << "Enter number of columns for Matrix B: ";
        cin >> colsB;

        cout << "1. Auto Generate Random Values" << endl;
        cout << "2. Manually Input Values" << endl;
        cout << "3. Exit Program" << endl;
        int fillOption;
        cin >> fillOption;
        if (fillOption == 3) {
            cout << "Exiting program." << endl;
            break;
        }

        vector<double> matrixA(rowsA * colsA);
        vector<double> matrixB(rowsB * colsB);

        if (fillOption == 1) {
            for (int i = 0; i < rowsA * colsA; i++) {
                matrixA[i] = rand() % 10;
            }
            for (int i = 0; i < rowsB * colsB; i++) {
                matrixB[i] = rand() % 10;
            }
        } else {
            cout << "Enter values for Matrix A:" << endl;
            for (int i = 0; i < rowsA; i++) {
                for (int j = 0; j < colsA; j++) {
                    cout << "Matrix A [" << i << "][" << j << "]: ";
                    cin >> matrixA[i * colsA + j];
                }
            }

            cout << "Enter values for Matrix B:" << endl;
            for (int i = 0; i < rowsB; i++) {
                for (int j = 0; j < colsB; j++) {
                    cout << "Matrix B [" << i << "][" << j << "]: ";
                    cin >> matrixB[i * colsB + j];
                }
            }
        }

        printMatrix(rowsA, colsA, "Matrix A", matrixA);
        printMatrix(rowsB, colsB, "Matrix B", matrixB);
        writeMatrixToFile(rowsA, colsA, "Matrix A", matrixA);
        writeMatrixToFile(rowsB, colsB, "Matrix B", matrixB);

        cout << "1. Addition" << endl;
        cout << "2. Subtraction" << endl;
        cout << "3. Multiplication" << endl;
        cout << "4. Inverse of Matrix A (2x2 or 3x3)" << endl;
        cout << "5. Exit Program" << endl;
        int op;
        cin >> op;

        if (op == 5) {
            cout << "Exiting program." << endl;
            break;
        }

        vector<double> resultMatrix;
        switch (op) {
            case 1: // Addition
            case 2: // Subtraction
                if (rowsA != rowsB || colsA != colsB) {
                    cout << "Error: Dimensions must match for addition/subtraction." << endl;
                    continue;
                }
                resultMatrix.resize(rowsA * colsA);
                for (int i = 0; i < rowsA * colsA; i++) {
                    resultMatrix[i] = (op == 1) ? (matrixA[i] + matrixB[i]) : (matrixA[i] - matrixB[i]);
                }
                printMatrix(rowsA, colsA, "Result Matrix", resultMatrix);
                writeMatrixToFile(rowsA, colsA, "Result Matrix", resultMatrix);
                break;
            case 3: // Multiplication
                if (colsA != rowsB) {
                    cout << "Error: Matrix A columns must equal Matrix B rows for multiplication." << endl;
                    continue;
                }
                resultMatrix.resize(rowsA * colsB);
                for (int i = 0; i < rowsA; i++) {
                    for (int j = 0; j < colsB; j++) {
                        double sum = 0;
                        for (int k = 0; k < colsA; k++) {
                            sum += matrixA[i * colsA + k] * matrixB[k * colsB + j];
                        }
                        resultMatrix[i * colsB + j] = sum;
                    }
                }
                printMatrix(rowsA, colsB, "Result Matrix", resultMatrix);
                writeMatrixToFile(rowsA, colsB, "Result Matrix", resultMatrix);
                break;
            case 4: // Inverse
                if (rowsA == 2 && colsA == 2) {
                    inverse2x2(matrixA[0], matrixA[1], matrixA[2], matrixA[3]);
                } else if (rowsA == 3 && colsA == 3) {
                    inverse3x3(matrixA);
                } else {
                    cout << "Only 2x2 or 3x3 matrices supported for inversion." << endl;
                }
                break;
        }
    }
    return 0;
}
