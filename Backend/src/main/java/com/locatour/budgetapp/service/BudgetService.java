package com.locatour.budgetapp.service;

import com.locatour.budgetapp.ai.AIService;
import com.locatour.budgetapp.model.User;
import com.locatour.budgetapp.model.dto.PlaceInfo;
import com.locatour.budgetapp.model.dto.SimulationRequest;
import com.locatour.budgetapp.model.dto.SimulationResponse;
import com.locatour.budgetapp.model.dto.SimulationResponse.DaySchedule;
import com.locatour.budgetapp.model.dto.SimulationResponse.DaySchedule.Activity;
import com.locatour.budgetapp.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional; // Import for transactional operations

import java.util.*;

@Service
public class BudgetService {

    @Autowired
    private AIService aiService;

    @Autowired
    private GooglePlacesService googlePlacesService;

    @Autowired
    private AuthService authService; // Autowire the AuthService to get the current user

    @Autowired
    private UserRepository userRepository; // Autowire UserRepository to save user changes

    @Transactional // Ensures the user update and AI call are part of a single transaction
    public SimulationResponse simulateBudget(SimulationRequest request) {
        try {
            // Vérifier le statut de l'utilisateur et les limites
            User currentUser = authService.getCurrentUser();
            
            // Vérification explicite du statut premium
            boolean isPremium = false;
            if (currentUser != null) {
                isPremium = currentUser.isSubscribed();
                System.out.println("Service - Utilisateur: " + currentUser.getUsername() + 
                                 ", Premium: " + isPremium + 
                                 ", Essais restants: " + currentUser.getFreeTriesLeft());
            }

            // Pour les utilisateurs non premium, vérifier les limites
            if (!isPremium) {
                if (currentUser.getFreeTriesLeft() <= 0) {
                    throw new IllegalStateException("Vous avez atteint la limite de simulations gratuites. Passez à la version premium pour des simulations illimitées.");
                }
                // Forcer la limite à 5 jours pour les utilisateurs non premium
                request.setForceLimiteDays(5);
                currentUser.setFreeTriesLeft(currentUser.getFreeTriesLeft() - 1);
                userRepository.save(currentUser);
            }

            // Calculer le nombre de jours optimal en fonction du budget et des préférences
            int nombreJours = calculerNombreJoursOptimal(request.getBudgetGlobal(), request, isPremium);
            
            // Calculer le budget par jour
            double budgetParJour = request.getBudgetGlobal() / nombreJours;

            // Créer la réponse
            SimulationResponse response = new SimulationResponse();
            response.setNombreJours(nombreJours);
            response.setBudgetParJour(budgetParJour);
            response.setPremium(isPremium);
            response.setSimulationsRestantes(isPremium ? -1 : currentUser.getFreeTriesLeft());

            // Calculer la répartition du budget
            Map<String, Double> repartitionBudget = calculerRepartitionBudget(request);
            response.setRepartitionBudget(repartitionBudget);

            // Générer l'emploi du temps
            List<DaySchedule> schedule = genererEmploiDuTemps(nombreJours, budgetParJour, request, repartitionBudget);
            response.setSchedule(schedule);

            // Générer la recommandation textuelle
            String recommendation = generateRecommendation(request, nombreJours, budgetParJour, isPremium);
            response.setRecommendation(recommendation);

            return response;
        } catch (Exception e) {
            throw new RuntimeException("Erreur lors de la simulation du budget: " + e.getMessage());
        }
    }

    private int calculerNombreJoursOptimal(double budgetGlobal, SimulationRequest request, boolean isPremium) {
        // Si une limite est forcée, l'utiliser directement
        if (request.getForceLimiteDays() != null) {
            System.out.println("Utilisation de la limite forcée: " + request.getForceLimiteDays() + " jours");
            return request.getForceLimiteDays();
        }

        // Coûts de base par jour selon la qualité
        Map<String, Double> coutBaseLogement = new HashMap<>();
        coutBaseLogement.put("économique", 30.0);
        coutBaseLogement.put("standard", 80.0);
        coutBaseLogement.put("luxe", 200.0);

        Map<String, Double> coutBaseNourriture = new HashMap<>();
        coutBaseNourriture.put("basique", 23.0);
        coutBaseNourriture.put("variée", 55.0);
        coutBaseNourriture.put("gastronomique", 175.0);

        Map<String, Double> coutBaseTourisme = new HashMap<>();
        coutBaseTourisme.put("découverte", 0.0);
        coutBaseTourisme.put("culturel", 40.0);
        coutBaseTourisme.put("aventure", 125.0);

        // Calculer le coût journalier total
        double coutJournalier = 
            coutBaseLogement.get(request.getQualiteLogement()) +
            coutBaseNourriture.get(request.getQualiteNourriture()) +
            coutBaseTourisme.get(request.getQualiteTourisme());

        // Calculer le nombre de jours optimal
        int nombreJours = (int) Math.floor(budgetGlobal / coutJournalier);

        // Appliquer les limites selon le statut premium
        int maxJours = isPremium ? 30 : 5;
        nombreJours = Math.max(1, Math.min(nombreJours, maxJours));
        
        System.out.println("Calcul du nombre de jours - Budget: " + budgetGlobal + 
                          "€, Coût journalier: " + coutJournalier + 
                          "€, Jours calculés: " + nombreJours + 
                          ", Premium: " + isPremium +
                          ", Limite max: " + maxJours);

        return nombreJours;
    }

    private Map<String, Double> calculerRepartitionBudget(SimulationRequest request) {
        Map<String, Double> repartition = new HashMap<>();
        
        // Logement (40-50% du budget)
        switch (request.getQualiteLogement()) {
            case "économique": repartition.put("logement", 0.40); break;
            case "standard": repartition.put("logement", 0.45); break;
            case "luxe": repartition.put("logement", 0.50); break;
            default: repartition.put("logement", 0.45); break;
        }
        
        // Nourriture (30-35% du budget)
        switch (request.getQualiteNourriture()) {
            case "basique": repartition.put("nourriture", 0.30); break;
            case "variée": repartition.put("nourriture", 0.32); break;
            case "gastronomique": repartition.put("nourriture", 0.35); break;
            default: repartition.put("nourriture", 0.32); break;
        }
        
        // Tourisme (15-30% du budget)
        switch (request.getQualiteTourisme()) {
            case "découverte": repartition.put("tourisme", 0.15); break;
            case "culturel": repartition.put("tourisme", 0.23); break;
            case "aventure": repartition.put("tourisme", 0.30); break;
            default: repartition.put("tourisme", 0.23); break;
        }
        
        return repartition;
    }

    private List<DaySchedule> genererEmploiDuTemps(int nombreJours, double budgetParJour, 
            SimulationRequest request, Map<String, Double> repartitionBudget) {
        List<DaySchedule> schedule = new ArrayList<>();
        
        // Calculer les budgets par catégorie
        double budgetLogementParJour = budgetParJour * repartitionBudget.get("logement");
        double budgetNourritureParJour = budgetParJour * repartitionBudget.get("nourriture");
        double budgetTourismeParJour = budgetParJour * repartitionBudget.get("tourisme");
        
        // Répartition du budget nourriture
        double budgetPetitDej = budgetNourritureParJour * 0.2;  // 20% du budget nourriture
        double budgetDejeuner = budgetNourritureParJour * 0.35; // 35% du budget nourriture
        double budgetDiner = budgetNourritureParJour * 0.45;    // 45% du budget nourriture
        
        // Répartition du budget tourisme
        double budgetActiviteMatin = budgetTourismeParJour * 0.5;   // 50% du budget tourisme
        double budgetActiviteAprem = budgetTourismeParJour * 0.5;   // 50% du budget tourisme
        
        for (int jour = 1; jour <= nombreJours; jour++) {
            DaySchedule daySchedule = new DaySchedule();
            daySchedule.setJour(jour);
            daySchedule.setBudgetJour(budgetParJour);
            
            List<Activity> activities = new ArrayList<>();
            
            // Petit-déjeuner
            Activity petitDej = new Activity();
            petitDej.setHeure("08:00");
            petitDej.setType("nourriture");
            petitDej.setDescription(switch (request.getQualiteNourriture()) {
                case "basique" -> "Petit-déjeuner simple";
                case "variée" -> "Petit-déjeuner dans un café";
                case "gastronomique" -> "Brunch gastronomique";
                default -> "Petit-déjeuner";
            });
            petitDej.setCoutEstime(budgetPetitDej);
            petitDej = enrichActivityWithPlace(petitDej, request.getDestination(), "nourriture", request.getQualiteNourriture());
            activities.add(petitDej);
            
            // Activité du matin
            Activity activityMatin = new Activity();
            activityMatin.setHeure("10:00");
            activityMatin.setType("tourisme");
            activityMatin.setDescription(switch (request.getQualiteTourisme()) {
                case "découverte" -> "Visite guidée";
                case "culturel" -> "Visite culturelle";
                case "aventure" -> "Excursion";
                default -> "Activité du matin";
            });
            activityMatin.setCoutEstime(budgetActiviteMatin);
            activityMatin = enrichActivityWithPlace(activityMatin, request.getDestination(), "tourisme", request.getQualiteTourisme());
            activities.add(activityMatin);
            
            // Déjeuner
            Activity dejeuner = new Activity();
            dejeuner.setHeure("13:00");
            dejeuner.setType("nourriture");
            dejeuner.setDescription(switch (request.getQualiteNourriture()) {
                case "basique" -> "Repas local";
                case "variée" -> "Restaurant traditionnel";
                case "gastronomique" -> "Restaurant gastronomique";
                default -> "Déjeuner";
            });
            dejeuner.setCoutEstime(budgetDejeuner);
            dejeuner = enrichActivityWithPlace(dejeuner, request.getDestination(), "nourriture", request.getQualiteNourriture());
            activities.add(dejeuner);
            
            // Activité de l'après-midi
            Activity activityAprem = new Activity();
            activityAprem.setHeure("15:00");
            activityAprem.setType("tourisme");
            activityAprem.setDescription(switch (request.getQualiteTourisme()) {
                case "découverte" -> "Découverte locale";
                case "culturel" -> "Visite guidée thématique";
                case "aventure" -> "Activité aventure";
                default -> "Activité de l'après-midi";
            });
            activityAprem.setCoutEstime(budgetActiviteAprem);
            activityAprem = enrichActivityWithPlace(activityAprem, request.getDestination(), "tourisme", request.getQualiteTourisme());
            activities.add(activityAprem);
            
            // Dîner
            Activity diner = new Activity();
            diner.setHeure("20:00");
            diner.setType("nourriture");
            diner.setDescription(switch (request.getQualiteNourriture()) {
                case "basique" -> "Dîner simple";
                case "variée" -> "Restaurant local";
                case "gastronomique" -> "Restaurant gastronomique";
                default -> "Dîner";
            });
            diner.setCoutEstime(budgetDiner);
            diner = enrichActivityWithPlace(diner, request.getDestination(), "nourriture", request.getQualiteNourriture());
            activities.add(diner);
            
            // Logement
            Activity logement = new Activity();
            logement.setHeure("22:00");
            logement.setType("logement");
            logement.setDescription(switch (request.getQualiteLogement()) {
                case "économique" -> "Hébergement économique";
                case "standard" -> "Hébergement standard";
                case "luxe" -> "Hébergement luxueux";
                default -> "Hébergement";
            });
            logement.setCoutEstime(budgetLogementParJour);
            logement = enrichActivityWithPlace(logement, request.getDestination(), "logement", request.getQualiteLogement());
            activities.add(logement);
            
            daySchedule.setActivities(activities);
            schedule.add(daySchedule);
        }
        
        return schedule;
    }

    private String generateRecommendation(SimulationRequest request, int nombreJours, double budgetParJour, boolean isPremium) {
        StringBuilder recommendation = new StringBuilder();
        recommendation.append(String.format("Pour votre voyage à %s avec un budget de %.2f€ :\n\n", 
            request.getDestination(), request.getBudgetGlobal()));

        // Expliquer le calcul du nombre de jours
        recommendation.append(String.format("📅 En fonction de vos préférences, nous recommandons un séjour de %d jours (%.2f€/jour) :\n", 
            nombreJours, budgetParJour));
        recommendation.append(String.format("- Logement %s : ~%.2f€/nuit\n", 
            request.getQualiteLogement(),
            request.getQualiteLogement().equals("économique") ? 30.0 :
            request.getQualiteLogement().equals("standard") ? 80.0 : 200.0));
        recommendation.append(String.format("- Repas %s : ~%.2f€/jour\n",
            request.getQualiteNourriture(),
            request.getQualiteNourriture().equals("basique") ? 23.0 :
            request.getQualiteNourriture().equals("variée") ? 55.0 : 175.0));
        recommendation.append(String.format("- Activités %s : ~%.2f€/jour\n\n",
            request.getQualiteTourisme(),
            request.getQualiteTourisme().equals("découverte") ? 0.0 :
            request.getQualiteTourisme().equals("culturel") ? 40.0 : 125.0));

        // Ajouter les recommandations détaillées
        recommendation.append("🏨 Logement (" + request.getQualiteLogement() + ") :\n");
        switch (request.getQualiteLogement()) {
            case "économique":
                recommendation.append("- Auberges de jeunesse ou hostels\n");
                recommendation.append("- Airbnb partagés\n");
                break;
            case "standard":
                recommendation.append("- Hôtels 3 étoiles\n");
                recommendation.append("- Airbnb entier\n");
                break;
            case "luxe":
                recommendation.append("- Hôtels 4-5 étoiles\n");
                recommendation.append("- Appartements de luxe\n");
                break;
        }

        recommendation.append("\n🍽️ Restauration (" + request.getQualiteNourriture() + ") :\n");
        switch (request.getQualiteNourriture()) {
            case "basique":
                recommendation.append("- Supermarchés locaux et cuisine\n");
                recommendation.append("- Street food\n");
                break;
            case "variée":
                recommendation.append("- Restaurants locaux\n");
                recommendation.append("- Mix de cuisine locale et internationale\n");
                break;
            case "gastronomique":
                recommendation.append("- Restaurants étoilés\n");
                recommendation.append("- Expériences culinaires uniques\n");
                break;
        }

        recommendation.append("\n🎯 Activités (" + request.getQualiteTourisme() + ") :\n");
        switch (request.getQualiteTourisme()) {
            case "découverte":
                recommendation.append("- Visites guidées gratuites\n");
                recommendation.append("- Attractions principales\n");
                break;
            case "culturel":
                recommendation.append("- Visites guidées privées\n");
                recommendation.append("- Musées et expositions\n");
                break;
            case "aventure":
                recommendation.append("- Activités sportives\n");
                recommendation.append("- Excursions guidées\n");
                break;
        }

        recommendation.append("\n💡 Conseils pour optimiser votre séjour :\n");
        recommendation.append("- Réservez votre logement à l'avance\n");
        recommendation.append("- Privilégiez les transports en commun\n");
        recommendation.append("- Gardez ~10% de budget pour les imprévus\n");

        // Ajouter une note sur le statut premium si nécessaire
        if (!isPremium) {
            recommendation.append("\n⭐ Note : Passez à la version premium pour :\n");
            recommendation.append("- Des simulations illimitées\n");
            recommendation.append("- Des séjours jusqu'à 30 jours\n");
            recommendation.append("- Des suggestions de lieux plus détaillées\n");
        }

        return recommendation.toString();
    }

    private Map<String, String> getGooglePlaceType(String activityType, String qualityLevel) {
        Map<String, String> searchParams = new HashMap<>();
        
        switch (activityType) {
            case "nourriture":
                searchParams.put("type", "restaurant");
                switch (qualityLevel) {
                    case "basique":
                        searchParams.put("query", "casual restaurant street food");
                        break;
                    case "variée":
                        searchParams.put("query", "local restaurant traditional");
                        break;
                    case "gastronomique":
                        searchParams.put("query", "fine dining restaurant gourmet");
                        break;
                    default:
                        searchParams.put("query", "restaurant");
                        break;
                }
                break;
                
            case "logement":
                searchParams.put("type", "lodging");
                switch (qualityLevel) {
                    case "économique":
                        searchParams.put("query", "hostel budget hotel");
                        break;
                    case "standard":
                        searchParams.put("query", "3 star hotel");
                        break;
                    case "luxe":
                        searchParams.put("query", "luxury hotel 4 star 5 star");
                        break;
                    default:
                        searchParams.put("query", "hotel");
                        break;
                }
                break;
                
            case "tourisme":
                switch (qualityLevel) {
                    case "découverte":
                        searchParams.put("type", "tourist_attraction");
                        searchParams.put("query", "popular attraction landmark");
                        break;
                    case "culturel":
                        searchParams.put("type", "museum");
                        searchParams.put("query", "museum cultural site");
                        break;
                    case "aventure":
                        searchParams.put("type", "amusement_park");
                        searchParams.put("query", "adventure activity outdoor");
                        break;
                    default:
                        searchParams.put("type", "tourist_attraction");
                        searchParams.put("query", "tourist attraction");
                        break;
                }
                break;
            default:
                searchParams.put("type", "point_of_interest");
                searchParams.put("query", "point of interest");
                break;
        }
        
        return searchParams;
    }

    private Activity enrichActivityWithPlace(Activity activity, String destination, String activityType, String qualityLevel) {
        try {
            Map<String, String> searchParams = getGooglePlaceType(activityType, qualityLevel);
            if (searchParams.isEmpty()) {
                return activity;
            }

            List<PlaceInfo> places = googlePlacesService.searchPlaces(
                searchParams.get("query"),
                searchParams.get("type"),
                destination
            );

            if (!places.isEmpty()) {
                // Prendre un lieu aléatoire parmi les 3 premiers résultats pour plus de variété
                PlaceInfo selectedPlace = places.get(new Random().nextInt(Math.min(3, places.size())));
                
                activity.setDescription(String.format("%s - %s", 
                    activity.getDescription(), 
                    selectedPlace.getName()
                ));
                
                if (selectedPlace.getPhotoUrl() != null) {
                    activity.setPhotoUrl(selectedPlace.getPhotoUrl());
                }
                
                activity.setPlaceDetails(String.format("%s (Note: %.1f/5)", 
                    selectedPlace.getDescription(),
                    selectedPlace.getRating()
                ));
            }
        } catch (Exception e) {
            // En cas d'erreur, on garde l'activité originale sans enrichissement
            System.err.println("Erreur lors de l'enrichissement de l'activité: " + e.getMessage());
        }
        
        return activity;
    }
}
