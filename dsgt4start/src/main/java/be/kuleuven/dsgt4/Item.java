package be.kuleuven.dsgt4;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

import java.util.Objects;

public class Item {

    private String name;
    private double price;
    private String brand;

    public Item(String name, double price, String brand) {
        this.name = name;
        this.price = price;
        this.brand = brand;
    }

    public Item() {

    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public double getPrice() {
        return price;
    }

    public void setPrice(double price) {
        this.price = price;
    }

    public String getBrand() {
        return brand;
    }

    public void setBrand(String brand) {
        this.brand = brand;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Item item = (Item) o;
        return Double.compare(price, item.price) == 0 && Objects.equals(name, item.name) && Objects.equals(brand, item.brand);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, price, brand);
    }

    @Override
    public String toString() {
        return "Item{" +
                ", productName='" + name + '\'' +
                ", price=" + price +
                '}';
    }

}
