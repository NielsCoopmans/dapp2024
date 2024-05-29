package be.kuleuven.dsgt4;

import be.kuleuven.dsgt4.auth.WebSecurityConfig;
import com.fasterxml.jackson.databind.deser.std.UUIDDeserializer;
import com.google.api.core.ApiFuture;
import com.google.cloud.firestore.CollectionReference;
import com.google.cloud.firestore.Firestore;
import com.google.cloud.firestore.QueryDocumentSnapshot;
import com.google.cloud.firestore.QuerySnapshot;
import com.google.common.reflect.TypeToken;
import com.google.gson.Gson;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AuthorizationServiceException;
import org.springframework.web.bind.annotation.*;

import java.lang.reflect.Type;
import java.time.LocalDateTime;
import java.util.*;
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
                Customer customer = new Customer(document.getString("email"),document.getString("name"));
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

    @GetMapping("/api/getAllOrders")
    public ResponseEntity<?> getAllOrders() {
        Logger logger = LoggerFactory.getLogger(this.getClass());
        try {
            // Check user authorization
            var user = WebSecurityConfig.getUser();
            if (!user.isManager()) {
                throw new AuthorizationServiceException("You are not a manager");
            }

            Map<String, Order> orders = new HashMap();
            ApiFuture<QuerySnapshot> query = db.collection("orders").get();
            QuerySnapshot querySnapshot = query.get();
            List<QueryDocumentSnapshot> documents = querySnapshot.getDocuments();

            Gson gson = new Gson();
            Type customerType = new TypeToken<Customer>() {}.getType();
            Type itemListType = new TypeToken<List<Item>>() {}.getType();

            for (QueryDocumentSnapshot document : documents) {
                Customer customer = gson.fromJson(gson.toJson(document.get("customer")),customerType);
                List<Item> items = gson.fromJson(gson.toJson(document.get("items")), itemListType);
                Order order = new Order(customer, items);
                orders.put(document.getId(),order);
            }

            return ResponseEntity.ok(orders);
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
