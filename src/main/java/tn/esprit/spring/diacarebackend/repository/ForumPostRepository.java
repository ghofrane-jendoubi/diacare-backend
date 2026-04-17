package tn.esprit.spring.diacarebackend.repository;

import org.springframework.stereotype.Repository;
import tn.esprit.spring.diacarebackend.entities.ForumPost;
import tn.esprit.spring.diacarebackend.dto.TopContributorDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.util.Map;
@Repository
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

    // ===== STATISTIQUES =====

    @Query("SELECT f FROM ForumPost f WHERE f.isFlagged = false ORDER BY f.likeCount DESC")
    List<ForumPost> findTopLikedPosts();

    @Query("SELECT f FROM ForumPost f WHERE f.isFlagged = false ORDER BY f.likeCount DESC")
    Page<ForumPost> findTopLikedPosts(Pageable pageable);

    @Query("SELECT new tn.esprit.spring.diacarebackend.dto.CategoryStatsDTO(f.category, COUNT(f)) " +
           "FROM ForumPost f " +
           "WHERE f.isFlagged = false " +
           "GROUP BY f.category")
    List<tn.esprit.spring.diacarebackend.dto.CategoryStatsDTO> countPostsByCategory();

    @Query("SELECT f.patientId as patientId, f.patientName as patientName, COUNT(f) as postCount, " +
           "COALESCE(SUM(f.likeCount), 0) as totalLikes, " +
           "COALESCE(SUM(f.commentCount), 0) as totalComments " +
           "FROM ForumPost f " +
           "WHERE f.isFlagged = false " +
           "GROUP BY f.patientId, f.patientName " +
           "ORDER BY COUNT(f) DESC")
    List<Object[]> findTopContributorsRaw(Pageable pageable);

    default List<tn.esprit.spring.diacarebackend.dto.TopContributorDTO> findTopContributors(Pageable pageable) {
        List<Object[]> results = findTopContributorsRaw(pageable);
        return results.stream()
                .map(row -> new tn.esprit.spring.diacarebackend.dto.TopContributorDTO(
                    ((Number) row[0]).longValue(),
                    (String) row[1],
                    ((Number) row[2]).longValue(),
                    ((Number) row[3]).longValue(),
                    ((Number) row[4]).longValue()
                ))
                .collect(java.util.stream.Collectors.toList());
    }
}
