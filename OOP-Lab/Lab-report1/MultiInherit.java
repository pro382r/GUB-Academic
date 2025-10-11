class A {
    protected String messageA = "Hello from Class A";
    
    public void displayA() {
        System.out.println(messageA);
    }
    
    public void commonMethod() {
        System.out.println("Common method from Class A");
    }
}

class B {
    protected String messageB = "Hello from Class B";
    
    public void displayB() {
        System.out.println(messageB);
    }
    
    public void commonMethod() {
        System.out.println("Common method from Class B");
    }
}

class C extends A {
    private B bObj;

    public C() {
        bObj = new B();
    }

    public void displayC() {
        System.out.println("Hello from Class C");
    }

    public void displayFromB() {
        bObj.displayB();
    }

    public void commonMethodFromB() {
        bObj.commonMethod();
    }

    public void demonstrateInheritance() {
        System.out.println("\n=== Demonstrating Multiple Inheritance ===");
        displayA();           // From A
        displayFromB();       // From B
        displayC();           // Own method
        commonMethod();       // From A
        commonMethodFromB();  // From B
    }
}

public class MultiInherit {
    public static void main(String[] args) {
        C obj = new C();
        obj.demonstrateInheritance();
    }
}
