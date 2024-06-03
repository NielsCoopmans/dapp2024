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

    public SupplierServiceExhaust(WebClient.Builder webClientBuilder) {
        this.webClient = webClientBuilder.baseUrl("http://40.125.67.223:8083").build();
    }

    public Exhaust[] getAllExhausts() {
        List<Exhaust> exhausts = webClient.get()
                .uri("/restrpc/exhaustsystems")
                .retrieve()
                .bodyToMono(new ParameterizedTypeReference<List<Exhaust>>() {})
                .block();

        return exhausts.toArray(new Exhaust[0]);
    }

    public Exhaust getExhaustById(int id) {
        return webClient.get()
                .uri("restrpc/exhaustsystems/" + id)
                .retrieve()
                .bodyToMono(Exhaust.class)
                .block();
    }

    public boolean orderExhaust(Exhaust[] exhausts) {
        try {
            webClient.post()
                    .uri("/restrpc/exhaustsystems/order")
                    .body(BodyInserters.fromValue(exhausts))
                    .retrieve()
                    .bodyToMono(Void.class) // You can use the appropriate type if the response has a body
                    .block();
            return true;
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

    public boolean checkOrder(int OrderId) {
        try {
            return Boolean.TRUE.equals(webClient.get()
                    .uri(uriBuilder -> uriBuilder.path("/restrpc/exhaustsystems/order/{id}").build(OrderId))
                    .retrieve()
                    .bodyToMono(Boolean.class)
                    .block());
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
}