package tn.esprit.spring.diacarebackend.repository;

import tn.esprit.spring.diacarebackend.entities.ContentComment;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface ContentCommentRepository extends JpaRepository<ContentComment, Long> {

    List<ContentComment> findByContentIdAndParentCommentIdIsNullAndIsApprovedTrueOrderByCreatedAtDesc(Long contentId);

    List<ContentComment> findByParentCommentIdAndIsApprovedTrue(Long parentId);

    long countByContentId(Long contentId);
}