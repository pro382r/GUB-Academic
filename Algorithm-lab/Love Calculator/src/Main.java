    // Md. Reahoon Zannah   ID: 223002038
    // Love Calculator App
import java.util.Random;
import java.util.Scanner;

public class Main {
    public static int findSCSLength(String name1, String name2) {
        int m = name1.length();
        int n = name2.length();
        int[][] dp = new int[m + 1][n + 1];

        for (int i = 0; i <= m; i++) {
            for (int j = 0; j <= n; j++) {
                if (i == 0) {
                    dp[i][j] = j;
                } else if (j == 0) {
                    dp[i][j] = i;
                } else if (name1.charAt(i - 1) == name2.charAt(j - 1)) {
                    dp[i][j] = 1 + dp[i - 1][j - 1];
                } else {
                    dp[i][j] = 1 + Math.min(dp[i - 1][j], dp[i][j - 1]);
                }
            }
        }
        return dp[m][n];
    }

    public static int countUniqueSCS(String name1, String name2) {
        int m = name1.length();
        int n = name2.length();
        int[][] dp = new int[m + 1][n + 1];

        for (int i = 0; i <= m; i++) dp[i][0] = 1;
        for (int j = 0; j <= n; j++) dp[0][j] = 1;

        for (int i = 1; i <= m; i++) {
            for (int j = 1; j <= n; j++) {
                if (name1.charAt(i - 1) == name2.charAt(j - 1)) {
                    dp[i][j] = dp[i - 1][j - 1];
                } else {
                    dp[i][j] = dp[i - 1][j] + dp[i][j - 1];
                }
            }
        }
        return dp[m][n];
    }

    public static int generateLoveScore(int scsLength, int uniqueSCSCount) {
        Random random = new Random();
        int randomFactor = random.nextInt(50);
        int loveScore = (scsLength * uniqueSCSCount + randomFactor) % 101;
        return loveScore;
    }

    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        System.out.print("Enter the first name: ");
        String name1 = scanner.nextLine();
        System.out.print("Enter the second name: ");
        String name2 = scanner.nextLine();

        int scsLength = findSCSLength(name1, name2);
        int uniqueSCSCount = countUniqueSCS(name1, name2);
        int loveScore = generateLoveScore(scsLength, uniqueSCSCount);

        System.out.println("Length of Shortest Common Supersequence: " + scsLength);
        System.out.println("Total Number of Unique Shortest SCSs: " + uniqueSCSCount);
        System.out.println("Generated Love Score: " + loveScore + " 💖");

        scanner.close();
    }
}