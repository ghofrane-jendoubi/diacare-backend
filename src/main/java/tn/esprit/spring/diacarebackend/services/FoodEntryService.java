package tn.esprit.spring.diacarebackend.services;

import org.springframework.stereotype.Service;
import tn.esprit.spring.diacarebackend.entities.FoodEntry;
import tn.esprit.spring.diacarebackend.entities.User;
import tn.esprit.spring.diacarebackend.repository.FoodEntryRepository;
import tn.esprit.spring.diacarebackend.repository.UserRepository;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class FoodEntryService {

    private final FoodEntryRepository repo;
    private final AIService aiService;
    private final UserRepository userRepository;

    public FoodEntryService(FoodEntryRepository repo,
                            AIService aiService,
                            UserRepository userRepository) {
        this.repo = repo;
        this.aiService = aiService;
        this.userRepository = userRepository;
    }

    // ✅ Ajouter entrée + ML
    public FoodEntry addEntry(String text, Long patientId) {

        User patient = userRepository.findById(patientId)
                .orElseThrow(() -> new RuntimeException("User not found with id: " + patientId));

        FoodEntry entry = new FoodEntry();
        entry.setText(text);
        entry.setPatient(patient);

        // Appel ML (retourne déjà le JSON avec tous les calculs)
        String result = aiService.analyzeFood(text);
        entry.setAnalysisResult(result);

        return repo.save(entry);
    }

    // ✅ Récupérer les entrées d'aujourd'hui
    public List<FoodEntry> getTodayEntries(Long patientId) {
        LocalDateTime startOfDay = LocalDateTime.now().withHour(0).withMinute(0).withSecond(0);
        LocalDateTime endOfDay = LocalDateTime.now().withHour(23).withMinute(59).withSecond(59);
        return repo.findByPatientIdAndCreatedAtBetween(patientId, startOfDay, endOfDay);
    }

    // ✅ Récupérer par patient (trié par date décroissante)
    public List<FoodEntry> getByPatientId(Long patientId) {
        return repo.findByPatientIdOrderByCreatedAtDesc(patientId);
    }

    // ✅ Afficher tous
    public List<FoodEntry> getAll() {
        return repo.findAll();
    }

    // ✅ Supprimer par ID
    public void deleteById(Long id) {
        if (!repo.existsById(id)) {
            throw new RuntimeException("FoodEntry not found with id " + id);
        }
        repo.deleteById(id);
    }
}