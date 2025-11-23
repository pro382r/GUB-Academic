import java.util.Scanner;

public class Jp1 {
    private static int getInput() {
        Scanner inp = new Scanner(System.in);

        int n = 0;
        try {
            n = inp.nextInt();
        } catch (Exception e) {
            System.out.println("Invalid input.");
            return getInput();
        }
        return n;
    }
    public static void main(String[] args) {
        int n = getInput();
        int i, j, k = 0;

        for (i = 2; i <= n / 2; i++) {
            if (n % i == 0) {
                k++;
                break;
            }
        }
        if (k > 0) {
            System.out.println("This is not prime.");
        } else if (k == 0) {
            System.out.println("This is prime.");
        }
    }
}
