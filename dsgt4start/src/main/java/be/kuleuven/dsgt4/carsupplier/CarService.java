package be.kuleuven.dsgt4.carsupplier;

import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
public class CarService {
    private List<Car> cars = new ArrayList<>();

    public CarService() {
        cars.add(new Car(UUID.randomUUID(),"Audi", "A4", "Black", 2019, 30000, "Audi A4 Black 2019"));
        cars.add(new Car(UUID.randomUUID(),"BMW", "X5", "White", 2020, 50000, "BMW X5 White 2020"));
        cars.add(new Car(UUID.randomUUID(),"Mercedes", "C-Class", "Silver", 2018, 35000, "Mercedes C-Class Silver 2018"));
    }

    public List<Car> getAllCars() {
        return cars;
    }

    public Car getCarById(UUID id) {
        return cars.stream().filter(car -> car.getId().equals(id)).findFirst().orElse(null);
    }

    public OrderResponse orderCar(CarOrderRequest carOrderRequest){
        Car car = getCarById(carOrderRequest.getCarId());
        if(car == null){
            return new OrderResponse("Car not found", "", carOrderRequest.getCustomerName(), carOrderRequest.getAddress());
        }
        boolean succes = processOrder(car);
        return new OrderResponse(succes ? "Order placed successfully" : "Order failed", car.getDescription(), carOrderRequest.getCustomerName(), carOrderRequest.getAddress());
    }

    private boolean processOrder(Car car){
        // Process the order
        for(Car c : cars){
            if(c.getId().equals(car.getId())) {
                //cars.remove(c);
                return true;
            }
        }
        return false;
    }

}
