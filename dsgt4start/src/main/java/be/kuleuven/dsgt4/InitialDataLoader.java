package be.kuleuven.dsgt4;

import javax.annotation.PostConstruct;

import com.google.cloud.firestore.Firestore;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

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
            // Create two initial customers
            Customer customer1 = new Customer("Customer 1","customer1@example.com");

            Customer customer2 = new Customer("Customer 2","customer2@example.com");

            // Add customers to Firestore
            db.collection("customers").document().set(customer1).get();
            db.collection("customers").document().set(customer2).get();
        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace(); // Handle the exception appropriately in production
        }
    }

}
