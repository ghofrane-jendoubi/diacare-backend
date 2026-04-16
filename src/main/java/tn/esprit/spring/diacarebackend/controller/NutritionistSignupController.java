package tn.esprit.spring.diacarebackend.controller;

import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import tn.esprit.spring.diacarebackend.entities.CertificateStatus;
import tn.esprit.spring.diacarebackend.dto.NutritionistSignupRequest;
import tn.esprit.spring.diacarebackend.entities.Nutritionist;
import tn.esprit.spring.diacarebackend.services.NutritionistSignupService;
import tn.esprit.spring.diacarebackend.services.PasswordResetService;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/nutritionists")
@CrossOrigin(origins = "http://localhost:4200")
public class NutritionistSignupController {

    private final NutritionistSignupService service;
    private final PasswordResetService passwordResetService;

    public NutritionistSignupController(NutritionistSignupService service,
                                        PasswordResetService passwordResetService) {
        this.service = service;
        this.passwordResetService = passwordResetService;
    }

    @PostMapping(value = "/signup", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> signup(
            @RequestPart("data") NutritionistSignupRequest request,
            @RequestPart("certificate") MultipartFile certificateImage
    ) {
        Nutritionist n = service.registerNutritionist(request, certificateImage);
        return ResponseEntity.ok(Map.of(
                "message", "Inscription réussie. Vérifiez votre email pour activer votre compte.",
                "nutritionistId", n.getId()
        ));
    }

    @GetMapping("/activate")
    public ResponseEntity<String> activate(@RequestParam("token") String token) {
        return ResponseEntity.ok(service.activateAccount(token));
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody Map<String, String> request) {
        return ResponseEntity.ok(service.login(request.get("email"), request.get("password")));
    }

    @PutMapping("/certificate/approve/{id}")
    public ResponseEntity<?> approve(@PathVariable Long id) {
        service.approveCertificate(id);
        return ResponseEntity.ok(Map.of("message", "Certificat approuvé"));
    }

    @PutMapping("/certificate/reject/{id}")
    public ResponseEntity<?> reject(@PathVariable Long id) {
        service.rejectCertificate(id);
        return ResponseEntity.ok(Map.of("message", "Certificat rejeté"));
    }

    @GetMapping("/pending")
    public ResponseEntity<List<Nutritionist>> getPending() {
        return ResponseEntity.ok(service.getByStatus(CertificateStatus.PENDING));
    }

    @GetMapping("/all")
    public ResponseEntity<List<Nutritionist>> getAllNutritionists() {
        return ResponseEntity.ok(service.getAllNutritionists());
    }

    @GetMapping("/{id}")
    public ResponseEntity<Nutritionist> getNutritionistById(@PathVariable Long id) {
        return ResponseEntity.ok(service.getNutritionistById(id));
    }

    @PostMapping("/forgot-password")
    public ResponseEntity<?> forgotPassword(@RequestBody Map<String, String> request) {
        passwordResetService.sendResetCode(request.get("email"));
        return ResponseEntity.ok(Map.of("message", "Code envoyé à votre email"));
    }

    @PostMapping("/verify-reset-code")
    public ResponseEntity<?> verifyResetCode(@RequestBody Map<String, String> request) {
        passwordResetService.verifyCode(request.get("email"), request.get("code"));
        return ResponseEntity.ok(Map.of("message", "Code valide"));
    }

    @PostMapping("/reset-password")
    public ResponseEntity<?> resetPassword(@RequestBody Map<String, String> request) {
        passwordResetService.resetPassword(
                request.get("email"),
                request.get("code"),
                request.get("newPassword")
        );
        return ResponseEntity.ok(Map.of("message", "Mot de passe réinitialisé avec succès"));
    }
}