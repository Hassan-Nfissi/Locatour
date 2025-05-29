package com.locatour.budgetapp.model.dto;

/**
 * DTO (Data Transfer Object) pour la requête de simulation de budget.
 * Contient les informations fournies par l'utilisateur depuis le frontend.
 */
public class SimulationRequest {
    private String username;
    private String destination;
    private double budgetGlobal;
    private String qualiteLogement;  // "économique", "standard", "luxe"
    private String qualiteNourriture;  // "basique", "variée", "gastronomique"
    private String qualiteTourisme;  // "découverte", "culturel", "aventure"
    private Integer forceLimiteDays; // Limite forcée du nombre de jours

    // Getters et Setters
    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }
    
    public String getDestination() { return destination; }
    public void setDestination(String destination) { this.destination = destination; }
    
    public double getBudgetGlobal() { return budgetGlobal; }
    public void setBudgetGlobal(double budgetGlobal) { this.budgetGlobal = budgetGlobal; }
    
    public String getQualiteLogement() { return qualiteLogement; }
    public void setQualiteLogement(String qualiteLogement) { this.qualiteLogement = qualiteLogement; }
    
    public String getQualiteNourriture() { return qualiteNourriture; }
    public void setQualiteNourriture(String qualiteNourriture) { this.qualiteNourriture = qualiteNourriture; }
    
    public String getQualiteTourisme() { return qualiteTourisme; }
    public void setQualiteTourisme(String qualiteTourisme) { this.qualiteTourisme = qualiteTourisme; }

    // Nouveaux getters et setters pour forceLimiteDays
    public Integer getForceLimiteDays() { return forceLimiteDays; }
    public void setForceLimiteDays(Integer forceLimiteDays) { this.forceLimiteDays = forceLimiteDays; }
}
