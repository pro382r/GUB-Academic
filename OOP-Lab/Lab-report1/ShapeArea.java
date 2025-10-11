import java.util.Scanner;

public class ShapeArea {
    double area;
    ShapeArea(double base, double height) {
        area = 0.5 * base * height;
    }

    ShapeArea(double length, double breadth, boolean isRectangle) {
        area = length * breadth;
    }

    ShapeArea(double radius) {
        area = Math.PI * radius * radius;
    }

    public double getArea() {
        return area;
    }

    public static void main(String[] args) {
        Scanner sc = new Scanner(System.in);

        System.out.println("Choose shape to calculate area:");
        System.out.println("1. Triangle");
        System.out.println("2. Rectangle");
        System.out.println("3. Circle");
        System.out.print("Enter your choice (1-3): ");
        int choice = sc.nextInt();

        ShapeArea shape = null;

        switch (choice) {
            case 1:
                // Triangle
                System.out.print("Enter base of triangle: ");
                double base = sc.nextDouble();
                System.out.print("Enter height of triangle: ");
                double height = sc.nextDouble();

                shape = new ShapeArea(base, height);
                System.out.println("Area of Triangle: " + shape.getArea());
                break;

            case 2:
                // Rectangle
                System.out.print("Enter length of rectangle: ");
                double length = sc.nextDouble();
                System.out.print("Enter breadth of rectangle: ");
                double breadth = sc.nextDouble();

                // To differentiate this constructor, we pass a boolean true as 3rd param
                shape = new ShapeArea(length, breadth, true);
                System.out.println("Area of Rectangle: " + shape.getArea());
                break;

            case 3:
                // Circle
                System.out.print("Enter radius of circle: ");
                double radius = sc.nextDouble();

                shape = new ShapeArea(radius);
                System.out.println("Area of Circle: " + shape.getArea());
                break;

            default:
                System.out.println("Invalid choice!");
        }

        sc.close();
    }
}
