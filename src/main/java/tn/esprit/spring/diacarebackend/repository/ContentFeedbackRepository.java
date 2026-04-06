package tn.esprit.spring.diacarebackend.repository;

import tn.esprit.spring.diacarebackend.entities.ContentFeedback;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface ContentFeedbackRepository extends JpaRepository<ContentFeedback, Long> {
    Optional<ContentFeedback> findByPatientIdAndContentId(Long patientId, Long contentId);

    @Query("SELECT f FROM ContentFeedback f WHERE f.contentId = :contentId")
    List<ContentFeedback> findByContentId(@Param("contentId") Long contentId);

    @Query("SELECT f FROM ContentFeedback f WHERE f.contentId IN :contentIds")
    List<ContentFeedback> findByContentIdIn(@Param("contentIds") Collection<Long> contentIds);

    @Query("SELECT CASE WHEN COUNT(f) > 0 THEN true ELSE false END " +
            "FROM ContentFeedback f WHERE f.contentId = :contentId AND f.patientId = :patientId")
    boolean existsByContentIdAndPatientId(@Param("contentId") Long contentId,
                                          @Param("patientId") Long patientId);

    @Query("SELECT f FROM ContentFeedback f WHERE f.patientId = :patientId")
    List<ContentFeedback> findByPatientId(@Param("patientId") Long patientId);

    List<ContentFeedback> findAllByOrderByCreatedAtDesc();
}
