package tn.esprit.spring.diacarebackend.dto;

import lombok.Data;
import tn.esprit.spring.diacarebackend.entities.BloodType;
import tn.esprit.spring.diacarebackend.entities.DiabetesType;
import tn.esprit.spring.diacarebackend.entities.Gender;

import java.time.LocalDate;

@Data
public class PatientSignupRequest {
    private String firstName;
    private String lastName;
    private String email;
    private String phone;
    private String password;
    private LocalDate dateOfBirth;
    private Gender gender;
    private String address;
    private String city;
    private DiabetesType diabetesType;
    private BloodType bloodType;
    private Double weight;
    private Double height;
    private String emergencyContact;
    private String familyHistory;


    // ── Change password (réutilisé pour PUT /{id}/password) ──
    private String currentPassword;
    private String newPassword;

    private String hcaptchaToken;
}