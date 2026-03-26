package tn.esprit.spring.diacarebackend.admin.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import tn.esprit.spring.diacarebackend.admin.entity.Admin;
import tn.esprit.spring.diacarebackend.admin.service.AdminService;

@RestController
@RequestMapping("/api/admin")
@CrossOrigin(origins = "http://localhost:4200")
public class AdminController {

    private final AdminService adminService;

    public AdminController(AdminService adminService) {
        this.adminService = adminService;
    }

    @PostMapping("/register")
    public ResponseEntity<Admin> registerAdmin(@RequestBody Admin admin) {
        Admin savedAdmin = adminService.saveAdmin(admin);
        return ResponseEntity.ok(savedAdmin);
    }
}