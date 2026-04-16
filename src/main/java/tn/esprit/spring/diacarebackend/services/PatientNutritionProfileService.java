package tn.esprit.spring.diacarebackend.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import tn.esprit.spring.diacarebackend.entities.PatientNutritionProfile;
import tn.esprit.spring.diacarebackend.repository.PatientNutritionProfileRepository;

@Service
public class PatientNutritionProfileService {

    @Autowired
    private PatientNutritionProfileRepository repository;

    public PatientNutritionProfile save(PatientNutritionProfile profile) {
        return repository.save(profile);
    }

    public PatientNutritionProfile getByPatientId(Long patientId) {
        return repository.findByPatientId(patientId)
                .orElse(new PatientNutritionProfile());
    }

    public void delete(Long id) {
        repository.deleteById(id);
    }
}