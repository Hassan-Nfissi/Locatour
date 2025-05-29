package com.locatour.budgetapp.model.dto;

import java.util.List;
import java.util.Map;

/**
 * DTO (Data Transfer Object) pour la réponse de simulation de budget.
 * Contient la recommandation générée par l'IA.
 */
public class SimulationResponse {
    private String recommendation; // La recommandation budgétaire générée par l'IA
    private List<DaySchedule> schedule;
    private double budgetParJour;
    private int nombreJours;
    private Map<String, Double> repartitionBudget;
    private boolean premium;
    private int simulationsRestantes; // -1 pour premium (illimité)

    // Classe interne pour représenter un jour dans l'emploi du temps
    public static class DaySchedule {
        private int jour;
        private List<Activity> activities;
        private double budgetJour;

        public static class Activity {
            private String heure;
            private String description;
            private String type; // "logement", "nourriture", "tourisme"
            private double coutEstime;
            private String photoUrl;
            private String placeDetails;

            // Getters et Setters
            public String getHeure() { return heure; }
            public void setHeure(String heure) { this.heure = heure; }
            public String getDescription() { return description; }
            public void setDescription(String description) { this.description = description; }
            public String getType() { return type; }
            public void setType(String type) { this.type = type; }
            public double getCoutEstime() { return coutEstime; }
            public void setCoutEstime(double coutEstime) { this.coutEstime = coutEstime; }
            public String getPhotoUrl() { return photoUrl; }
            public void setPhotoUrl(String photoUrl) { this.photoUrl = photoUrl; }
            public String getPlaceDetails() { return placeDetails; }
            public void setPlaceDetails(String placeDetails) { this.placeDetails = placeDetails; }
        }

        // Getters et Setters
        public int getJour() { return jour; }
        public void setJour(int jour) { this.jour = jour; }
        public List<Activity> getActivities() { return activities; }
        public void setActivities(List<Activity> activities) { this.activities = activities; }
        public double getBudgetJour() { return budgetJour; }
        public void setBudgetJour(double budgetJour) { this.budgetJour = budgetJour; }
    }

    // Getters et Setters
    public String getRecommendation() { return recommendation; }
    public void setRecommendation(String recommendation) { this.recommendation = recommendation; }
    public List<DaySchedule> getSchedule() { return schedule; }
    public void setSchedule(List<DaySchedule> schedule) { this.schedule = schedule; }
    public double getBudgetParJour() { return budgetParJour; }
    public void setBudgetParJour(double budgetParJour) { this.budgetParJour = budgetParJour; }
    public int getNombreJours() { return nombreJours; }
    public void setNombreJours(int nombreJours) { this.nombreJours = nombreJours; }
    public Map<String, Double> getRepartitionBudget() { return repartitionBudget; }
    public void setRepartitionBudget(Map<String, Double> repartitionBudget) { this.repartitionBudget = repartitionBudget; }
    public boolean isPremium() {
        return premium;
    }
    public void setPremium(boolean premium) {
        this.premium = premium;
    }
    public int getSimulationsRestantes() {
        return simulationsRestantes;
    }
    public void setSimulationsRestantes(int simulationsRestantes) {
        this.simulationsRestantes = simulationsRestantes;
    }
}
