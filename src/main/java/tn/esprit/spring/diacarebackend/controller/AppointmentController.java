package tn.esprit.spring.diacarebackend.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import tn.esprit.spring.diacarebackend.dto.AppointmentRequest;
import tn.esprit.spring.diacarebackend.entities.Appointment;
import tn.esprit.spring.diacarebackend.entities.User;
import tn.esprit.spring.diacarebackend.repository.AppointmentRepository;
import tn.esprit.spring.diacarebackend.repository.UserRepository;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/appointments")
@CrossOrigin(origins = "http://localhost:4200")
public class AppointmentController {

    @Autowired
    private AppointmentRepository appointmentRepository;

    @Autowired
    private UserRepository userRepository;

    @GetMapping("/doctor/{doctorId}")
    public ResponseEntity<List<Appointment>> getDoctorAppointments(@PathVariable Long doctorId) {
        List<Appointment> appointments = appointmentRepository.findByDoctorId(doctorId);
        return ResponseEntity.ok(appointments);
    }

    @GetMapping("/doctor/{doctorId}/patients")
    public ResponseEntity<List<User>> getPatientsWithConversations(@PathVariable Long doctorId) {
        List<User> patients = appointmentRepository.findPatientsWithConversations(doctorId);
        return ResponseEntity.ok(patients);
    }

    @PostMapping("/create")
    public ResponseEntity<Appointment> createAppointment(@RequestBody AppointmentRequest request) {
        User doctor = userRepository.findById(request.getDoctorId())
                .orElseThrow(() -> new RuntimeException("Doctor not found"));
        User patient = userRepository.findById(request.getPatientId())
                .orElseThrow(() -> new RuntimeException("Patient not found"));

        Appointment appointment = new Appointment();
        appointment.setDoctor(doctor);
        appointment.setPatient(patient);
        appointment.setPatientName(request.getPatientName());
        appointment.setTitle(request.getTitle());
        appointment.setStartTime(request.getStart());
        appointment.setEndTime(request.getEnd());
        appointment.setDescription(request.getDescription());
        appointment.setType(request.getType());
        appointment.setMeetLink(request.getMeetLink());
        appointment.setStatus(request.getStatus());
        appointment.setPaid(false);
        appointment.setFee(request.getFee());

        Appointment saved = appointmentRepository.save(appointment);
        return ResponseEntity.ok(saved);
    }
    @GetMapping("/{id}")
    public ResponseEntity<Appointment> getAppointmentById(@PathVariable Long id) {
        return appointmentRepository.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PutMapping("/{id}")
    public ResponseEntity<Appointment> updateAppointment(
            @PathVariable Long id,
            @RequestBody AppointmentRequest request) {

        Appointment appointment = appointmentRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Appointment not found"));

        if (request.getTitle() != null)
            appointment.setTitle(request.getTitle());

        if (request.getStart() != null)
            appointment.setStartTime(request.getStart());

        if (request.getEnd() != null)
            appointment.setEndTime(request.getEnd());

        if (request.getDescription() != null)
            appointment.setDescription(request.getDescription());

        if (request.getType() != null)
            appointment.setType(request.getType());

        if (request.getMeetLink() != null)
            appointment.setMeetLink(request.getMeetLink());

        if (request.getStatus() != null)
            appointment.setStatus(request.getStatus());

        if (request.getFee() != null)
            appointment.setFee(request.getFee());

        Appointment updated = appointmentRepository.save(appointment);
        return ResponseEntity.ok(updated);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Map<String, String>> deleteAppointment(@PathVariable Long id) {
        appointmentRepository.deleteById(id);
        Map<String, String> response = new HashMap<>();
        response.put("message", "Appointment deleted successfully");
        return ResponseEntity.ok(response);
    }
}

