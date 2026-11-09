package br.ufpb.dcx.apps4society.educapi.config;

import br.ufpb.dcx.apps4society.educapi.domain.Role;
import br.ufpb.dcx.apps4society.educapi.domain.User;
import br.ufpb.dcx.apps4society.educapi.repositories.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.util.Optional;

/**
 * Ensures there is always exactly one SYSADMIN account, identified by the
 * email configured in `app.sysadmin.email` (SYSADMIN_EMAIL env var).
 *
 * Runs on every application startup, regardless of profile:
 * - If a User with that email already exists, promotes it to SYSADMIN
 *   (in case it isn't already).
 * - If it does not exist yet, creates it as a Google-style account
 *   (no local password), so the person can log in via "Sign in with Google"
 *   using that same email.
 *
 * If `app.sysadmin.email` is not configured, seeding is skipped (with a
 * warning), so this is safe to run in test/CI environments too.
 */
@Component
public class SysAdminSeeder implements CommandLineRunner {

    private static final Logger logger = LoggerFactory.getLogger(SysAdminSeeder.class);

    private final UserRepository userRepository;

    @Value("${app.sysadmin.email:}")
    private String sysAdminEmail;

    public SysAdminSeeder(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public void run(String... args) {
        if (sysAdminEmail == null || sysAdminEmail.isBlank()) {
            logger.warn("app.sysadmin.email (SYSADMIN_EMAIL) nao configurado. Nenhum SYSADMIN sera criado/garantido.");
            return;
        }

        Optional<User> existing = userRepository.findByEmail(sysAdminEmail);

        if (existing.isPresent()) {
            User user = existing.get();
            if (user.getRole() != Role.SYSADMIN) {
                user.setRole(Role.SYSADMIN);
                userRepository.save(user);
                logger.info("Usuario existente promovido a SYSADMIN. email={}", sysAdminEmail);
            }
            return;
        }

        User sysAdmin = new User("SysAdmin", sysAdminEmail, null);
        sysAdmin.setRole(Role.SYSADMIN);
        userRepository.save(sysAdmin);
        logger.info("SYSADMIN criado automaticamente. email={}", sysAdminEmail);
    }
}
