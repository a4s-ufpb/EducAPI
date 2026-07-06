package br.ufpb.dcx.apps4society.educapi.config;

import br.ufpb.dcx.apps4society.educapi.filter.JwtAuthenticationFilter;
import br.ufpb.dcx.apps4society.educapi.repositories.UserRepository;
import br.ufpb.dcx.apps4society.educapi.services.JWTService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

/**
 * Wires Spring Security into the app purely to enable role-based method
 * authorization ({@code @PreAuthorize("hasRole('ADMIN')")} etc.) on top of
 * the JWT authentication that already exists (Google Sign-In and local
 * login, both handled by {@code JWTService}/{@code LoginResource}).
 *
 * The HTTP-level authorization is intentionally left open
 * ({@code permitAll()} for every path): access control for regular
 * "auth/**" endpoints continues to be enforced exactly as before, inside
 * the services themselves. This config only adds the extra role-based
 * layer needed for admin/sysadmin-only endpoints, without changing the
 * behavior of anything that already existed.
 */
@Configuration
@EnableWebSecurity
@EnableMethodSecurity(prePostEnabled = true)
public class SecurityConfig {

    @Autowired
    private JWTService jwtService;

    @Autowired
    private UserRepository userRepository;

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable())
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth.anyRequest().permitAll())
                .addFilterBefore(
                        new JwtAuthenticationFilter(jwtService, userRepository),
                        UsernamePasswordAuthenticationFilter.class
                );

        return http.build();
    }
}
