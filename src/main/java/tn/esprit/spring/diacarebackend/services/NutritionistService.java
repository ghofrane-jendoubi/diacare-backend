// NutritionistService.java
package tn.esprit.spring.diacarebackend.services;

import org.springframework.stereotype.Service;
import tn.esprit.spring.diacarebackend.entities.Nutritionist;
import tn.esprit.spring.diacarebackend.repository.NutritionistRepository;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@Service
public class NutritionistService {

    private final NutritionistRepository nutritionistRepository;

    public NutritionistService(NutritionistRepository nutritionistRepository) {
        this.nutritionistRepository = nutritionistRepository;
    }

    public Map<String, Object> getNutritionistById(Long id) {
        Optional<Nutritionist> nutritionistOpt = nutritionistRepository.findById(id);
        if (nutritionistOpt.isEmpty()) {
            throw new RuntimeException("Nutritionniste non trouvé avec l'ID: " + id);
        }

        Nutritionist nutritionist = nutritionistOpt.get();
        Map<String, Object> response = new HashMap<>();
        response.put("id", nutritionist.getId());
        response.put("firstName", nutritionist.getFirstName());
        response.put("lastName", nutritionist.getLastName());
        response.put("email", nutritionist.getEmail());
        response.put("phone", nutritionist.getPhone());

        return response;
    }
}