package be.kuleuven.dsgt4;

import java.util.ArrayList;
import java.util.List;

public class ExhaustSystemSupplier {
    private String supplierName;
    private List<ExhaustSystem> availableExhaustSystems;

    // Constructor for the supplier
    public ExhaustSystemSupplier(String supplierName) {
        this.supplierName = supplierName;
        this.availableExhaustSystems = new ArrayList<>();
    }

    // Method to add a new exhaust system to the inventory
    public void addExhaustSystem(String name, String brand, double price) {
        availableExhaustSystems.add(new ExhaustSystem(name, brand, price));
    }

    // Method to sell an exhaust system by name
    public boolean sellExhaustSystem(String name) {
        for (ExhaustSystem exhaustSystem : availableExhaustSystems) {
            if (exhaustSystem.getName().equalsIgnoreCase(name)) {
                availableExhaustSystems.remove(exhaustSystem);
                System.out.println("Sold " + name + " from brand " + exhaustSystem.getBrand() + " for $" + exhaustSystem.getPrice());
                return true;
            }
        }
        System.out.println("Exhaust system not found: " + name);
        return false;
    }

    // Method to list available exhaust systems
    public void listAvailableExhaustSystems() {
        if (availableExhaustSystems.isEmpty()) {
            System.out.println("No exhaust systems available.");
        } else {
            System.out.println("Available exhaust systems:");
            for (ExhaustSystem exhaustSystem : availableExhaustSystems) {
                System.out.println(exhaustSystem);
            }
        }
    }

    public String getSupplierName() {
        return supplierName;
    }

    @Override
    public String toString() {
        return "ExhaustSystemSupplier: " + supplierName;
    }
}
