package tn.esprit.spring.diacarebackend.DTOs;


import lombok.Data;

@Data
public class TopContributorDTO {
    private Long patientId;
    private String patientName;
    private long postCount;
    private long commentCount;
}