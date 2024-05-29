package be.kuleuven.dsgt4;

import com.google.cloud.firestore.Firestore;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/broker/cars")
public class CarController {
    @Autowired
    Firestore db;

    private final SupplierService supplierService;

    public CarController(SupplierService supplierService) {
        this.supplierService = supplierService;
    }

    @GetMapping
    public Car[] getAllCars() {
        return supplierService.getAllCars();
    }
}
