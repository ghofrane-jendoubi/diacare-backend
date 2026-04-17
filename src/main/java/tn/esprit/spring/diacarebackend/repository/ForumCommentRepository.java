package tn.esprit.spring.diacarebackend.repository;

import org.springframework.stereotype.Repository;
import tn.esprit.spring.diacarebackend.entities.ForumComment;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
@Repository
public interface ForumCommentRepository extends JpaRepository<ForumComment, Long> {
    List<ForumComment> findByPostIdOrderByCreatedAtAsc(Long postId);
    List<ForumComment> findByPatientId(Long patientId);
}
