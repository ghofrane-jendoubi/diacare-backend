package tn.esprit.spring.diacarebackend.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import tn.esprit.spring.diacarebackend.dto.AdminSignupRequest;
import tn.esprit.spring.diacarebackend.entities.Admin;
import tn.esprit.spring.diacarebackend.services.AdminSignupService;
import tn.esprit.spring.diacarebackend.services.DoctorSignupService;

import java.util.Map;

@RestController
@RequestMapping("/api/admins")
@CrossOrigin(origins = "http://localhost:4200")
public class AdminSignupController {

    private final AdminSignupService signupService;
    private final DoctorSignupService doctorService;

    public AdminSignupController(AdminSignupService signupService,
                                 DoctorSignupService doctorService) {
        this.signupService = signupService;
        this.doctorService = doctorService;
    }

    // ================== ADMIN SIGNUP / LOGIN ==================
    @PostMapping("/signup")
    public ResponseEntity<?> signup(@RequestBody AdminSignupRequest request) {
        Admin admin = signupService.registerAdmin(request);
        return ResponseEntity.ok(admin);
    }

    @GetMapping("/activate")
    public ResponseEntity<String> activate(@RequestParam("token") String token) {
        String result = signupService.activateAccount(token);
        return ResponseEntity.ok(result);
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody Map<String, String> request) {
        String email = request.get("email");
        String password = request.get("password");
        Map<String, Object> response = signupService.login(email, password);
        return ResponseEntity.ok(response);
    }

    // ================== DOCTOR APPROVAL ==================
    // Ici tu peux ajouter les endpoints pour valider un docteur
}