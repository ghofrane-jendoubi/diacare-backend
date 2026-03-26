package tn.esprit.spring.diacarebackend.Service;

import tn.esprit.spring.diacarebackend.entities.GlycemieRecord;
import tn.esprit.spring.diacarebackend.repository.GlycemieRecordRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class IaDashboardService {

    @Value("${gemini.api.key:demo}")
    private String geminiKey;

    private final GlycemieRecordRepository glycemieRepo;
    private final RestTemplate restTemplate = new RestTemplate();

    public IaDashboardService(GlycemieRecordRepository glycemieRepo) {
        this.glycemieRepo = glycemieRepo;
    }

    // ===== AJOUTER UNE MESURE =====
    @Transactional
    public GlycemieRecord addMesure(Long patientId, Double valeur, String moment, String notes) {
        GlycemieRecord record = new GlycemieRecord();
        record.setPatientId(patientId);
        record.setValeur(valeur);
        record.setMoment(GlycemieRecord.Moment.valueOf(moment));
        record.setNotes(notes);
        record.setMeasuredAt(LocalDateTime.now());  // <-- AJOUT OBLIGATOIRE
        return glycemieRepo.save(record);
    }

    // ===== HISTORIQUE =====
    public List<GlycemieRecord> getHistorique(Long patientId) {
        return glycemieRepo.findByPatientIdOrderByMeasuredAtDesc(patientId);
    }

    // ===== DASHBOARD COMPLET =====
    public Map<String, Object> getDashboard(Long patientId) {
        List<GlycemieRecord> records = glycemieRepo
                .findLast10ByPatientId(patientId, PageRequest.of(0, 10));

        Map<String, Object> dashboard = new HashMap<>();

        // Stats de base
        dashboard.put("totalMesures", records.size());

        // Mapper l'historique avec gestion du null
        dashboard.put("historique", records.stream().map(r -> {
            Map<String, Object> map = new HashMap<>();
            map.put("id", r.getId());
            map.put("valeur", r.getValeur());
            map.put("moment", r.getMoment().name());
            // Si measuredAt est null, on utilise la date courante
            map.put("date", r.getMeasuredAt() != null ? r.getMeasuredAt().toString() : LocalDateTime.now().toString());
            return map;
        }).collect(Collectors.toList()));

        if (records.isEmpty()) {
            dashboard.put("scoresSante", getScoreDefaut());
            dashboard.put("alertes", List.of());
            dashboard.put("recommandations", List.of(
                    "Commencez par enregistrer votre première mesure de glycémie"
            ));
            dashboard.put("prediction", Map.of(
                    "tendance", "indisponible",
                    "risque", "non calculé"
            ));
            return dashboard;
        }

        // Calculs statistiques
        double moyenne = records.stream()
                .mapToDouble(GlycemieRecord::getValeur).average().orElse(0);
        double max = records.stream()
                .mapToDouble(GlycemieRecord::getValeur).max().orElse(0);
        double min = records.stream()
                .mapToDouble(GlycemieRecord::getValeur).min().orElse(0);
        double derniere = records.get(0).getValeur();

        dashboard.put("stats", Map.of(
                "moyenne", Math.round(moyenne * 100.0) / 100.0,
                "max", max,
                "min", min,
                "derniere", derniere
        ));

        // Score de santé calculé localement
        dashboard.put("scoresSante", calculerScores(records, moyenne));

        // Alertes intelligentes
        dashboard.put("alertes", genererAlertes(records, moyenne, derniere));

        // Prédiction IA via Gemini
        dashboard.put("prediction", genererPrediction(records, moyenne));

        // Recommandations IA
        dashboard.put("recommandations", genererRecommandations(records, moyenne));

        return dashboard;
    }

    // ===== SCORES DE SANTÉ =====
    private Map<String, Object> calculerScores(List<GlycemieRecord> records, double moyenne) {
        int scoreControle;
        if (moyenne >= 0.7 && moyenne <= 1.3) scoreControle = 90;
        else if (moyenne >= 0.6 && moyenne <= 1.6) scoreControle = 70;
        else if (moyenne >= 0.5 && moyenne <= 2.0) scoreControle = 50;
        else scoreControle = 25;

        int scoreRegularite = Math.min(100, records.size() * 10);
        int scoreGlobal = (scoreControle + scoreRegularite) / 2;

        String niveauRisque;
        String couleurRisque;
        if (scoreGlobal >= 75) { niveauRisque = "FAIBLE"; couleurRisque = "#16a34a"; }
        else if (scoreGlobal >= 50) { niveauRisque = "MODÉRÉ"; couleurRisque = "#f59e0b"; }
        else { niveauRisque = "ÉLEVÉ"; couleurRisque = "#dc2626"; }

        return Map.of(
                "global", scoreGlobal,
                "controleGlycemique", scoreControle,
                "regularite", scoreRegularite,
                "niveauRisque", niveauRisque,
                "couleurRisque", couleurRisque
        );
    }

    // ===== ALERTES INTELLIGENTES =====
    private List<Map<String, Object>> genererAlertes(
            List<GlycemieRecord> records, double moyenne, double derniere) {
        List<Map<String, Object>> alertes = new ArrayList<>();

        if (derniere > 2.5) {
            alertes.add(Map.of(
                    "type", "DANGER",
                    "emoji", "🚨",
                    "titre", "Hyperglycémie sévère",
                    "message", String.format("Dernière mesure : %.2f g/L — Contactez votre médecin", derniere),
                    "couleur", "#dc2626"
            ));
        } else if (derniere > 1.8) {
            alertes.add(Map.of(
                    "type", "WARNING",
                    "emoji", "⚠️",
                    "titre", "Glycémie élevée",
                    "message", String.format("Dernière mesure : %.2f g/L — Limitez les glucides", derniere),
                    "couleur", "#f59e0b"
            ));
        } else if (derniere < 0.7) {
            alertes.add(Map.of(
                    "type", "DANGER",
                    "emoji", "🩸",
                    "titre", "Hypoglycémie détectée",
                    "message", "Prenez du sucre rapidement — 3 morceaux ou jus de fruit",
                    "couleur", "#7c3aed"
            ));
        }

        if (moyenne > 1.5) {
            alertes.add(Map.of(
                    "type", "INFO",
                    "emoji", "📊",
                    "titre", "Moyenne élevée sur la période",
                    "message", String.format("Moyenne : %.2f g/L — Consultez votre nutritionniste", moyenne),
                    "couleur", "#2563eb"
            ));
        }

        if (records.size() < 3) {
            alertes.add(Map.of(
                    "type", "INFO",
                    "emoji", "📝",
                    "titre", "Données insuffisantes",
                    "message", "Enregistrez plus de mesures pour des prédictions précises",
                    "couleur", "#0891b2"
            ));
        }

        return alertes;
    }

    // ===== PRÉDICTION TENDANCE =====
    private Map<String, Object> genererPrediction(
            List<GlycemieRecord> records, double moyenne) {
        if (records.size() < 3) {
            return Map.of(
                    "tendance", "INSUFFISANT",
                    "message", "Enregistrez au moins 3 mesures",
                    "couleur", "#64748b"
            );
        }

        double recent = records.subList(0, Math.min(3, records.size()))
                .stream().mapToDouble(GlycemieRecord::getValeur).average().orElse(0);
        double ancien = records.subList(Math.max(0, records.size() - 3), records.size())
                .stream().mapToDouble(GlycemieRecord::getValeur).average().orElse(0);

        String tendance;
        String couleur;
        String message;
        String emoji;

        double diff = recent - ancien;
        if (Math.abs(diff) < 0.1) {
            tendance = "STABLE"; emoji = "➡️";
            couleur = "#16a34a";
            message = "Votre glycémie est stable — Continuez vos efforts !";
        } else if (diff > 0) {
            tendance = "HAUSSE"; emoji = "📈";
            couleur = "#f59e0b";
            message = String.format("Tendance à la hausse (+%.2f g/L) — Surveillez votre alimentation", diff);
        } else {
            tendance = "BAISSE"; emoji = "📉";
            couleur = diff < -0.3 ? "#dc2626" : "#16a34a";
            message = diff < -0.3
                    ? "Baisse importante — Attention à l'hypoglycémie"
                    : "Tendance à la baisse — Bonne évolution !";
        }

        String geminiInsight = callGeminiPrediction(moyenne, tendance);

        return Map.of(
                "tendance", tendance,
                "emoji", emoji,
                "couleur", couleur,
                "message", message,
                "insight", geminiInsight,
                "differenceGlycemie", Math.round(diff * 100.0) / 100.0
        );
    }

    // ===== RECOMMANDATIONS GEMINI =====
    private List<String> genererRecommandations(
            List<GlycemieRecord> records, double moyenne) {

        String prompt = String.format("""
            Patient diabétique. Données glycémiques récentes:
            - Moyenne glycémie: %.2f g/L
            - Nombre de mesures: %d
            - Dernière valeur: %.2f g/L
            
            Donne exactement 4 recommandations courtes et pratiques en français.
            Format JSON: {"recommandations": ["conseil 1", "conseil 2", "conseil 3", "conseil 4"]}
            Réponds UNIQUEMENT avec le JSON.
            """,
                moyenne,
                records.size(),
                records.isEmpty() ? 0 : records.get(0).getValeur()
        );

        try {
            String response = callGemini(prompt);
            response = response.replaceAll("```json", "").replaceAll("```", "").trim();
            com.fasterxml.jackson.databind.ObjectMapper mapper = new com.fasterxml.jackson.databind.ObjectMapper();
            Map<String, Object> parsed = mapper.readValue(response, Map.class);
            return (List<String>) parsed.get("recommandations");
        } catch (Exception e) {
            return List.of(
                    "Mesurez votre glycémie 3 à 4 fois par jour",
                    "Évitez les sucres rapides entre les repas",
                    "Marchez 30 minutes après chaque repas principal",
                    "Consultez votre médecin si la glycémie dépasse 2 g/L"
            );
        }
    }

    // ===== APPEL GEMINI =====
    private String callGemini(String prompt) {
        if ("demo".equals(geminiKey)) return "{}";
        try {
            String url = "https://generativelanguage.googleapis.com/v1beta/" +
                    "models/gemini-pro:generateContent?key=" + geminiKey;

            Map<String, Object> request = new HashMap<>();
            request.put("contents", List.of(Map.of(
                    "parts", List.of(Map.of("text", prompt))
            )));
            request.put("generationConfig", Map.of(
                    "temperature", 0.5, "maxOutputTokens", 500
            ));

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);

            ResponseEntity<Map> response = restTemplate.postForEntity(
                    url, new HttpEntity<>(request, headers), Map.class);

            List<Map> candidates = (List<Map>) response.getBody().get("candidates");
            Map content = (Map) candidates.get(0).get("content");
            List<Map> parts = (List<Map>) content.get("parts");
            return (String) parts.get(0).get("text");

        } catch (Exception e) {
            System.err.println("Erreur Gemini: " + e.getMessage());
            return "{}";
        }
    }

    private String callGeminiPrediction(double moyenne, String tendance) {
        String prompt = String.format("""
            Glycémie moyenne: %.2f g/L, tendance: %s.
            Donne UN conseil court (max 20 mots) en français pour améliorer cette tendance.
            Réponds uniquement avec le texte du conseil.
            """, moyenne, tendance);
        try {
            String result = callGemini(prompt);
            return result.isBlank() ? "Continuez à surveiller régulièrement." : result.trim();
        } catch (Exception e) {
            return "Continuez à surveiller régulièrement.";
        }
    }

    private Map<String, Object> getScoreDefaut() {
        return Map.of(
                "global", 0,
                "controleGlycemique", 0,
                "regularite", 0,
                "niveauRisque", "NON CALCULÉ",
                "couleurRisque", "#64748b"
        );
    }
}