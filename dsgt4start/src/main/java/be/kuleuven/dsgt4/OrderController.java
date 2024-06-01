package be.kuleuven.dsgt4;

import be.kuleuven.dsgt4.auth.WebSecurityConfig;
import com.google.api.core.ApiFuture;
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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ExecutionException;

@RestController
public class OrderController {
    @Autowired
    Firestore db;

    @PostMapping("/api/createOrder")
    public ResponseEntity<?> createOrder(@RequestBody Map<String, Object> orderData) {
        try {

            var user = WebSecurityConfig.getUser();
            String customerEmail = user.getEmail();
            List<Item> items = (List<Item>) orderData.get("items");

            if (customerEmail == null || items == null) {
                return ResponseEntity.badRequest().body("Missing required fields: customerEmail or items");
            }

            // Create order object
            Order order = new Order(new Customer(customerEmail),items);

            UUID orderId = UUID.randomUUID();
            db.collection("orders").document(orderId.toString()).set(order);

            return ResponseEntity.ok(order);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error creating order: " + e.getMessage());
        }
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
}
