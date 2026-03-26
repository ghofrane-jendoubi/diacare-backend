package tn.esprit.spring.diacarebackend.admin.entity;


import jakarta.persistence.*;
import lombok.Data;
import tn.esprit.spring.diacarebackend.user.entity.User;

@Entity
@Table(name = "admins")
@Data
public class Admin {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    private User user;
}