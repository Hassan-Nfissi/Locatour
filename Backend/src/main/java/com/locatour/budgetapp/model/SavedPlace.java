package com.locatour.budgetapp.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Entity
@Table(name = "saved_places")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class SavedPlace {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private String description;

    @Column
    private String imageUrl;

    @Column
    private Double latitude;

    @Column
    private Double longitude;

    @Column
    private String category;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    // Constructeur pratique pour la création
    public SavedPlace(String name, String description, String imageUrl, Double latitude, Double longitude, String category, User user) {
        this.name = name;
        this.description = description;
        this.imageUrl = imageUrl;
        this.latitude = latitude;
        this.longitude = longitude;
        this.category = category;
        this.user = user;
    }
} 