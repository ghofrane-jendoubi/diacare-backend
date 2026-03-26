package tn.esprit.spring.diacarebackend.repository;

import tn.esprit.spring.diacarebackend.entities.ForumPost;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;

public interface ForumPostRepository extends JpaRepository<ForumPost, Long> {

    Page<ForumPost> findByIsFlaggedFalseOrderByCreatedAtDesc(Pageable pageable);

    Page<ForumPost> findByCategoryAndIsFlaggedFalseOrderByCreatedAtDesc(
            ForumPost.Category category, Pageable pageable);

    List<ForumPost> findByPatientIdOrderByCreatedAtDesc(Long patientId);

    @Modifying
    @Transactional
    @Query("UPDATE ForumPost p SET p.likeCount = p.likeCount + 1 WHERE p.id = :id")
    void incrementLikeCount(@Param("id") Long id);

    @Modifying
    @Transactional
    @Query("UPDATE ForumPost p SET p.likeCount = p.likeCount - 1 WHERE p.id = :id AND p.likeCount > 0")
    void decrementLikeCount(@Param("id") Long id);

    @Modifying
    @Transactional
    @Query("UPDATE ForumPost p SET p.commentCount = p.commentCount + 1 WHERE p.id = :id")
    void incrementCommentCount(@Param("id") Long id);
}