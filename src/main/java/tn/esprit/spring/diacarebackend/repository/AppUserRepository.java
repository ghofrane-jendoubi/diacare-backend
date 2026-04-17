package tn.esprit.spring.diacarebackend.repository;

import org.springframework.stereotype.Repository;
import tn.esprit.spring.diacarebackend.entities.AppUser;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
@Repository
public interface AppUserRepository extends JpaRepository<AppUser, Long> {
    Optional<AppUser> findByEmailAndPassword(String email, String password);
    Optional<AppUser> findByEmail(String email);
    boolean existsByEmail(String email);
    List<AppUser> findByDiabetesTypeIgnoreCase(String diabetesType);
    List<AppUser> findByDiabetesTypeIn(Collection<String> diabetesTypes);
}
