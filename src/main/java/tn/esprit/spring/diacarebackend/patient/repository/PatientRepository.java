package tn.esprit.spring.diacarebackend.patient.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import tn.esprit.spring.diacarebackend.patient.entity.Patient;

import java.util.Optional;

public interface PatientRepository extends JpaRepository<Patient, Long> {
    Optional<Patient> findByEmail(String email);
    Optional<Patient> findByActivationToken(String activationToken);
}