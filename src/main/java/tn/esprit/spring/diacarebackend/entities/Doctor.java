package tn.esprit.spring.diacarebackend.entities;

import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Entity
@Table(name = "doctors")
@Data
@EqualsAndHashCode(callSuper = true)
public class Doctor extends User {

    @Enumerated(EnumType.STRING)
    private Speciality speciality;

    @Column(unique = true)
    private String licenseNumber;

    private String hospital;

    private Integer yearsOfExperience;

    private Double consultationFee;

    // Image du certificat stockée sur le serveur
    @Column(name = "certificate_image")
    private String certificateImage;

    // Statut du certificat — PENDING par défaut
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private CertificateStatus certificateStatus = CertificateStatus.PENDING;
}