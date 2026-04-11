package tn.esprit.spring.diacarebackend.controller;

import com.stripe.Stripe;
import com.stripe.model.PaymentIntent;
import com.stripe.param.PaymentIntentCreateParams;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import tn.esprit.spring.diacarebackend.entities.Appointment;
import tn.esprit.spring.diacarebackend.entities.Payment;
import tn.esprit.spring.diacarebackend.repository.AppointmentRepository;
import tn.esprit.spring.diacarebackend.repository.PaymentRepository;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/payments")
@CrossOrigin(origins = "http://localhost:4200")
public class PaymentController {

    @Autowired
    private PaymentRepository paymentRepository;

    @Autowired
    private AppointmentRepository appointmentRepository;

    @Value("${stripe.secret.key}")
    private String stripeSecretKey;

    @PostMapping("/create-intent")
    public ResponseEntity<Map<String, Object>> createPaymentIntent(@RequestBody Map<String, Object> request) {
        try {
            Long appointmentId = Long.valueOf(request.get("appointmentId").toString());
            Long patientId = Long.valueOf(request.get("patientId").toString());

            System.out.println("🔍 Création du PaymentIntent pour appointmentId: " + appointmentId + ", patientId: " + patientId);

            Appointment appointment = appointmentRepository.findById(appointmentId)
                    .orElseThrow(() -> new RuntimeException("Rendez-vous non trouvé"));

            // Configurer Stripe avec la clé secrète
            Stripe.apiKey = stripeSecretKey;


            long amountInMillimes = (long) (appointment.getFee() * 1000);

            System.out.println("💰 Montant: " + appointment.getFee() + " TND = " + amountInMillimes + " millimes");

            // Créer le PaymentIntent
            PaymentIntentCreateParams params = PaymentIntentCreateParams.builder()
                    .setAmount(amountInMillimes)
                    .setCurrency("eur")
                    .putMetadata("appointmentId", appointmentId.toString())
                    .putMetadata("patientId", patientId.toString())
                    .build();

            PaymentIntent paymentIntent = PaymentIntent.create(params);

            System.out.println("✅ PaymentIntent créé: " + paymentIntent.getId());

            // Enregistrer le PaymentIntent en base de données
            Payment payment = new Payment();
            payment.setPaymentIntentId(paymentIntent.getId());
            payment.setAppointmentId(appointmentId);
            payment.setPatientId(patientId);
            payment.setAmount(appointment.getFee());
            payment.setStatus(paymentIntent.getStatus());
            payment.setCreatedAt(LocalDateTime.now());
            paymentRepository.save(payment);

            Map<String, Object> response = new HashMap<>();
            response.put("clientSecret", paymentIntent.getClientSecret());
            response.put("paymentIntentId", paymentIntent.getId());
            response.put("amount", appointment.getFee());

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            e.printStackTrace();
            System.err.println("❌ Erreur création PaymentIntent: " + e.getMessage());
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(errorResponse);
        }
    }

    // PaymentController.java - méthode confirmPayment
    @PostMapping("/confirm")
    public ResponseEntity<Map<String, Object>> confirmPayment(@RequestBody Map<String, Object> request) {
        try {
            String paymentIntentId = request.get("paymentIntentId").toString();
            String status = request.get("status").toString();

            System.out.println("🔍 Confirmation paiement: " + paymentIntentId + ", status: " + status);

            Payment payment = paymentRepository.findByPaymentIntentId(paymentIntentId)
                    .orElseThrow(() -> new RuntimeException("Payment non trouvé"));

            payment.setStatus(status);
            paymentRepository.save(payment);

            // ✅ Mettre à jour le rendez-vous comme payé
            if ("SUCCEEDED".equals(status)) {
                Appointment appointment = appointmentRepository.findById(payment.getAppointmentId())
                        .orElseThrow(() -> new RuntimeException("Rendez-vous non trouvé"));

                appointment.setPaid(true);
                appointmentRepository.save(appointment);

                System.out.println("✅ Rendez-vous #" + appointment.getId() + " marqué comme payé");
            }

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Paiement confirmé avec succès");

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            e.printStackTrace();
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(errorResponse);
        }
    }
    @GetMapping("/paid/{appointmentId}")
    public ResponseEntity<Map<String, Boolean>> isAppointmentPaid(@PathVariable Long appointmentId) {
        try {
            boolean isPaid = paymentRepository.existsByAppointmentIdAndStatus(appointmentId, "SUCCEEDED");
            System.out.println("🔍 Vérification paiement appointment " + appointmentId + ": " + isPaid);
            return ResponseEntity.ok(Map.of("isPaid", isPaid));
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.ok(Map.of("isPaid", false));
        }
    }

    @GetMapping("/check-access/{appointmentId}/{patientId}")
    public ResponseEntity<Map<String, Boolean>> checkMeetAccess(
            @PathVariable Long appointmentId,
            @PathVariable Long patientId) {
        try {
            boolean canAccess = paymentRepository.existsByAppointmentIdAndStatus(appointmentId, "SUCCEEDED");
            System.out.println("🔍 Vérification accès appointment " + appointmentId + ": " + canAccess);
            return ResponseEntity.ok(Map.of("canAccess", canAccess));
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.ok(Map.of("canAccess", false));
        }
    }
}