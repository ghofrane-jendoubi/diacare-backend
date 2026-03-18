package tn.esprit.spring.diacarebackend.serviceImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import tn.esprit.spring.diacarebackend.entities.User;
import tn.esprit.spring.diacarebackend.repository.UserRepository;
import tn.esprit.spring.diacarebackend.services.UserService;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;

    @Override
    public User getCurrentUser(String token) {

        // temporaire
        return userRepository.findById(1L)
                .orElseThrow(() -> new RuntimeException("User not found"));
    }
}