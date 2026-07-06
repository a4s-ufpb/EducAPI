package br.ufpb.dcx.apps4society.educapi.filter;

import br.ufpb.dcx.apps4society.educapi.domain.User;
import br.ufpb.dcx.apps4society.educapi.repositories.UserRepository;
import br.ufpb.dcx.apps4society.educapi.services.JWTService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;
import java.util.Optional;

/**
 * Reads the "Authorization" header (if present) on every request, validates
 * the JWT and, when valid, populates the {@link SecurityContextHolder} with
 * an authenticated principal carrying a {@code ROLE_<role>} authority
 * (e.g. {@code ROLE_CLIENTE}, {@code ROLE_ADMIN}, {@code ROLE_SYSADMIN}).
 *
 * This filter is intentionally permissive at the servlet-filter level: it
 * never blocks or rejects a request by itself, even for a missing/invalid
 * token. Endpoints that require authentication keep enforcing that the same
 * way they already did (via {@code JWTService.recoverUser} inside the
 * services), so existing behavior for those endpoints is unchanged.
 *
 * What this filter enables is role-based authorization via
 * {@code @PreAuthorize} on controller/service methods, since Spring Security
 * needs an Authentication object in the context to evaluate those
 * expressions. Endpoints without a {@code @PreAuthorize} annotation are not
 * affected at all.
 */
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private static final Logger logger = LoggerFactory.getLogger(JwtAuthenticationFilter.class);

    private final JWTService jwtService;
    private final UserRepository userRepository;

    public JwtAuthenticationFilter(JWTService jwtService, UserRepository userRepository) {
        this.jwtService = jwtService;
        this.userRepository = userRepository;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        String header = request.getHeader("Authorization");

        if (header != null && header.startsWith("Bearer ")) {
            try {
                Optional<String> emailOptional = jwtService.recoverUser(header);

                if (emailOptional.isPresent()) {
                    Optional<User> userOptional = userRepository.findByEmail(emailOptional.get());

                    userOptional.ifPresent(user -> {
                        List<GrantedAuthority> authorities =
                                List.of(new SimpleGrantedAuthority("ROLE_" + user.getRole().name()));

                        UsernamePasswordAuthenticationToken authentication =
                                new UsernamePasswordAuthenticationToken(user, null, authorities);

                        SecurityContextHolder.getContext().setAuthentication(authentication);
                    });
                }
            } catch (RuntimeException e) {
                // Invalid/expired token: leave the SecurityContext empty and let the
                // request proceed. Endpoints that require auth still enforce it
                // themselves (via JWTService.recoverUser) and return 401 as before.
                logger.debug("Token invalido ou expirado ao popular o SecurityContext: {}", e.getMessage());
                SecurityContextHolder.clearContext();
            }
        }

        filterChain.doFilter(request, response);
    }
}
