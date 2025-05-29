package com.locatour.budgetapp.model.dto;

import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

/**
 * DTO (Data Transfer Object) pour les informations d'un lieu (hôtel, restaurant, attraction).
 * Utilisé pour les résultats de l'API Google Places.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class PlaceInfo {
    private String name;
    private String description;
    private String type;
    private double rating;
    private String photoUrl;
    private Double latitude;
    private Double longitude;

    public PlaceInfo(String name, String description, String type, double rating, String photoUrl) {
        this.name = name;
        this.description = description;
        this.type = type;
        this.rating = rating;
        this.photoUrl = photoUrl;
    }

    // Getters
    public String getName() { return name; }
    public String getDescription() { return description; }
    public String getType() { return type; }
    public double getRating() { return rating; }
    public String getPhotoUrl() { return photoUrl; }
    public Double getLatitude() { return latitude; }
    public Double getLongitude() { return longitude; }

    // Setters
    public void setName(String name) { this.name = name; }
    public void setDescription(String description) { this.description = description; }
    public void setType(String type) { this.type = type; }
    public void setRating(double rating) { this.rating = rating; }
    public void setPhotoUrl(String photoUrl) { this.photoUrl = photoUrl; }
    public void setLatitude(Double latitude) { this.latitude = latitude; }
    public void setLongitude(Double longitude) { this.longitude = longitude; }
}
