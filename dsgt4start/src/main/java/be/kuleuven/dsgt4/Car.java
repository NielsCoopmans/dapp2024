package be.kuleuven.dsgt4;

import com.fasterxml.jackson.databind.deser.std.UUIDDeserializer;

import java.util.Objects;
import java.util.UUID;

public class Car extends Item{

    private UUID id;
    private String name;
    private String brand;
    private String model;
    private String color;
    private int year;
    private double price;
    private String description;
    private Status status;

    public Car() {
        super();
    }

    public Car(UUID id, String brand, String model, String color, int year, double price, String name) {
        super(name,price,brand);
        this.id = id;
        this.name = name;
        this.brand = brand;
        this.model = model;
        this.color = color;
        this.year = year;
        this.price = price;
        this.description = name;
        this.status = Status.AVAILABLE;
    }


    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public String getBrand() {
        return brand;
    }

    public void setBrand(String brand) {
        this.brand = brand;
    }

    public String getModel() {
        return model;
    }

    public void setModel(String model) {
        this.model = model;
    }

    public String getColor() {
        return color;
    }

    public void setColor(String color) {
        this.color = color;
    }

    public int getYear() {
        return year;
    }

    public void setYear(int year) {
        this.year = year;
    }

    public double getPrice() {
        return price;
    }

    public void setPrice(double price) {
        this.price = price;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
        this.name = description;
    }

    public Status getStatus() {
        return status;
    }

    public void setStatus(Status status) {
        this.status = status;
    }

    @Override
    public String toString() {
        return "Car{" +
                ", brand='" + brand + '\'' +
                ", model='" + model + '\'' +
                ", color='" + color + '\'' +
                ", year=" + year +
                ", price=" + price +
                ", description='" + description + '\'' +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Car car = (Car) o;
        return Objects.equals(brand, car.brand) &&
                Objects.equals(model, car.model) &&
                Objects.equals(color, car.color) &&
                Objects.equals(year, car.year) &&
                Objects.equals(price, car.price) &&
                Objects.equals(description, car.description);
    }

    @Override
    public int hashCode() {
        return Objects.hash( brand, model, color, year, price, description);
    }
}
