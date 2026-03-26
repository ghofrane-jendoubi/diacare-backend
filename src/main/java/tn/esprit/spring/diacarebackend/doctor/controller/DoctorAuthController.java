package tn.esprit.spring.diacarebackend.doctor.controller;

import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import tn.esprit.spring.diacarebackend.doctor.dto.DoctorSignupRequest;
import tn.esprit.spring.diacarebackend.doctor.entity.CertificateStatus;
import tn.esprit.spring.diacarebackend.doctor.entity.Doctor;
import tn.esprit.spring.diacarebackend.doctor.service.DoctorSignupService;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/doctors")
@CrossOrigin(origins = "http://localhost:4200")
public class DoctorAuthController {

    private final DoctorSignupService signupService;

    public DoctorAuthController(DoctorSignupService signupService) {
        this.signupService = signupService;
    }

    // POST multipart/form-data : data (JSON) + certificate (image)
    @PostMapping(value = "/signup", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> signup(
            @RequestPart("data") DoctorSignupRequest request,
            @RequestPart("certificate") MultipartFile certificateImage
    ) {
        Doctor doctor = signupService.registerDoctor(request, certificateImage);
        return ResponseEntity.ok(Map.of(
                "message", "Inscription réussie. Vérifiez votre email pour activer votre compte.",
                "doctorId", doctor.getId()
        ));
    }

    @GetMapping("/activate")
    public ResponseEntity<String> activate(@RequestParam("token") String token) {
        return ResponseEntity.ok(signupService.activateAccount(token));
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody Map<String, String> request) {
        return ResponseEntity.ok(signupService.login(request.get("email"), request.get("password")));
    }

    // ===== ENDPOINTS ADMIN =====

    @PutMapping("/certificate/approve/{doctorId}")
    public ResponseEntity<?> approve(@PathVariable Long doctorId) {
        signupService.approveCertificate(doctorId);
        return ResponseEntity.ok(Map.of("message", "Certificat approuvé"));
    }

    @PutMapping("/certificate/reject/{doctorId}")
    public ResponseEntity<?> reject(@PathVariable Long doctorId) {
        signupService.rejectCertificate(doctorId);
        return ResponseEntity.ok(Map.of("message", "Certificat rejeté"));
    }

    @GetMapping("/pending")
    public ResponseEntity<List<Doctor>> getPending() {
        return ResponseEntity.ok(signupService.getDoctorsByStatus(CertificateStatus.PENDING));
    }
    @GetMapping("/all")
    public ResponseEntity<List<Doctor>> getAllDoctors() {
        return ResponseEntity.ok(signupService.getAllDoctors());
    }

    @GetMapping("/{id}")
    public ResponseEntity<Doctor> getDoctorById(@PathVariable Long id) {
        return ResponseEntity.ok(signupService.getDoctorById(id));
    }
}