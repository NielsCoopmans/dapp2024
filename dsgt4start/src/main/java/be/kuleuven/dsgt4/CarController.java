package be.kuleuven.dsgt4;

import be.kuleuven.dsgt4.auth.WebSecurityConfig;
import com.google.cloud.firestore.Firestore;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AuthorizationServiceException;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;


@RestController
@RequestMapping("/api/broker/cars")
public class CarController {

    private final SupplierServiceCar supplierServiceCar;

    public CarController(SupplierServiceCar supplierServiceCar) {
        this.supplierServiceCar = supplierServiceCar;
    }

    @GetMapping
    public Car[] getAllCars(){
        return supplierServiceCar.getAllCars();
    }

    @PostMapping("/{id}/order")
    public ResponseEntity<String> orderCar(@PathVariable UUID id) {
        try{
            supplierServiceCar.orderCar(id);
            return ResponseEntity.ok("Car ordered");
        }catch (Exception e){
            return ResponseEntity.status(500).body("Failed to order car: " + e.getMessage());
        }
    }

    @PostMapping("/{id}/reserve")
    public ResponseEntity<String> reserveCar(@PathVariable UUID id) {
        try {
            supplierServiceCar.reserveCar(id);
            return ResponseEntity.ok("Car reserved successfully");
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Failed to reserve car: " + e.getMessage());
        }
    }

    @PostMapping("/{id}/cancel")
    public ResponseEntity<String> cancelOrder(@PathVariable UUID id) {
        try {
            supplierServiceCar.cancelCar(id);
            return ResponseEntity.ok("Order cancelled successfully");
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Failed to cancel order: " + e.getMessage());
        }
    }

}
