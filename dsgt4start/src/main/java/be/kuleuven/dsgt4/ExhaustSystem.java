package be.kuleuven.dsgt4;

public class ExhaustSystem {
    private String name;
    private String brand;
    private double price;

    // Constructor for an exhaust system
    public ExhaustSystem(String name, String brand, double price) {
        this.name = name;
        this.brand = brand;
        this.price = price;
    }

    // Getters for each attribute
    public String getName() {
        return name;
    }

    public String getBrand() {
        return brand;
    }

    public double getPrice() {
        return price;
    }

    @Override
    public String toString() {
        return "Name: " + name + ", Brand: " + brand + ", Price: $" + price;
    }
}
