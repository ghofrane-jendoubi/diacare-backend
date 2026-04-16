package tn.esprit.spring.diacarebackend.controller;

import tn.esprit.spring.diacarebackend.dto.PatientFeedbackDto;
import tn.esprit.spring.diacarebackend.services.EmotionFeedbackService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/doctor")
@CrossOrigin(origins = "http://localhost:4200")
public class DoctorFeedbackController {

    private final EmotionFeedbackService feedbackService;

    public DoctorFeedbackController(EmotionFeedbackService feedbackService) {
        this.feedbackService = feedbackService;
    }

    @GetMapping("/patients/feedbacks")
    public ResponseEntity<List<PatientFeedbackDto>> getPatientFeedbacks(
            @RequestParam(required = false) Long doctorId) {
        if (doctorId == null) {
            return ResponseEntity.badRequest().build();
        }
        return ResponseEntity.ok(feedbackService.getDoctorFeedbacks(doctorId));
    }
}
