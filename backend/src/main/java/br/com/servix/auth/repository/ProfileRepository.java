package br.com.servix.auth.repository;

import br.com.servix.auth.domain.Profile;
import br.com.servix.auth.domain.ProfileName;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProfileRepository extends JpaRepository<Profile, Long> {

    Optional<Profile> findByName(ProfileName name);
}
