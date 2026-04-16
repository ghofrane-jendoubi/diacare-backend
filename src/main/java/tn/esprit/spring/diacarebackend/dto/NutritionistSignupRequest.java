package tn.esprit.spring.diacarebackend.dto;

import lombok.Data;

@Data
public class NutritionistSignupRequest {
    private String firstName;
    private String lastName;
    private String email;
    private String phone;
    private String password;
    private String licenseNumber;
    private Integer yearsOfExperience;
    private String workplace;
    private String workplaceAddress;
    private String hcaptchaToken;
}