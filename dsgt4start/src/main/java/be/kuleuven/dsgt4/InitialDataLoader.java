package be.kuleuven.dsgt4;

import javax.annotation.PostConstruct;

import com.google.cloud.firestore.Firestore;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.ExecutionException;

@Component
public class InitialDataLoader {

    @Autowired
    Firestore db;

    @PostConstruct
    public void init() {
        // Check if customers exist, if not, add them
        try {
            var query = db.collection("customers").get().get();
            if (query.isEmpty()) {
                addInitialCustomers();
            }
        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace(); // Handle the exception appropriately in production
        }
    }

    private void addInitialCustomers() {
        try {
            Car car1 = new Car( "citroen", "C3", "yellow", 2012,815666.52,"old");
            db.collection("cars").document().set(car1).get();

            // Create two initial customers
            Customer customer1 = new Customer("Customer 1","customer1@example.com");

            Customer customer2 = new Customer("Customer 2","customer2@example.com");

            // Add customers to Firestore
            db.collection("customers").document().set(customer1).get();
            db.collection("customers").document().set(customer2).get();

            // Create items
            Item item1 = new Item("Product 1", 19.99);
            Item item2 = new Item("Product 2", 99.99);
            Item item3 = new Item("Product 3", 99.99);

            // Create orders
            Order order1 = new Order(customer1, List.of(item1, item2, car1));
            Order order2 = new Order(customer2, List.of(item3));

            // Add orders to Firestore
            db.collection("orders").document().set(order1).get();
            db.collection("orders").document().set(order2).get();
        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace(); // Handle the exception appropriately in production
        }
    }


}
