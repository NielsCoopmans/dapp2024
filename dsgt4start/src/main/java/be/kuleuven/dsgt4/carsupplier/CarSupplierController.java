package be.kuleuven.dsgt4.carsupplier;

import org.springframework.web.bind.annotation.*;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/cars")
public class CarSupplierController {
    @Autowired
    private CarService carService;

    @GetMapping
    public List<Car> getAllCars() {
        return carService.getAllCars();
    }

    @GetMapping("/{id}")
    public Car getCarById(@PathVariable UUID id) {
        return carService.getCarById(id);
    }

    @PostMapping("/order")
    public OrderResponse orderCar(@RequestBody CarOrderRequest orderRequest) {
        return carService.orderCar(orderRequest);
    }
}
