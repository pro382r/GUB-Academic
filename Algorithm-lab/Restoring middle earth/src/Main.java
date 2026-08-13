    // Md.Reahoon Zannah    ID: 223002038
    // Restoring Middle Earth

import java.util.PriorityQueue;
import java.util.Scanner;

public class Main {
    public static int minimizeGroupingCost(int totalPopulation, int n, int k) {

        PriorityQueue<Integer> groupQueue = new PriorityQueue<>();
        groupQueue.add(totalPopulation);
        int totalCost = 0;

        while (groupQueue.size() < n) {
            int currentGroup = groupQueue.poll();
            int splitCount = Math.min(k, n - groupQueue.size());

            int groupSize = currentGroup / splitCount;
            int remainder = currentGroup % splitCount;

            for (int i = 0; i < splitCount; i++) {
                if (i < remainder) {
                    groupQueue.add(groupSize + 1);
                } else {
                    groupQueue.add(groupSize);
                }
            }
            totalCost += currentGroup;
        }
        return totalCost;
    }

    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);

        System.out.print("Enter total population of refugees: ");
        int totalPopulation = scanner.nextInt();

        System.out.print("Enter number of counties (n): ");
        int n = scanner.nextInt();

        System.out.print("Enter maximum groups to split in each step (k): ");
        int k = scanner.nextInt();

        int minCost = minimizeGroupingCost(totalPopulation, n, k);

        System.out.println("Minimum cost to group refugees: " + minCost);
        scanner.close();
    }
}