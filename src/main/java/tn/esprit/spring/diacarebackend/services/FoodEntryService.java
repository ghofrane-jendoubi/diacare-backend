package tn.esprit.spring.diacarebackend.services;

import org.springframework.stereotype.Service;
import tn.esprit.spring.diacarebackend.entities.FoodEntry;
import tn.esprit.spring.diacarebackend.entities.User;
import tn.esprit.spring.diacarebackend.repository.FoodEntryRepository;
import tn.esprit.spring.diacarebackend.repository.UserRepository;

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

    // ✅ ajouter entrée + ML
    public FoodEntry addEntry(String text, Long patientId) {

        User patient = userRepository.findById(patientId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        FoodEntry entry = new FoodEntry();
        entry.setText(text);
        entry.setPatient(patient);

        // appel ML
        String result = aiService.analyzeFood(text);
        entry.setAnalysisResult(result);

        return repo.save(entry);
    }

    // ✅ afficher
    public List<FoodEntry> getAll() {
        return repo.findAll();
    }
}