package tn.esprit.spring.diacarebackend.repository;


import tn.esprit.spring.diacarebackend.entities.EducationalContent;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface EducationalContentRepository extends JpaRepository<EducationalContent, Long> {

    Page<EducationalContent> findByIsPublishedTrueOrderByCreatedAtDesc(Pageable pageable);

    Page<EducationalContent> findByCategoryAndIsPublishedTrue(
            EducationalContent.Category category, Pageable pageable);

    @Query("SELECT e FROM EducationalContent e WHERE e.isPublished = true AND " +
            "(LOWER(e.title) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
            "LOWER(e.content) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
            "LOWER(e.tags) LIKE LOWER(CONCAT('%', :keyword, '%')))")
    Page<EducationalContent> searchContent(@Param("keyword") String keyword, Pageable pageable);

    List<EducationalContent> findByIsFeaturedTrueAndIsPublishedTrue();

    @Modifying
    @Query("UPDATE EducationalContent e SET e.viewCount = e.viewCount + 1 WHERE e.id = :id")
    void incrementViewCount(@Param("id") Long id);

    @Modifying
    @Query("UPDATE EducationalContent e SET e.likeCount = e.likeCount + 1 WHERE e.id = :id")
    void incrementLikeCount(@Param("id") Long id);

    @Modifying
    @Query("UPDATE EducationalContent e SET e.likeCount = e.likeCount - 1 WHERE e.id = :id AND e.likeCount > 0")
    void decrementLikeCount(@Param("id") Long id);

    @Modifying
    @Query("UPDATE EducationalContent e SET e.commentCount = e.commentCount + 1 WHERE e.id = :id")
    void incrementCommentCount(@Param("id") Long id);

    List<EducationalContent> findTop5ByIsPublishedTrueOrderByViewCountDesc();
}
