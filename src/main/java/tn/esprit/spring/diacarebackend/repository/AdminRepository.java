package tn.esprit.spring.diacarebackend.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import tn.esprit.spring.diacarebackend.entities.Admin;
@Repository
public interface AdminRepository extends JpaRepository<Admin, Long> {
}