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
public class SupplierServiceCar {
    private final WebClient webClient;
    private static final String API_KEY = "Iw8zeveVyaPNWonPNaU0213uw3g6Ei";

    public SupplierServiceCar(WebClient.Builder webClientBuilder) {
        this.webClient = webClientBuilder.baseUrl("http://20.37.125.125:8089").build();
    }

    public Car[] getAllCars(){
        CollectionModel<EntityModel<Car>> carsModel = webClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path("api/cars")
                        .queryParam("key", API_KEY)
                        .build())
                .retrieve()
                .bodyToMono(new ParameterizedTypeReference<CollectionModel<EntityModel<Car>>>() {})
                .block();

        List<Car> cars = carsModel.getContent().stream()
                .map(EntityModel::getContent)
                .collect(Collectors.toList());

        return cars.toArray(new Car[0]);
    }

    public Car getCarById(UUID id) {
        return webClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path("api/cars/" + id)
                        .queryParam("key", API_KEY)
                        .build())
                .retrieve()
                .bodyToMono(Car.class)
                .block();
    }

    public void orderCar(UUID id) {
        webClient.post()
                .uri(uriBuilder -> uriBuilder
                        .path("api/cars/" + id + "/order")
                        .queryParam("key", API_KEY)
                        .build())
                .retrieve()
                .bodyToMono(Void.class)
                .block();
    }

    public void reserveCar(UUID id) {
        webClient.post()
                .uri(uriBuilder -> uriBuilder
                        .path("api/cars/" + id + "/reserve")
                        .queryParam("key", API_KEY)
                        .build())
                .retrieve()
                .bodyToMono(Void.class)
                .block();
    }

    public void cancelCar(UUID id) {
        webClient.post()
                .uri(uriBuilder -> uriBuilder
                        .path("api/cars/" + id + "/cancel")
                        .queryParam("key", API_KEY)
                        .build())
                .retrieve()
                .bodyToMono(Void.class)
                .block();
    }
}
