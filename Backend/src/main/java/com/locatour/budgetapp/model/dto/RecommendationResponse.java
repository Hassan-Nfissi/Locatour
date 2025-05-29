package com.locatour.budgetapp.model.dto;

import java.util.List;

// Importation des annotations Lombok (optionnel)
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

/**
 * DTO (Data Transfer Object) pour la réponse de recommandation de budget.
 * Contient la répartition du budget et les lieux recommandés.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class RecommendationResponse {
    private double budgetLogement; // Budget alloué au logement
    private double budgetNourriture; // Budget alloué à la nourriture
    private double budgetTourisme; // Budget alloué au tourisme
    private String explicationAI; // Explication générée par l'IA concernant la répartition
    private List<PlaceInfo> lieuxRecommandes; // Liste des lieux recommandés par Google Places
}
