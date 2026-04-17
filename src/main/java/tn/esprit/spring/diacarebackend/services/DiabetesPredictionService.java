package tn.esprit.spring.diacarebackend.services;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.http.*;
import com.fasterxml.jackson.databind.ObjectMapper;
import tn.esprit.spring.diacarebackend.dto.DiabetesPredictionRequest;

import java.util.HashMap;
import java.util.Map;
import java.util.List;
import java.util.ArrayList;
@Service
public class DiabetesPredictionService {

    @Value("${prediction.api.url:http://localhost:5007}")
    private String predictionApiUrl;

    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;

    public DiabetesPredictionService() {
        this.restTemplate = new RestTemplate();
        this.objectMapper = new ObjectMapper();
    }

    public Map<String, Object> predict(DiabetesPredictionRequest request) {
        try {
            // Convertir le DTO en Map pour l'envoyer à Flask
            Map<String, Object> patientData = new HashMap<>();
            patientData.put("Age", request.getAge());
            patientData.put("Gender", request.getGender());
            patientData.put("Polyuria", request.getPolyuria());
            patientData.put("Polydipsia", request.getPolydipsia());
            patientData.put("sudden_weight_loss", request.getSudden_weight_loss());
            patientData.put("weakness", request.getWeakness());
            patientData.put("Polyphagia", request.getPolyphagia());
            patientData.put("Genital_thrush", request.getGenital_thrush());
            patientData.put("visual_blurring", request.getVisual_blurring());
            patientData.put("Itching", request.getItching());
            patientData.put("Irritability", request.getIrritability());
            patientData.put("delayed_healing", request.getDelayed_healing());
            patientData.put("partial_paresis", request.getPartial_paresis());
            patientData.put("muscle_stiffness", request.getMuscle_stiffness());
            patientData.put("Alopecia", request.getAlopecia());
            patientData.put("Obesity", request.getObesity());

            // Appel à l'API Flask
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            HttpEntity<Map<String, Object>> httpRequest = new HttpEntity<>(patientData, headers);

            ResponseEntity<Map> response = restTemplate.postForEntity(
                    predictionApiUrl + "/predict",
                    httpRequest,
                    Map.class
            );

            Map<String, Object> result = response.getBody();

            // Ajouter le niveau de risque et les recommandations
            if (result != null) {
                int prediction = (int) result.get("prediction");
                result.put("riskLevel", prediction == 1 ? "Élevé" : "Faible");
                result.put("recommendations", getRecommendations(prediction, request));
            }

            return result;

        } catch (Exception e) {
            Map<String, Object> error = new HashMap<>();
            error.put("error", e.getMessage());
            return error;
        }
    }

    private List<String> getRecommendations(int prediction, DiabetesPredictionRequest request) {
        List<String> recommendations = new ArrayList<>();

        if (prediction == 1) {
            recommendations.add("Consultez un médecin endocrinologue rapidement");
            recommendations.add("Surveillez votre glycémie régulièrement");
            recommendations.add("Adoptez une alimentation équilibrée pauvre en sucre");
            recommendations.add("Pratiquez une activité physique régulière");
        }

        if (request.getObesity() == 1) {
            recommendations.add("Perdez du poids progressivement (objectif: 5-10% de votre poids)");
        }

        if (request.getPolyuria() == 1 || request.getPolydipsia() == 1) {
            recommendations.add("Buvez de l'eau et évitez les boissons sucrées");
        }

        return recommendations;
    }
}