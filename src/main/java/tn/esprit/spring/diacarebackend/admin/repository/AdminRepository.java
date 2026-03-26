package tn.esprit.spring.diacarebackend.admin.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import tn.esprit.spring.diacarebackend.admin.entity.Admin;

public interface AdminRepository extends JpaRepository<Admin, Long> {
}