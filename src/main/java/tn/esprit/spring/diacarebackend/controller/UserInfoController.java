// UserInfoController.java - Endpoint unique pour tous les utilisateurs
package tn.esprit.spring.diacarebackend.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import tn.esprit.spring.diacarebackend.entities.*;
import tn.esprit.spring.diacarebackend.repository.*;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/user-info")
@CrossOrigin(origins = "http://localhost:4200")
public class UserInfoController {

    private final PatientRepository patientRepository;
    private final DoctorRepository doctorRepository;
    private final NutritionistRepository nutritionistRepository;

    public UserInfoController(PatientRepository patientRepository,
                              DoctorRepository doctorRepository,
                              NutritionistRepository nutritionistRepository) {
        this.patientRepository = patientRepository;
        this.doctorRepository = doctorRepository;
        this.nutritionistRepository = nutritionistRepository;
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getUserInfo(@PathVariable Long id) {
        Map<String, Object> response = new HashMap<>();

        // Chercher dans les patients
        Optional<Patient> patient = patientRepository.findById(id);
        if (patient.isPresent()) {
            response.put("id", patient.get().getId());
            response.put("firstName", patient.get().getFirstName());
            response.put("lastName", patient.get().getLastName());
            response.put("email", patient.get().getEmail());
            response.put("role", "PATIENT");
            return ResponseEntity.ok(response);
        }

        // Chercher dans les docteurs
        Optional<Doctor> doctor = doctorRepository.findById(id);
        if (doctor.isPresent()) {
            response.put("id", doctor.get().getId());
            response.put("firstName", doctor.get().getFirstName());
            response.put("lastName", doctor.get().getLastName());
            response.put("email", doctor.get().getEmail());
            response.put("role", "DOCTOR");
            return ResponseEntity.ok(response);
        }

        // Chercher dans les nutritionnistes
        Optional<Nutritionist> nutritionist = nutritionistRepository.findById(id);
        if (nutritionist.isPresent()) {
            response.put("id", nutritionist.get().getId());
            response.put("firstName", nutritionist.get().getFirstName());
            response.put("lastName", nutritionist.get().getLastName());
            response.put("email", nutritionist.get().getEmail());
            response.put("role", "NUTRITIONIST");
            return ResponseEntity.ok(response);
        }

        return ResponseEntity.notFound().build();
    }
}