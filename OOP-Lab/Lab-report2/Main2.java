import java.util.*;

interface IsEmergency { 
    void soundSiren();
}

class FireEmergency implements IsEmergency {
    public void soundSiren() { 
        System.out.println("Siren Sounded");
    }
}

class SmokeAlarm {
    String loc; boolean active;
    SmokeAlarm(String l) { loc = l; }
    void show() { 
        System.out.println((active ? "Smoke detected at " : "No smoke at ") + loc);
    }
}

public class Main2 {
    public static void main(String[] args) {
        var sc = new Scanner(System.in);
        System.out.print("Devices: ");
        var devices = new Object[sc.nextInt()];

        for (int i = 0; i < devices.length; i++) {
            System.out.print("Type (1=Smoke, 2=Fire): ");
            if (sc.nextInt() == 2) devices[i] = new FireEmergency();
            else {
                System.out.print("Location: ");
                devices[i] = new SmokeAlarm(sc.next());
            }
        }

        System.out.println("\nTrigger Check:");
        for (var d : devices) 
            if (d instanceof SmokeAlarm s) {
                System.out.print("Trigger at " + s.loc + "? (y/n): ");
                if (sc.next().startsWith("y")) s.active = true;
            }

        System.out.println("\n--- Status ---");
        for (var d : devices) {
            if (d instanceof IsEmergency e) e.soundSiren();
            if (d instanceof SmokeAlarm s) s.show();
        }
    }
}
