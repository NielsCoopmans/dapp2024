package be.kuleuven.dsgt4;

import com.google.cloud.firestore.Firestore;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;


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

}
