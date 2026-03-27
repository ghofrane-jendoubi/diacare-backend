package tn.esprit.spring.diacarebackend.dto;

import lombok.Data;

@Data
public class AdminSignupRequest {
    private String firstName;
    private String lastName;
    private String email;
    private String phone;
    private String password;
}