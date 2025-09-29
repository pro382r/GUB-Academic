
package vehicle;

public class Car {
    String name, color;
    double fuel, maxFuel;
    float tiresize;
    
    //this is a constrctor........
    public Car(String name, double fuel, double maxFuel, String color, float tiersize){
        System.out.println("Car is creating...");
        
        this.name = name;
        this.color = color;
        this.fuel = fuel;
        this.maxFuel = maxFuel;
        this.tiresize = tiresize;
    

    }
    
    void describe(){
        System.out.println("###########################");
        System.out.println("Name: "+name);
        System.out.println("Color: "+color);
        System.out.println("Fuel: "+fuel);
        System.out.println("Tier size: "+tiresize);
        System.out.println("==========================");
    }
    

    void refill(double quantity){
        if(fuel+quantity>maxFuel){
            fuel = maxFuel;
        }else{
            fuel+=quantity;
        }
    }
    
    void run(double km){
        if(fuel==0) return;
        fuel-=km;
    }
    
    
}
