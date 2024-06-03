package be.kuleuven.dsgt4;

import be.kuleuven.dsgt4.auth.WebSecurityConfig;
import com.google.api.core.ApiFuture;
import com.google.cloud.firestore.Firestore;
import com.google.cloud.firestore.QueryDocumentSnapshot;
import com.google.cloud.firestore.QuerySnapshot;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AuthorizationServiceException;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ExecutionException;

@RestController
public class CustomerController {
    @Autowired
    Firestore db;



    @PostMapping("/api/createCustomer")
    public void createOrder(@RequestBody Map<String, Object> customerData) {
        UUID customerId = UUID.randomUUID();
        String emailCustomer = (String) customerData.get("email");
        Customer customer = new Customer(emailCustomer);
        db.collection("customers").document(customerId.toString()).set(customer);
    }

    @PutMapping("/api/updateCustomer/{customerId}")
    public Customer updateCustomer(@PathVariable String customerId, @RequestBody Customer customer) {
        db.collection("customers").document(customerId).set(customer);
        return customer;
    }

    @DeleteMapping("/api/deleteCustomer/{customerId}")
    public void deleteCustomer(@PathVariable String customerId) {
        db.collection("customers").document(customerId).delete();
    }

    @GetMapping("/api/getCustomerById/{customerId}")
    public Customer getCustomerById(@PathVariable String customerId) throws InterruptedException, ExecutionException {
        var doc = db.collection("customers").document(customerId).get().get();
        return doc.toObject(Customer.class);
    }

    @GetMapping("/api/getALLCustomers")
    public @ResponseBody ResponseEntity<?> getALLCustomers() throws InterruptedException, ExecutionException {
        Logger logger = LoggerFactory.getLogger(this.getClass());
        try{
            var user = WebSecurityConfig.getUser();
            if (!user.isManager()) throw new AuthorizationServiceException("You are not a manager");

            Map<String,Customer> customers = new HashMap<>();

            ApiFuture<QuerySnapshot> query = db.collection("customers").get();
            QuerySnapshot querySnapshot = query.get();
            List<QueryDocumentSnapshot> documents = querySnapshot.getDocuments();
            for (QueryDocumentSnapshot document : documents) {
                Customer customer = new Customer(document.getString("email"));
                customers.put(document.getId(), customer);
            }

            return ResponseEntity.ok(customers);
        } catch (InterruptedException | ExecutionException e) {
            logger.error("Error fetching orders", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error fetching orders: " + e.getMessage());
        } catch (AuthorizationServiceException e) {
            logger.error("Authorization error", e);
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Authorization error: " + e.getMessage());
        } catch (Exception e) {
            logger.error("Unexpected error", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Unexpected error: " + e.getMessage());
        }
    }
}
