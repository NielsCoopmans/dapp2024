package be.kuleuven.dsgt4;

import java.util.List;
import java.util.UUID;

public class Order {
    private Customer customer;
    private List<Item> items;


    public Order(Customer customer, List<Item> items){
        this.customer = customer;
        this.items = items;
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
