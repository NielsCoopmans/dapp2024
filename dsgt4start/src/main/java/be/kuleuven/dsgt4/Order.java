package be.kuleuven.dsgt4;

import java.util.List;
import java.util.UUID;

public class Order {
    private UUID id;
    private Customer customer;
    private List items;

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public Order(Customer customer, List items){
        this.customer = customer;
        this.items = items;
        this.id = UUID.randomUUID();
    }

    public List getItems() {
        return items;
    }

    public void setItems(List items) {
        this.items = items;
    }

    public Customer getCustomer() {
        return customer;
    }

    public void setCustomer(Customer customer) {
        this.customer = customer;
    }
}
