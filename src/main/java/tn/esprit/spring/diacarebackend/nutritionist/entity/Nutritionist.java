package tn.esprit.spring.diacarebackend.nutritionist.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import tn.esprit.spring.diacarebackend.doctor.entity.CertificateStatus;
import tn.esprit.spring.diacarebackend.user.entity.User;

@Entity
@Table(name = "nutritionists")
@Data
@EqualsAndHashCode(callSuper = true)
public class Nutritionist extends User {

    private String licenseNumber;

    private Integer yearsOfExperience;

    private String workplace;       // "Cabinet libéral", "Clinique", "En ligne"

    private String workplaceAddress;

    @Column(name = "certificate_image")
    private String certificateImage;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private CertificateStatus certificateStatus = CertificateStatus.PENDING;
}