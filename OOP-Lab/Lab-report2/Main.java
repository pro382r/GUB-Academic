import java.util.Scanner;

public class Main {
    public static void main(String[] args) {
        Scanner sc = new Scanner(System.in);
        System.out.print("Enter Rect Length & Breadth: ");
        print(new Rectangle(sc.nextDouble(), sc.nextDouble()));

        System.out.print("Enter Square Side: ");
        print(new Square(sc.nextDouble()));
    }

    static void print(Shape s) {
        System.out.println("\n=== " + s.getClass().getSimpleName() + " ===");
        System.out.println("Area: " + s.area() + "\nPerimeter: " + s.peri());
    }
}

abstract class Shape {
    double l, b;
    Shape(double l, double b) { this.l = l; this.b = b; }
    double area() { return l * b; }
    double peri() { return 2 * (l + b); }
}

class Rectangle extends Shape {
    Rectangle(double l, double b) { super(l, b); }
}

class Square extends Shape {
    Square(double s) { super(s, s); }
}
