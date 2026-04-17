package tn.esprit.spring.diacarebackend.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import tn.esprit.spring.diacarebackend.entities.Doctor;
import tn.esprit.spring.diacarebackend.repository.DoctorRepository;

import java.util.List;

@RestController
@RequestMapping("/api/doctors")
@CrossOrigin(origins = "http://localhost:4200")
public class DoctorController {

    @Autowired
    private DoctorRepository DoctorRepository;

    // Récupérer tous les médecins vérifiés (pour les patients)
    @GetMapping
    public ResponseEntity<List<Doctor>> getAllDoctors() {
        List<Doctor> doctors = DoctorRepository.findAllVerifiedDoctorsForDisplay();
        return ResponseEntity.ok(doctors);
    }

    // Récupérer un médecin par son ID
    @GetMapping("/{id}")
    public ResponseEntity<Doctor> getDoctorById(@PathVariable Long id) {
        return DoctorRepository.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
}