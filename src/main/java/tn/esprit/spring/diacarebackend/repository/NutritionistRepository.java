package tn.esprit.spring.diacarebackend.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import org.springframework.stereotype.Repository;
import tn.esprit.spring.diacarebackend.entities.CertificateStatus;
import tn.esprit.spring.diacarebackend.entities.Nutritionist;

import java.util.List;
import java.util.Optional;
@Repository
public interface NutritionistRepository extends JpaRepository<Nutritionist, Long> {
    Optional<Nutritionist> findByEmail(String email);
    Optional<Nutritionist> findByActivationToken(String activationToken);
    List<Nutritionist> findByCertificateStatus(CertificateStatus status);
}