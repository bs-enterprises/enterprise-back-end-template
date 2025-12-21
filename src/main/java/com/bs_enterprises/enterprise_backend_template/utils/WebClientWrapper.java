package com.bs_enterprises.enterprise_backend_template.utils;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.util.MultiValueMap;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import javax.validation.constraints.NotBlank;
import java.util.Map;
import java.util.Objects;

import static com.bs_enterprises.enterprise_backend_template.constants.ApplicationConstants.*;
import static org.springframework.http.HttpHeaders.AUTHORIZATION;

@Service
public class WebClientWrapper {

    private final ObjectMapper objectMapper;
    private final WebClient webClient;

    public WebClientWrapper(ObjectMapper objectMapper, WebClient webClient) {
        this.objectMapper = objectMapper;
        this.webClient = webClient;
    }

    @CircuitBreaker(name = "service", fallbackMethod = "availableMethod")
    public String webclientRequest(String token, String url, @NotBlank String requestType, Object data) {
        if (requestType.equalsIgnoreCase(GET)) {
            return Objects.requireNonNull(webClient.get()
                            .uri(url)
                            .header(AUTHORIZATION, BEARER + token)
                            .retrieve()
                            .onStatus(HttpStatusCode::isError, clientResponse -> {
                                Mono<String> errorMessage = clientResponse.bodyToMono(String.class);
                                return errorMessage.flatMap(msg -> Mono.error(new RuntimeException(msg)));
                            }))
                    .bodyToMono(String.class)
                    .block();
        } else {
            if (requestType.equalsIgnoreCase(DELETE)) {
                if (data == null) {
                    return Objects.requireNonNull(webClient.method(HttpMethod.DELETE)
                                    .uri(url)
                                    .header(AUTHORIZATION, BEARER + token)
                                    .retrieve()
                                    .onStatus(HttpStatusCode::isError, clientResponse -> {
                                        Mono<String> errorMessage = clientResponse.bodyToMono(String.class);
                                        return errorMessage.flatMap(msg -> Mono.error(new RuntimeException(msg)));

                                    }))
                            .bodyToMono(String.class)
                            .block();
                } else {
                    return Objects.requireNonNull(webClient.method(HttpMethod.DELETE)
                                    .uri(url)
                                    .header(AUTHORIZATION, BEARER + token)
                                    .bodyValue(data)
                                    .retrieve()
                                    .onStatus(HttpStatusCode::isError, clientResponse -> {
                                        Mono<String> errorMessage = clientResponse.bodyToMono(String.class);
                                        return errorMessage.flatMap(msg -> Mono.error(new RuntimeException(msg)));

                                    }))
                            .bodyToMono(String.class)
                            .block();
                }
            }
            if (requestType.equalsIgnoreCase(PUT)) {
                return Objects.requireNonNull(webClient.put()
                                .uri(url)
                                .bodyValue(data)
                                .header(AUTHORIZATION, BEARER + token)
                                .retrieve()
                                .onStatus(HttpStatusCode::isError, clientResponse -> {
                                    Mono<String> errorMessage = clientResponse.bodyToMono(String.class);
                                    return errorMessage.flatMap(msg -> Mono.error(new RuntimeException(msg)));

                                }))
                        .bodyToMono(String.class)
                        .block();
            }
            if (requestType.equalsIgnoreCase(PATCH)) {
                return Objects.requireNonNull(webClient.patch()
                                .uri(url)
                                .bodyValue(data)
                                .header(AUTHORIZATION, BEARER + token)
                                .retrieve()
                                .onStatus(HttpStatusCode::isError, clientResponse -> {
                                    Mono<String> errorMessage = clientResponse.bodyToMono(String.class);
                                    return errorMessage.flatMap(msg -> Mono.error(new RuntimeException(msg)));

                                }))
                        .bodyToMono(String.class)
                        .block();
            }
            return Objects.requireNonNull(webClient.post()
                            .uri(url)
                            .header(AUTHORIZATION, BEARER + token)
                            .bodyValue(data)
                            .retrieve()
                            .onStatus(HttpStatusCode::isError, clientResponse -> {
                                Mono<String> errorMessage = clientResponse.bodyToMono(String.class);
                                return errorMessage.flatMap(msg -> Mono.error(new RuntimeException(msg)));

                            }))
                    .bodyToMono(String.class)
                    .block();
        }
    }

    public ResponseEntity<String> webclientRequestwithHeaderResponse(String token, String url, @NotBlank String requestType) {
        if (requestType.equalsIgnoreCase(GET)) {
            return Objects.requireNonNull(webClient.get()
                    .uri(url)
                    .header(AUTHORIZATION, BEARER + token)
                    .retrieve()
                    .onStatus(HttpStatusCode::isError, clientResponse -> {
                        Mono<String> errorMessage = clientResponse.bodyToMono(String.class);
                        return errorMessage.flatMap(msg -> Mono.error(new RuntimeException(msg)));

                    })
                    .toEntity(String.class)
                    .block());
        }
        return null;
    }

    @CircuitBreaker(name = "service", fallbackMethod = "availableMethod")
    public String requestForExcelUpload(String token, String url, @NotBlank String requestType,
                                        MultiValueMap<String, Object> parts) {
        return Objects.requireNonNull(webClient.post()
                .uri(url)
                .header(AUTHORIZATION, BEARER + token)
                .contentType(MediaType.MULTIPART_FORM_DATA)
                .body(BodyInserters.fromMultipartData(parts))
                .retrieve()
                .onStatus(HttpStatusCode::isError, clientResponse -> {
                    Mono<String> errorMessage = clientResponse.bodyToMono(String.class);
                    return errorMessage.flatMap(msg -> Mono.error(new RuntimeException(msg)));

                })
                .bodyToMono(String.class) // Assuming you want a response from the service
                .block()); // Wait for the response, you can handle it as needed
    }

    public String availableMethod(Exception ex) throws JsonProcessingException, IllegalStateException {
        String abc = ex.getMessage();
        Map<String, Object> map = this.objectMapper.readValue(abc, Map.class);
        throw new IllegalStateException(String.format("Service %s not available", map.get("path")));
    }

    public String webclientRequestWithHeaders(String token, String url, String requestType, Object data, HttpHeaders headers) {
        WebClient.RequestBodySpec requestSpec = webClient.method(HttpMethod.valueOf(requestType))
                .uri(url)
                .headers(httpHeaders -> httpHeaders.addAll(headers))
                .header(AUTHORIZATION, BEARER + token);

        // If data exists, attach it to the request body
        if (data != null) {
            if (data instanceof MultiValueMap) {
                // Handle the case for application/x-www-form-urlencoded data
                requestSpec.body(BodyInserters.fromFormData((MultiValueMap<String, String>) data));
            } else {
                // Otherwise, treat it as JSON or other body type
                requestSpec.bodyValue(data);
            }
        }

        return Objects.requireNonNull(requestSpec.retrieve()
                .onStatus(HttpStatusCode::isError, clientResponse -> {
                    Mono<String> errorMessage = clientResponse.bodyToMono(String.class);
                    return errorMessage.flatMap(msg -> Mono.error(new RuntimeException(msg)));
                })
                .bodyToMono(String.class)
                .block());
    }

}