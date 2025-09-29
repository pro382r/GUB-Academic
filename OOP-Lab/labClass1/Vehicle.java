
package vehicle;

public class Vehicle {

    public static void main(String[] args) {
        Car mercedes = new Car();
        
        mercedes.name = "Mercedes";
        mercedes.color = "Red";
        mercedes.fuel = 6.5;
        mercedes.maxFuel = 8;
        mercedes.tiresize = (float) 4.5; //mercedes.tiresize = 4.5f;
        
        //mercedes.describe();
        
        mercedes.run(5);
        mercedes.refill(11.1);
        
        mercedes.describe();
        
        
        System.out.println();
        
        
        
        Car tesla = new Car("Tesla", 2.2, 13, "Blue", 5.4f);
        
        tesla.name = "Tesla";
        tesla.color = "Blue";
        tesla.fuel = 2.2;
        tesla.maxFuel = 13;
        tesla.tiresize = 5.4f;
        
        tesla.run(9);
        tesla.refill(14.1);
        
        tesla.describe();
    }
    
}
