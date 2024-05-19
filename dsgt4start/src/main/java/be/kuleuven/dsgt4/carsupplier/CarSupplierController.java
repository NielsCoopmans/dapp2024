package be.kuleuven.dsgt4.carsupplier;

import org.springframework.hateoas.CollectionModel;
import org.springframework.hateoas.EntityModel;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.*;
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
    public CollectionModel<EntityModel<Car>> getAllCars() {
        List<EntityModel<Car>> cars = carService.getAllCars().stream()
                .map(car -> EntityModel.of(car,
                        linkTo(methodOn(CarSupplierController.class).getCarById(car.getId())).withSelfRel(),
                        linkTo(methodOn(CarSupplierController.class).getAllCars()).withRel("cars")))
                .toList();
        return CollectionModel.of(cars, linkTo(methodOn(CarSupplierController.class).getAllCars()).withSelfRel());

    }

    @GetMapping("/{id}")
    public EntityModel<Car> getCarById(@PathVariable UUID id) {
        Car car = carService.getCarById(id);
        if (car == null) {
            return EntityModel.of(null, linkTo(methodOn(CarSupplierController.class).getAllCars()).withRel("cars"));
        }
        return EntityModel.of(car,
                linkTo(methodOn(CarSupplierController.class).getCarById(id)).withSelfRel(),
                linkTo(methodOn(CarSupplierController.class).getAllCars()).withRel("cars"));
    }

    @PostMapping("/order")
    public EntityModel<OrderResponse> orderCar(@RequestBody CarOrderRequest orderRequest) {
        OrderResponse response = carService.orderCar(orderRequest);
        return EntityModel.of(response,
                linkTo(methodOn(CarSupplierController.class).orderCar(orderRequest)).withSelfRel(),
                linkTo(methodOn(CarSupplierController.class).getAllCars()).withRel("cars"));
    }
}
