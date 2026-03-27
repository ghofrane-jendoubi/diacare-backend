package tn.esprit.spring.diacarebackend.dto;

import lombok.Data;
import tn.esprit.spring.diacarebackend.entities.Speciality;

@Data
public class DoctorSignupRequest {
    private String firstName;
    private String lastName;
    private String email;
    private String phone;
    private String password;
    private Speciality speciality;
    private String licenseNumber;
    private Integer yearsOfExperience;
    private Double consultationFee;
    private String hospital;
}