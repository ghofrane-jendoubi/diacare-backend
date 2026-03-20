package tn.esprit.spring.diacarebackend.repository;

import tn.esprit.spring.diacarebackend.entities.ContentLike;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface ContentLikeRepository extends JpaRepository<ContentLike, Long> {
    Optional<ContentLike> findByContentIdAndUserId(Long contentId, Long userId);
    boolean existsByContentIdAndUserId(Long contentId, Long userId);
    void deleteByContentIdAndUserId(Long contentId, Long userId);
}