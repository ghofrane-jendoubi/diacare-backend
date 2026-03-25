package tn.esprit.spring.diacarebackend.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import tn.esprit.spring.diacarebackend.entities.PatientNutritionProfile;
import tn.esprit.spring.diacarebackend.services.PatientNutritionProfileService;

@RestController
@RequestMapping("/api/nutrition-profile")
@CrossOrigin(origins = "*")
public class PatientNutritionProfileController {

    @Autowired
    private PatientNutritionProfileService service;

    // CREATE / UPDATE
    @PostMapping
    public PatientNutritionProfile save(
            @RequestBody PatientNutritionProfile profile) {
        return service.save(profile);
    }

    // READ
    @GetMapping("/patient/{patientId}")
    public PatientNutritionProfile get(
            @PathVariable Long patientId) {
        return service.getByPatientId(patientId);
    }

    // DELETE
    @DeleteMapping("/{id}")
    public void delete(@PathVariable Long id) {
        service.delete(id);
    }
}