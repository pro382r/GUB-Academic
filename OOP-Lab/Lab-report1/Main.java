import java.util.Scanner;

public class Main {
  public static void main(String[] args) {
      System.out.print("Type an integer: ");
      int a;

      Scanner input = new Scanner(System.in);

      a = input.nextInt();

      if(a%2 == 0){
        System.out.println("This is an EVEN number.");
      }else System.out.println("This is an ODD number.");
    
    input.close();
  }
}
