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
        // Try to find any user (maybe the first one)
        return userRepository.findAll().stream().findFirst()
                .orElseGet(() -> {
                    // No user exists -> create one
                    User newUser = new User();
                    newUser.setName("Default User");
                    newUser.setEmail("default@example.com");
                    return userRepository.save(newUser);
                });
    }
}