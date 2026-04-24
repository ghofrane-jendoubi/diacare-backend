package tn.esprit.spring.diacarebackend.serviceImpl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import tn.esprit.spring.diacarebackend.entities.User;
import tn.esprit.spring.diacarebackend.repository.UserRepository;
import tn.esprit.spring.diacarebackend.services.UserService;

import java.util.Base64;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;

    @Override
    public User getCurrentUser(String token) {
        // Si le token est l'ID utilisateur encodé
        try {
            String decoded = new String(Base64.getDecoder().decode(token));
            Long userId = Long.parseLong(decoded);
            return getUserById(userId);
        } catch (Exception e) {
            // Si le token est l'email
            return userRepository.findByEmail(token)
                    .orElseThrow(() -> new RuntimeException("User not found with token: " + token));
        }
    }

    @Override
    public User getUserById(Long id) {
        log.info("Fetching user by id: {}", id);
        return userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found with id: " + id));
    }


    public User getUserByEmail(String email) {
        log.info("Fetching user by email: {}", email);
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found with email: " + email));
    }
}