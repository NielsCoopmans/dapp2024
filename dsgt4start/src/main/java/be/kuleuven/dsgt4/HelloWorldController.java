package be.kuleuven.dsgt4;

import be.kuleuven.dsgt4.auth.WebSecurityConfig;
import com.google.cloud.firestore.Firestore;
import com.google.cloud.firestore.QueryDocumentSnapshot;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.AuthorizationServiceException;
import org.springframework.web.bind.annotation.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ExecutionException;

// Add the controller.
@RestController
class HelloWorldController {

    @Autowired
    Firestore db;

    @GetMapping("/api/hello")
    public String hello() {
        System.out.println("Inside hello");
        return "hello world!";
    }

    @PostMapping("/api/createOrder")
    public Order createOrder(@RequestBody Order order) {
        UUID orderId = UUID.randomUUID();
        order.setId(orderId); // Set UUID for the order
        db.collection("orders").document(orderId.toString()).set(order);
        return order;
    }

    @PutMapping("/api/updateOrder/{orderId}")
    public Order updateOrder(@PathVariable String orderId, @RequestBody Order order) {
        db.collection("orders").document(orderId).set(order);
        return order;
    }

    @DeleteMapping("/api/deleteOrder/{orderId}")
    public void deleteOrder(@PathVariable String orderId) {
        db.collection("orders").document(orderId).delete();
    }

    @GetMapping("/api/getOrderById/{orderId}")
    public Order getOrderById(@PathVariable String orderId) throws InterruptedException, ExecutionException {
        var doc = db.collection("orders").document(orderId).get().get();
        return doc.toObject(Order.class);
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
    public @ResponseBody List<Customer> getALLCustomers() throws InterruptedException, ExecutionException {
        var user = WebSecurityConfig.getUser();
        if (!user.isManager()) throw new AuthorizationServiceException("You are not a manager");

        List<Customer> customers = new ArrayList<>();

        var query = db.collection("customers").get().get();

        for (QueryDocumentSnapshot document : query.getDocuments()) {
            customers.add(document.toObject(Customer.class));
        }

        return customers;
    }

    @GetMapping("/api/getAllOrders")
    public @ResponseBody List<Order> getAllOrders() throws InterruptedException, ExecutionException {
        var user = WebSecurityConfig.getUser();
        if (!user.isManager()) throw new AuthorizationServiceException("You are not a manager");

        List<Order> orders = new ArrayList<>();

        var query = db.collection("orders").get().get();

        for (QueryDocumentSnapshot document : query.getDocuments()) {
            orders.add(document.toObject(Order.class));
        }

        return orders;
    }

    @GetMapping("/api/whoami")
    public User whoami() throws InterruptedException, ExecutionException {
        var user = WebSecurityConfig.getUser();
        if (!user.isManager()) throw new AuthorizationServiceException("You are not a manager");

        UUID buuid = UUID.randomUUID();
        UserMessage b = new UserMessage(buuid, LocalDateTime.now(), user.getRole(), user.getEmail());
        this.db.collection("usermessages").document(b.getId().toString()).set(b.toDoc()).get();

        return user;
    }
}
