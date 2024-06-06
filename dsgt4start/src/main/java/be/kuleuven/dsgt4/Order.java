package be.kuleuven.dsgt4;

import java.util.List;
import java.util.UUID;

public class Order {
    private int id;
    private Customer customer;
    private List<Item> items;
    private boolean carsCompleted;
    private boolean exhaustsCompleted;


    public Order(int id, Customer customer, List<Item> items, boolean carsCompleted, boolean exhaustsCompleted){
        this.id = id;
        this.customer = customer;
        this.items = items;
        this.carsCompleted = carsCompleted;
        this.exhaustsCompleted = exhaustsCompleted;
    }

    public boolean isExhaustsCompleted() {
        return exhaustsCompleted;
    }

    public void setExhaustsCompleted(boolean exhaustsCompleted) {
        this.exhaustsCompleted = exhaustsCompleted;
    }

    public boolean isCarsCompleted() {
        return carsCompleted;
    }

    public void setCarsCompleted(boolean carsCompleted) {
        this.carsCompleted = carsCompleted;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public List<Item> getItems() {
        return items;
    }

    public void setItems(List<Item> items) {
        this.items = items;
    }

    public Customer getCustomer() {
        return customer;
    }

    public void setCustomer(Customer customer) {
        this.customer = customer;
    }
}
