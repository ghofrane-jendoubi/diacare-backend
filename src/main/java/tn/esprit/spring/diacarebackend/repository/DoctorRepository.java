package tn.esprit.spring.diacarebackend.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import tn.esprit.spring.diacarebackend.entities.Doctor;

import java.util.List;
@Repository
public interface DoctorRepository extends JpaRepository<Doctor, Long> {

    // Récupérer tous les médecins vérifiés
    List<Doctor> findByVerifiedTrue();

    // Récupérer les médecins par spécialité
    List<Doctor> findBySpeciality(String speciality);

    // Récupérer uniquement les infos nécessaires pour l'affichage
    @Query("SELECT new Doctor(d.id, d.firstName, d.lastName, d.speciality, d.profilePicture) " +
            "FROM Doctor d WHERE d.verified = true")
    List<Doctor> findAllVerifiedDoctorsForDisplay();
}