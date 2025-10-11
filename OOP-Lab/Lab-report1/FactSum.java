import java.util.Scanner;

public class FactSum {
  public static long factorial(int f) {
    return (f <= 1) ? 1 : f * factorial(f - 1);
}

  public static void main(String[] args) {
      System.out.print("Type an EVEN integer: ");
      int i, n, f, x;
      //long r=0;
      double d, sum=0, ex, r=0, a;

      Scanner input = new Scanner(System.in);
      n = input.nextInt();

      System.out.print("Enter the value of X = ");
      x = input.nextInt();

      if(n%2 == 0){
        System.out.println("This is an EVEN number.");
        for(i=2; i<=n; i+=2){
          ex= Math.pow(x,i);
          r = factorial(i-1);
          a = ex/r;
          sum = sum + a;
        }

        System.out.printf("Summation is %.4f\n", sum);
      }else {
        System.out.println("Plz type Even number.");
      }
    
    input.close();
    
  }
}
