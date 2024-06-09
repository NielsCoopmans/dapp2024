package be.kuleuven.dsgt4;

import org.springframework.core.ParameterizedTypeReference;
import org.springframework.hateoas.CollectionModel;
import org.springframework.hateoas.EntityModel;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.ClientResponse;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class SupplierServiceExhaust {
    private final WebClient webClient;
    private static final String API_KEY = "Iw8zeveVyaPNWonPNaU0213uw3g6Ei"; // Use the same API key

    public SupplierServiceExhaust(WebClient.Builder webClientBuilder) {
        this.webClient = webClientBuilder.baseUrl("http://40.125.67.223:8083").build();
    }

    public Exhaust[] getAllExhausts() {
        CollectionModel<EntityModel<Exhaust>> exhaustsModel = webClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/rest/exhaustsystems")
                        .queryParam("key", API_KEY) // Include API key here
                        .build())
                .retrieve()
                .bodyToMono(new ParameterizedTypeReference<CollectionModel<EntityModel<Exhaust>>>() {})
                .block();
        List<Exhaust> exhausts = exhaustsModel.getContent().stream()
                .map(EntityModel::getContent)
                .collect(Collectors.toList());
        return exhausts.toArray(new Exhaust[0]);
    }

    public Exhaust getExhaustById(int id) {
        return webClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path("rest/exhaustsystems/" + id)
                        .queryParam("key", API_KEY) // Include API key here
                        .build())
                .retrieve()
                .bodyToMono(Exhaust.class)
                .block();
    }

    public boolean currentStockExhaust(int id) {
        Exhaust exhaust = getExhaustById(id);
        if(exhaust != null && exhaust.getStock() > 0) {
            return true;
        }
        return false;
    }

    public boolean orderExhaust(Exhaust[] exhausts) {
        for (Exhaust exhaust : exhausts) {
            ClientResponse response = webClient.post()
                    .uri(uriBuilder -> uriBuilder
                            .path("/rest/exhaustsystems/order/{id}")
                            .queryParam("key", API_KEY)
                            .build(exhaust.getId()))
                    .exchange()
                    .block();

            if (response == null || response.statusCode().value() != 200) {
                System.err.println("Failed to order exhaust: " + (response != null ? response.statusCode() : "No response"));
                if (response != null) {
                    String responseBody = response.bodyToMono(String.class).block();
                    System.err.println("Response body: " + responseBody);
                }
                return false;
            }
        }
        return true;
    }


    public void cancelOrder(int id) {
        ClientResponse response = webClient.post()
                .uri(uriBuilder -> uriBuilder
                        .path("/rest/exhaustsystems/cancel/{id}")
                        .queryParam("key", API_KEY)
                        .build(id))
                .exchange()
                .block();

    }
}
