package tn.esprit.spring.diacarebackend.nutritionist.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import tn.esprit.spring.diacarebackend.doctor.entity.CertificateStatus;
import tn.esprit.spring.diacarebackend.nutritionist.entity.Nutritionist;

import java.util.List;
import java.util.Optional;

public interface NutritionistRepository extends JpaRepository<Nutritionist, Long> {
    Optional<Nutritionist> findByEmail(String email);
    Optional<Nutritionist> findByActivationToken(String activationToken);
    List<Nutritionist> findByCertificateStatus(CertificateStatus status);
}