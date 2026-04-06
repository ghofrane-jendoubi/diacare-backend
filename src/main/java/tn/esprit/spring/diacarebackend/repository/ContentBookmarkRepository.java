package tn.esprit.spring.diacarebackend.repository;

import tn.esprit.spring.diacarebackend.entities.ContentBookmark;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface ContentBookmarkRepository extends JpaRepository<ContentBookmark, Long> {
    boolean existsByContentIdAndUserId(Long contentId, Long userId);
    Optional<ContentBookmark> findByContentIdAndUserId(Long contentId, Long userId);
    void deleteByContentIdAndUserId(Long contentId, Long userId);
    List<ContentBookmark> findByUserId(Long userId);
    List<ContentBookmark> findByUserIdIn(Collection<Long> userIds);
}
