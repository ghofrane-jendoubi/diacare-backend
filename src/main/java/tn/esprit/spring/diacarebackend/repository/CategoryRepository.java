package tn.esprit.spring.diacarebackend.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import tn.esprit.spring.diacarebackend.entities.Category;

@Repository
public interface CategoryRepository extends JpaRepository<Category, Long> {
}
