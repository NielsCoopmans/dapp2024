package be.kuleuven.dsgt4;

import be.kuleuven.dsgt4.auth.WebSecurityConfig;
import com.fasterxml.jackson.core.type.TypeReference;
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
import com.fasterxml.jackson.databind.ObjectMapper;

import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

@RestController
public class OrderController {
    @Autowired
    Firestore db;

    @Autowired
    private ScheduledExecutorService scheduler;

    private final SupplierServiceExhaust supplierServiceExhaust;
    private final SupplierServiceCar supplierServiceCar;
    private static final AtomicInteger idCounter = new AtomicInteger();
    private static final Logger logger = LoggerFactory.getLogger(OrderController.class);

    public OrderController(SupplierServiceExhaust supplierServiceExhaust,SupplierServiceCar supplierServiceCar) {
        this.supplierServiceExhaust = supplierServiceExhaust;
        this.supplierServiceCar = supplierServiceCar;
    }

    private int generateUniqueId() {
        return idCounter.incrementAndGet();
    }

    @PostMapping("/api/createOrder")
    public ResponseEntity<?> createOrder(@RequestBody Map<String, Object> orderData) {
        try {

            var user = WebSecurityConfig.getUser();
            String customerEmail = user.getEmail();

            ObjectMapper mapper = new ObjectMapper();
            List<Item> items = mapper.convertValue(orderData.get("items"), new TypeReference<List<Item>>() {});

            if (customerEmail == null || items == null) {
                return ResponseEntity.badRequest().body("Missing required fields: customerEmail or items");
            }
            int id = generateUniqueId();
            // Create order object
            Order order = new Order(id, new Customer(customerEmail), items, false, false);

            UUID orderId = UUID.randomUUID();
            // Save order to Firestore (assuming Ansys Cron is configured for this collection)
            db.collection("orders").document(orderId.toString()).set(order);

            List<Car> cars = items.stream()
                    .filter(item -> item instanceof Car)
                    .map(item -> (Car) item)
                    .toList();

            List<Exhaust> exhausts = items.stream()
                    .filter(item -> item instanceof Exhaust)
                    .map(item -> (Exhaust) item)
                    .toList();

            // Check if either list is empty
            if (!cars.isEmpty()) {
                scheduler.scheduleAtFixedRate(new OrderCarsTask(orderId, cars), 0, 10, TimeUnit.MINUTES);
            }
            else {
                db.collection("orders").document(orderId.toString()).update("carsCompleted", true);
            }

            if (!exhausts.isEmpty()) {
                scheduler.scheduleAtFixedRate(new OrderExhaustsTask(orderId, exhausts), 0, 10, TimeUnit.MINUTES);
            }
            else {
                db.collection("orders").document(orderId.toString()).update("exhaustsCompleted", true);
            }

            return ResponseEntity.ok(order);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error creating order: " + e.getMessage());
        }
    }

    private class OrderCarsTask implements Runnable {
        private final UUID orderId;
        private final Car[] cars;

        OrderCarsTask(UUID orderId, List<Car> cars) {
            this.orderId = orderId;
            this.cars = cars.toArray(new Car[0]);
        }

        @Override
        public void run() {
            try {
                boolean success = orderCars(cars);
                if (success) {
                    db.collection("orders").document(orderId.toString()).update("carsCompleted", true);
                    checkAndUpdateOrderCompletion(orderId);
                }
            } catch (Exception e) {
                logger.error("Error ordering cars", e);
            }
        }
    }

    private class OrderExhaustsTask implements Runnable {
        private final UUID orderId;
        private final Exhaust[] exhausts;

        OrderExhaustsTask(UUID orderId, List<Exhaust> exhausts) {
            this.orderId = orderId;
            this.exhausts = exhausts.toArray(new Exhaust[0]);
        }

        @Override
        public void run() {
            try {
                boolean success = orderExhaust(exhausts);
                if (success) {
                    db.collection("orders").document(orderId.toString()).update("exhaustsCompleted", true);
                    checkAndUpdateOrderCompletion(orderId);
                }
            } catch (Exception e) {
                logger.error("Error ordering exhausts", e);
            }
        }
    }


    private void checkAndUpdateOrderCompletion(UUID orderId) throws ExecutionException, InterruptedException {
        var doc = db.collection("orders").document(orderId.toString()).get().get();
        Boolean carsCompleted = doc.getBoolean("carsCompleted");
        Boolean exhaustsCompleted = doc.getBoolean("exhaustsCompleted");

        if (Boolean.TRUE.equals(carsCompleted) && Boolean.TRUE.equals(exhaustsCompleted)) {
            db.collection("orders").document(orderId.toString()).update("completed", true);
        }
    }

    public boolean checkOrderCars(int orderId){
        return false;
    }

    public boolean orderCars(Car[] cars) {
        return false;
    }

    public boolean orderExhaust(Exhaust[] exhausts) {
        return supplierServiceExhaust.orderExhaust(exhausts);
    }

    public boolean checkOrderExhaust(int orderId){
        return supplierServiceExhaust.checkOrder(orderId);
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
                Boolean completed = document.getBoolean("carsCompleted");
                Boolean exhaustCompleted = document.getBoolean("exhaustCompleted");
                int id = document.get("id",Integer.TYPE);
                Order order = new Order(id,customer, items, Boolean.TRUE.equals(completed), Boolean.TRUE.equals(exhaustCompleted));
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
