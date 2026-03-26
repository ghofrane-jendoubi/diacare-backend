package tn.esprit.spring.diacarebackend.doctor.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import tn.esprit.spring.diacarebackend.doctor.entity.CertificateStatus;
import tn.esprit.spring.diacarebackend.doctor.entity.Doctor;

import java.util.List;
import java.util.Optional;

public interface DoctorRepository extends JpaRepository<Doctor, Long> {
    // Ces méthodes cherchent dans users JOIN doctors automatiquement
    Optional<Doctor> findByEmail(String email);
    Optional<Doctor> findByActivationToken(String activationToken);
    List<Doctor> findByCertificateStatus(CertificateStatus status);
}