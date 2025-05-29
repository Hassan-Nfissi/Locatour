package com.locatour.budgetapp.controller;

import com.locatour.budgetapp.model.SavedPlace;
import com.locatour.budgetapp.model.User;
import com.locatour.budgetapp.model.dto.PlaceInfo;
import com.locatour.budgetapp.repository.SavedPlaceRepository;
import com.locatour.budgetapp.service.AuthService;
import com.locatour.budgetapp.service.GooglePlacesService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.HashMap;

@RestController
@RequestMapping("/api/places")
@CrossOrigin(origins = "*")
@PreAuthorize("isAuthenticated()")
public class PlacesController {

    @Autowired
    private GooglePlacesService googlePlacesService;

    @Autowired
    private SavedPlaceRepository savedPlaceRepository;

    @Autowired
    private AuthService authService;

    /**
     * Récupère les lieux touristiques populaires.
     * Par défaut, recherche à Paris.
     */
    @GetMapping
    public ResponseEntity<?> getPlaces(
            @RequestParam(required = false) Double latitude,
            @RequestParam(required = false) Double longitude,
            @RequestParam(required = false, defaultValue = "5000") Integer radius,
            @RequestParam(required = false, defaultValue = "tourist_attraction") String type) {
        try {
            // Coordonnées par défaut (Paris)
            double lat = latitude != null ? latitude : 48.8566;
            double lng = longitude != null ? longitude : 2.3522;

            List<PlaceInfo> places = googlePlacesService.getNearbyPlaces(lat, lng, radius, type);
            return ResponseEntity.ok(places);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("message", "Erreur lors de la récupération des lieux: " + e.getMessage());
            error.put("status", "403");
            return ResponseEntity.status(403).body(error);
        }
    }

    /**
     * Récupère les détails d'un lieu spécifique.
     */
    @GetMapping("/{placeId}")
    public ResponseEntity<?> getPlaceDetails(@PathVariable String placeId) {
        try {
            PlaceInfo place = googlePlacesService.getPlaceDetails(placeId);
            return ResponseEntity.ok(place);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(error);
        }
    }

    /**
     * Recherche des lieux par mot-clé et type.
     */
    @GetMapping("/search")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> searchPlaces(
            @RequestParam(required = false) String query,
            @RequestParam(required = false) String type,
            @RequestParam(required = false) Double latitude,
            @RequestParam(required = false) Double longitude) {
        try {
            String searchQuery = query != null ? query : "point of interest";
            String searchType = type != null ? type : "tourist_attraction";
            String destination = "";
            if (latitude != null && longitude != null) {
                destination = latitude + "," + longitude;
            }
            List<PlaceInfo> places = googlePlacesService.searchPlaces(searchQuery, searchType, destination);
            return ResponseEntity.ok(places);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(error);
        }
    }

    /**
     * Récupère tous les lieux sauvegardés de l'utilisateur connecté.
     */
    @GetMapping("/saved")
    public ResponseEntity<?> getSavedPlaces() {
        try {
            User currentUser = authService.getCurrentUser();
            List<SavedPlace> savedPlaces = savedPlaceRepository.findByUser(currentUser);
            return ResponseEntity.ok(savedPlaces);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(error);
        }
    }

    /**
     * Supprime un lieu sauvegardé.
     */
    @DeleteMapping("/saved/{id}")
    public ResponseEntity<?> deletePlace(@PathVariable Long id) {
        try {
            User currentUser = authService.getCurrentUser();
            savedPlaceRepository.deleteByIdAndUser(id, currentUser);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(error);
        }
    }

    /**
     * Sauvegarde un nouveau lieu.
     */
    @PostMapping("/saved")
    public ResponseEntity<?> savePlace(@RequestBody PlaceInfo placeInfo) {
        try {
            User currentUser = authService.getCurrentUser();
            SavedPlace savedPlace = new SavedPlace(
                placeInfo.getName(),
                placeInfo.getDescription(),
                placeInfo.getPhotoUrl(),
                placeInfo.getLatitude(),
                placeInfo.getLongitude(),
                placeInfo.getType(),
                currentUser
            );
            savedPlace = savedPlaceRepository.save(savedPlace);
            return ResponseEntity.ok(savedPlace);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(error);
        }
    }
} 