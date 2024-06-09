package be.kuleuven.dsgt4;

import org.springframework.core.ParameterizedTypeReference;
import org.springframework.hateoas.CollectionModel;
import org.springframework.hateoas.EntityModel;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.BodyInserters;
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
            try {
                if (!Boolean.TRUE.equals(webClient.post()
                        .uri(uriBuilder -> uriBuilder
                                .path("/rest/exhaustsystems/order/{id}")
                                .queryParam("key", API_KEY) // Include API key here
                                .build(exhaust.getId()))
                        .body(BodyInserters.fromValue(exhausts))
                        .retrieve()
                        .bodyToMono(Boolean.class) // You can use the appropriate type if the response has a body
                        .block())) {
                    return false;
                }
            } catch (WebClientResponseException e) {
                // Handle WebClient-specific exceptions here
                e.printStackTrace();
                return false;
            } catch (Exception e) {
                // Handle other exceptions here
                e.printStackTrace();
                return false;
            }
        }
        return true;
    }
}
