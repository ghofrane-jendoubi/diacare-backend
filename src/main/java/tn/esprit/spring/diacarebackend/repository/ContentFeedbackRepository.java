package tn.esprit.spring.diacarebackend.repository;

import org.springframework.stereotype.Repository;
import tn.esprit.spring.diacarebackend.entities.ContentFeedback;
import tn.esprit.spring.diacarebackend.entities.Emotion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
@Repository
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

    List<ContentFeedback> findTop5ByPatientIdOrderByCreatedAtDesc(Long patientId);

    List<ContentFeedback> findByPatientIdOrderByCreatedAtAsc(Long patientId);

    // Récupérer l'émotion dominante d'un contenu (celle avec le plus de feedbacks)
    @Query("SELECT f.emotion FROM ContentFeedback f WHERE f.contentId = :contentId " +
           "GROUP BY f.emotion ORDER BY COUNT(f) DESC")
    List<Emotion> findDominantEmotionsByContentId(@Param("contentId") Long contentId);
}
