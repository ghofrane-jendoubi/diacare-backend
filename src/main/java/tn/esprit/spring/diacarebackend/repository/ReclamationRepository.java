package tn.esprit.spring.diacarebackend.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import tn.esprit.spring.diacarebackend.entities.Reclamation;
import tn.esprit.spring.diacarebackend.entities.ReclamationStatus;
import tn.esprit.spring.diacarebackend.entities.UserRole;

import java.util.List;
@Repository
public interface ReclamationRepository extends JpaRepository<Reclamation, Long> {
    List<Reclamation> findByCreatedByIdAndCreatedByRole(Long createdById, UserRole createdByRole);
    List<Reclamation> findByTargetRole(UserRole targetRole);
    List<Reclamation> findByTargetRoleAndStatus(UserRole targetRole, ReclamationStatus status);
}
