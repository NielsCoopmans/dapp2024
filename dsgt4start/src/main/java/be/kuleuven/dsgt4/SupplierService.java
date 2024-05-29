package be.kuleuven.dsgt4;

import com.google.cloud.firestore.Firestore;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.hateoas.CollectionModel;
import org.springframework.hateoas.EntityModel;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;

@Service
public class SupplierService {
    private final WebClient webClient;

    @Autowired
    Firestore db;

    public SupplierService(WebClient.Builder webClientBuilder) {
        this.webClient = webClientBuilder.baseUrl("http://20.37.125.125:8089").build();
    }

    public Car[] getAllCars() throws ExecutionException, InterruptedException {
        CollectionModel<EntityModel<Car>> carsModel = webClient.get()
                .uri("/api/cars")
                .retrieve()
                .bodyToMono(new ParameterizedTypeReference<CollectionModel<EntityModel<Car>>>() {})
                .block();

        List<Car> cars = carsModel.getContent().stream()
                .map(EntityModel::getContent)
                .collect(Collectors.toList());
        var query = db.collection("cars").get().get();
        if (query.isEmpty()) {
            //alle cars in the db
            for(Car car: cars){
                db.collection("cars").document().set(car).get();
            }
        }

        return cars.toArray(new Car[0]);
    }

    public Car getCarById(UUID id) {
        return webClient.get()
                .uri("api/cars/" + id)
                .retrieve()
                .bodyToMono(Car.class)
                .block();
    }
}
