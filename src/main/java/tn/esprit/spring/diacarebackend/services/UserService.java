package tn.esprit.spring.diacarebackend.services;

import tn.esprit.spring.diacarebackend.entities.User;

public interface UserService {
    User getCurrentUser(String token);
}