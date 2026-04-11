package tn.esprit.spring.diacarebackend.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import tn.esprit.spring.diacarebackend.dto.PatientSignupRequest;
import tn.esprit.spring.diacarebackend.entities.Patient;
import tn.esprit.spring.diacarebackend.services.PatientService;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/patients")
@CrossOrigin(origins = "http://localhost:4200")
public class PatientController {

    private final PatientService service;

    public PatientController(PatientService service) {
        this.service = service;
    }

    @PostMapping("/signup")
    public ResponseEntity<?> signup(@RequestBody PatientSignupRequest request) {
        Patient patient = service.registerPatient(request);
        return ResponseEntity.ok(Map.of(
                "message", "Inscription réussie. Vérifiez votre email pour activer votre compte.",
                "patientId", patient.getId()
        ));
    }

    @GetMapping("/activate")
    public ResponseEntity<String> activate(@RequestParam("token") String token) {
        return ResponseEntity.ok(service.activateAccount(token));
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody Map<String, String> request) {
        return ResponseEntity.ok(service.login(
                request.get("email"),
                request.get("password")
        ));
    }

    @GetMapping("/all")
    public ResponseEntity<List<Patient>> getAllPatients() {
        return ResponseEntity.ok(service.getAllPatients());
    }

    // ── Nouveaux ───────────────────────────────────────────

    @GetMapping("/{id}")
    public ResponseEntity<?> getById(@PathVariable Long id) {
        return ResponseEntity.ok(service.getPatientById(id));
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> updateInfo(
            @PathVariable Long id,
            @RequestBody PatientSignupRequest request) {
        return ResponseEntity.ok(service.updateInfo(id, request));
    }

    @PutMapping("/{id}/sante")
    public ResponseEntity<?> updateSante(
            @PathVariable Long id,
            @RequestBody PatientSignupRequest request) {
        return ResponseEntity.ok(service.updateSante(id, request));
    }

    @PutMapping("/{id}/password")
    public ResponseEntity<?> changePassword(
            @PathVariable Long id,
            @RequestBody PatientSignupRequest request) {
        service.changePassword(id, request);
        return ResponseEntity.ok(Map.of("message", "Mot de passe modifié avec succès"));
    }

    @PostMapping("/{id}/upload-photo")
    public ResponseEntity<?> uploadPhoto(
            @PathVariable Long id,
            @RequestParam("file") MultipartFile file) {
        return ResponseEntity.ok(service.uploadPhoto(id, file));
    }
}