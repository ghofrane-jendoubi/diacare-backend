package tn.esprit.spring.diacarebackend.patient.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import tn.esprit.spring.diacarebackend.patient.dto.PatientSignupRequest;
import tn.esprit.spring.diacarebackend.patient.entity.Patient;
import tn.esprit.spring.diacarebackend.patient.service.PatientService;

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
}