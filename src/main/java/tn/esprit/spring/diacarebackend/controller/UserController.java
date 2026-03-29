package tn.esprit.spring.diacarebackend.controller;

import org.springframework.stereotype.Repository;
import org.springframework.web.bind.annotation.*;
import tn.esprit.spring.diacarebackend.entities.User;
import tn.esprit.spring.diacarebackend.repository.UserRepository;

import java.util.List;

@RestController
@RequestMapping("/api/users")
public class UserController {

    private final UserRepository userRepository;

    // Injection du repository
    public UserController(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    // Endpoint pour récupérer tous les utilisateurs
    @GetMapping
    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    // Endpoint pour ajouter un utilisateur (optionnel, pour tester)
    @PostMapping
    public User createUser(@RequestBody User user) {
        return userRepository.save(user);
    }
}