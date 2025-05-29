package com.locatour.budgetapp.ai;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.chat.prompt.PromptTemplate;
import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Value;

import java.util.Map;

/**
 * Service pour interagir avec le modèle de langage via Spring AI et Ollama.
 * Génère des recommandations de répartition de budget.
 */
@Service
public class AIService {

    private final ChatClient chatClient;
    @Value("${spring.ai.ollama.chat.model}") // Inject the model name from properties
    private String modelName; // Keep modelName if you want to log it or use it for other purposes

    /**
     * Constructeur avec injection de dépendance de ChatClient.
     * @param chatClient Le client de chat configuré par Spring AI.
     */
    public AIService(ChatClient chatClient) {
        this.chatClient = chatClient;
    }

    /**
     * Génère une recommandation de répartition de budget basée sur les paramètres fournis.
     * Le prompt est structuré pour demander une réponse au format JSON.
     *
     * @param budgetGlobal Le budget total.
     * @param destination La destination du voyage.
     * @param qualiteLogement La qualité souhaitée pour le logement.
     * @param qualiteNourriture La qualité souhaitée pour la nourriture.
     * @param qualiteTourisme La qualité souhaitée pour le tourisme.
     * @return Une chaîne JSON contenant la répartition du budget et une explication.
     */
    public String generateBudgetRecommendation(double budgetGlobal, String destination, String qualiteLogement, String qualiteNourriture, String qualiteTourisme) {
        String promptTemplateString = """
            Vous êtes un assistant de voyage expert. Pour un budget global de {budgetGlobal} EUR et une destination de {destination},
            avec une qualité de logement '{qualiteLogement}', une qualité de nourriture '{qualiteNourriture}' et une qualité de tourisme '{qualiteTourisme}',
            recommandez une répartition du budget entre 'logement', 'nourriture' et 'tourisme'.
            Fournissez la réponse uniquement sous forme de JSON avec les clés 'budgetLogement', 'budgetNourriture', 'budgetTourisme' (valeurs numériques en EUR) et 'explicationAI' (texte explicatif).
            Assurez-vous que la somme des budgets (logement + nourriture + tourisme) est égale au budget global.
            Exemple de format JSON: \\{ "budgetLogement": 500.00, "budgetNourriture": 300.00, "budgetTourisme": 200.00, "explicationAI": "Votre budget a été réparti..." }\\
            """;

        PromptTemplate promptTemplate = new PromptTemplate(promptTemplateString);
        Prompt prompt = promptTemplate.create(Map.of(
                "budgetGlobal", budgetGlobal,
                "destination", destination,
                "qualiteLogement", qualiteLogement,
                "qualiteNourriture", qualiteNourriture,
                "qualiteTourisme", qualiteTourisme
        ));

        // Appel au modèle de chat Ollama via ChatClient
        // The .content() method directly returns a String in Spring AI 1.0.0+
        // Removed .model(modelName) as the ChatClient is expected to be configured with the default model
        // from application.properties or its builder.
        String aiResponseContent = chatClient.prompt(prompt)
                .call()
                .content();

        // Retourne le contenu de la réponse de l'IA (qui devrait être une chaîne JSON)
        return aiResponseContent;
    }
}