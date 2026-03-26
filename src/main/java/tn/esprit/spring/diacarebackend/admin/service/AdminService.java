package tn.esprit.spring.diacarebackend.admin.service;

import org.springframework.stereotype.Service;
import tn.esprit.spring.diacarebackend.admin.entity.Admin;
import tn.esprit.spring.diacarebackend.admin.repository.AdminRepository;

import java.util.List;

@Service
public class AdminService {

    private final AdminRepository adminRepository;

    public AdminService(AdminRepository adminRepository) {
        this.adminRepository = adminRepository;
    }

    public List<Admin> getAllAdmins() {
        return adminRepository.findAll();
    }

    public Admin getAdminById(Long id) {
        return adminRepository.findById(id).orElse(null);
    }

    public Admin saveAdmin(Admin admin) {
        return adminRepository.save(admin);
    }

    public void deleteAdmin(Long id) {
        adminRepository.deleteById(id);
    }
}