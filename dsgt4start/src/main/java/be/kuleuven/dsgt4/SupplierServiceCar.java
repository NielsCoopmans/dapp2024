package be.kuleuven.dsgt4;

import com.google.cloud.firestore.Firestore;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.hateoas.CollectionModel;
import org.springframework.hateoas.EntityModel;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.ClientResponse;
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
        this.webClient = webClientBuilder.baseUrl("http://20.37.125.125:8089/api/cars/").build();
    }

    public Car[] getAllCars(){
        CollectionModel<EntityModel<Car>> carsModel = webClient.get()
                .uri(uriBuilder -> uriBuilder
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
                        .path( id.toString() )
                        .queryParam("key", API_KEY)
                        .build())
                .retrieve()
                .bodyToMono(Car.class)
                .block();
    }

    public void orderCar(UUID id) {
        ClientResponse response = webClient.post()
                .uri(uriBuilder -> uriBuilder
                        .path( id.toString() + "/order")
                        .queryParam("key", API_KEY)
                        .build())
                .exchange()
                .block();

        if (response.statusCode().isError()) {
            System.err.println("Failed to order car: " + response.statusCode());
            String responseBody = response.bodyToMono(String.class).block();
            System.err.println("Response body: " + responseBody);
        } else {
            System.out.println("Car ordered successfully");
        }
    }

    public void reserveCar(UUID id) {
        webClient.post()
                .uri(uriBuilder -> uriBuilder
                        .path( id.toString() + "/reserve")
                        .queryParam("key", API_KEY)
                        .build())
                .retrieve()
                .bodyToMono(Void.class)
                .block();
    }

    public void cancelCar(UUID id) {
        webClient.post()
                .uri(uriBuilder -> uriBuilder
                        .path( id + "/cancel")
                        .queryParam("key", API_KEY)
                        .build())
                .retrieve()
                .bodyToMono(Void.class)
                .block();
    }
}
