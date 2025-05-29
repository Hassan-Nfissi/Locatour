package com.locatour.budgetapp.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.locatour.budgetapp.model.dto.PlaceInfo;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.core.publisher.Mono;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

/**
 * Service pour interagir avec l'API Google Places.
 * Permet de rechercher des lieux (hôtels, restaurants, attractions) dans une destination donnée.
 */
@Service
public class GooglePlacesService {
    private static final Logger logger = LoggerFactory.getLogger(GooglePlacesService.class);

    @Value("${google.places.api.key}")
    private String apiKey; // Clé API Google Places, injectée depuis application.properties

    private final WebClient webClient; // Client HTTP réactif pour les appels API
    private final ObjectMapper objectMapper;

    /**
     * Constructeur avec injection de dépendance de WebClient.Builder.
     * @param webClientBuilder Le builder pour construire le WebClient.
     */
    public GooglePlacesService(WebClient.Builder webClientBuilder, ObjectMapper objectMapper) {
        // Initialisation du WebClient avec l'URL de base de l'API Google Places
        this.webClient = webClientBuilder.baseUrl("https://maps.googleapis.com/maps/api/place/").build();
        this.objectMapper = objectMapper;
    }

    /**
     * Recherche des lieux (hôtels, restaurants, attractions) en utilisant l'API Google Places (Text Search).
     *
     * @param query Le terme de recherche (ex: "hôtels", "restaurants", "musées").
     * @param type Le type de lieu à rechercher (ex: "lodging", "restaurant", "tourist_attraction").
     * @param destination La destination pour affiner la recherche.
     * @return Une liste de PlaceInfo contenant les détails des lieux trouvés.
     */
    public List<PlaceInfo> searchPlaces(String query, String type, String destination) {
        try {
            Mono<String> responseMono = webClient.get()
                    .uri(uriBuilder -> uriBuilder
                            .path("textsearch/json") // Endpoint pour la recherche textuelle
                            .queryParam("query", query + " " + destination) // Concatène la requête et la destination
                            .queryParam("type", type) // Filtre par type de lieu
                            .queryParam("key", apiKey) // Votre clé API Google Places
                            .build())
                    .retrieve() // Récupère la réponse
                    .bodyToMono(String.class)
                    .onErrorResume(WebClientResponseException.class, ex -> {
                        logger.error("Error calling Google Places API: {} - {}", ex.getRawStatusCode(), ex.getResponseBodyAsString());
                        return Mono.error(new RuntimeException("Failed to fetch places from Google API"));
                    });

            String jsonResponse = responseMono.block();
            return parsePlacesResponse(jsonResponse);
        } catch (Exception e) {
            logger.error("Error in searchPlaces: ", e);
            throw new RuntimeException("Failed to search places", e);
        }
    }

    /**
     * Parse la chaîne JSON de la réponse de l'API Google Places et la convertit
     * en une liste de PlaceInfo.
     *
     * @param jsonResponse La chaîne JSON reçue de l'API Google Places.
     * @return Une liste de PlaceInfo.
     */
    private List<PlaceInfo> parsePlacesResponse(String jsonResponse) {
        List<PlaceInfo> places = new ArrayList<>();

        try {
            JsonNode rootNode = objectMapper.readTree(jsonResponse);
            JsonNode status = rootNode.get("status");
            
            if (status != null && !"OK".equals(status.asText()) && !"ZERO_RESULTS".equals(status.asText())) {
                logger.error("Google Places API returned error status: {}", status.asText());
                throw new RuntimeException("Google Places API error: " + status.asText());
            }

            JsonNode resultsNode = rootNode.get("results");
            if (resultsNode != null && resultsNode.isArray()) {
                for (JsonNode placeNode : resultsNode) {
                    try {
                        places.add(parsePlaceNode(placeNode));
                    } catch (Exception e) {
                        logger.warn("Error parsing place node: {}", e.getMessage());
                    }
                }
            }

            return places;
        } catch (Exception e) {
            logger.error("Error parsing places response: ", e);
            throw new RuntimeException("Failed to parse places response", e);
        }
    }

    private PlaceInfo parsePlaceNode(JsonNode placeNode) {
        String name = getNodeTextOrDefault(placeNode, "name", "N/A");
        String address = getNodeTextOrDefault(placeNode, "formatted_address", "N/A");
        double rating = placeNode.has("rating") ? placeNode.get("rating").asDouble() : 0.0;
        String type = extractFirstType(placeNode);
        String photoUrl = extractPhotoUrl(placeNode);

        return new PlaceInfo(name, address, type, rating, photoUrl);
    }

    private String getNodeTextOrDefault(JsonNode node, String fieldName, String defaultValue) {
        JsonNode field = node.get(fieldName);
        return (field != null && !field.isNull()) ? field.asText() : defaultValue;
    }

    private String extractFirstType(JsonNode placeNode) {
        if (placeNode.has("types") && placeNode.get("types").isArray() && placeNode.get("types").size() > 0) {
            return placeNode.get("types").get(0).asText();
        }
        return "N/A";
    }

    private String extractPhotoUrl(JsonNode placeNode) {
        if (placeNode.has("photos") && placeNode.get("photos").isArray() && placeNode.get("photos").size() > 0) {
            JsonNode firstPhoto = placeNode.get("photos").get(0);
            if (firstPhoto.has("photo_reference")) {
                String photoReference = firstPhoto.get("photo_reference").asText();
                return String.format("https://maps.googleapis.com/maps/api/place/photo?maxwidth=400&photoreference=%s&key=%s",
                        photoReference, apiKey);
            }
        }
        return null;
    }

    /**
     * Récupère les détails d'un lieu spécifique via son ID Google Places.
     *
     * @param placeId L'identifiant unique du lieu dans Google Places
     * @return Les détails du lieu
     */
    public PlaceInfo getPlaceDetails(String placeId) {
        try {
            Mono<String> responseMono = webClient.get()
                    .uri(uriBuilder -> uriBuilder
                            .path("details/json")
                            .queryParam("place_id", placeId)
                            .queryParam("fields", "name,formatted_address,rating,type,photo,geometry,price_level,opening_hours,website")
                            .queryParam("key", apiKey)
                            .build())
                    .retrieve()
                    .bodyToMono(String.class)
                    .onErrorResume(WebClientResponseException.class, ex -> {
                        logger.error("Error fetching place details: {} - {}", ex.getRawStatusCode(), ex.getResponseBodyAsString());
                        return Mono.error(new RuntimeException("Failed to fetch place details"));
                    });

            String jsonResponse = responseMono.block();
            return parsePlaceDetails(jsonResponse);
        } catch (Exception e) {
            logger.error("Error in getPlaceDetails: ", e);
            throw new RuntimeException("Failed to get place details", e);
        }
    }

    /**
     * Récupère les lieux à proximité d'une position donnée.
     *
     * @param latitude Latitude du point central
     * @param longitude Longitude du point central
     * @param radius Rayon de recherche en mètres (max 50000)
     * @param type Type de lieu (optional)
     * @return Liste des lieux trouvés
     */
    public List<PlaceInfo> getNearbyPlaces(double latitude, double longitude, int radius, String type) {
        try {
            StringBuilder location = new StringBuilder()
                    .append(latitude)
                    .append(",")
                    .append(longitude);

            Mono<String> responseMono = webClient.get()
                    .uri(uriBuilder -> {
                        uriBuilder
                                .path("nearbysearch/json")
                                .queryParam("location", location.toString())
                                .queryParam("radius", Math.min(radius, 50000))
                                .queryParam("key", apiKey);
                        
                        if (type != null && !type.isEmpty()) {
                            uriBuilder.queryParam("type", type);
                        }
                        
                        return uriBuilder.build();
                    })
                    .retrieve()
                    .bodyToMono(String.class)
                    .onErrorResume(WebClientResponseException.class, ex -> {
                        logger.error("Error fetching nearby places: {} - {}", ex.getRawStatusCode(), ex.getResponseBodyAsString());
                        return Mono.error(new RuntimeException("Failed to fetch nearby places"));
                    });

            String jsonResponse = responseMono.block();
            return parsePlacesResponse(jsonResponse);
        } catch (Exception e) {
            logger.error("Error in getNearbyPlaces: ", e);
            throw new RuntimeException("Failed to get nearby places", e);
        }
    }

    /**
     * Parse la réponse JSON des détails d'un lieu.
     */
    private PlaceInfo parsePlaceDetails(String jsonResponse) {
        try {
            JsonNode rootNode = objectMapper.readTree(jsonResponse);
            JsonNode status = rootNode.get("status");
            
            if (status != null && !"OK".equals(status.asText())) {
                logger.error("Google Places API returned error status: {}", status.asText());
                throw new RuntimeException("Google Places API error: " + status.asText());
            }

            JsonNode resultNode = rootNode.get("result");
            if (resultNode != null) {
                return parsePlaceNode(resultNode);
            }

            throw new RuntimeException("No result found in place details response");
        } catch (Exception e) {
            logger.error("Error parsing place details: ", e);
            throw new RuntimeException("Failed to parse place details", e);
        }
    }
}
