package be.kuleuven.dsgt4;

public class Exhaust extends Item{
    int id;
    String name;
    double price;
    String brand;
    int stock;

    // Default constructor
    public Exhaust() {
        super();
    }

    public Exhaust(int id, String name, double price, String brand, int stock) {
        super(name, price,brand);
        this.id = id;
        this.name = name;
        this.price = price;
        this.brand = brand;
        this.stock = stock;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Override
    public double getPrice() {
        return price;
    }

    public void setPrice(Double price) {
        this.price = price;
    }

    public String getBrand() {
        return brand;
    }

    public void setBrand(String brand) {
        this.brand = brand;
    }

    public int getStock() {
        return stock;
    }

    public void setStock(int stock) {
        this.stock = stock;
    }
}
