package tn.esprit.spring.diacarebackend.repository;

import tn.esprit.spring.diacarebackend.entities.ForumLike;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ForumLikeRepository extends JpaRepository<ForumLike, Long> {
    boolean existsByPostIdAndPatientId(Long postId, Long patientId);
    void deleteByPostIdAndPatientId(Long postId, Long patientId);
}