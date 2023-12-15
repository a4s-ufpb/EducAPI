package br.ufpb.dcx.apps4society.educapi.services;

import java.util.Date;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTVerifier;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.AlgorithmMismatchException;
import com.auth0.jwt.exceptions.MissingClaimException;
import com.auth0.jwt.exceptions.SignatureVerificationException;
import com.auth0.jwt.exceptions.TokenExpiredException;
import com.auth0.jwt.interfaces.DecodedJWT;

import br.ufpb.dcx.apps4society.educapi.domain.User;
import br.ufpb.dcx.apps4society.educapi.dto.user.UserLoginDTO;
import br.ufpb.dcx.apps4society.educapi.filter.TokenFilter;
import br.ufpb.dcx.apps4society.educapi.repositories.UserRepository;
import br.ufpb.dcx.apps4society.educapi.response.LoginResponse;
import br.ufpb.dcx.apps4society.educapi.services.exceptions.InvalidUserException;

@Service
public class JWTService {

    @Autowired
    private UserRepository userRepository;

    @Value("${app.token.key:token_key}")
    private String TOKEN_KEY;

    public JWTService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public LoginResponse authenticate(UserLoginDTO userLoginDTO) throws InvalidUserException {

        Optional<User> userOptional = userRepository.findByEmailAndPassword(userLoginDTO.getEmail(),
                userLoginDTO.getPassword());
        if (userOptional.isEmpty()) {
            throw new InvalidUserException();
        }

        return new LoginResponse(generateToken(userLoginDTO));
    }

    private String generateToken(UserLoginDTO userLoginDTO) {
        Algorithm algorithm = Algorithm.HMAC256(TOKEN_KEY.getBytes());
        return JWT.create()
                .withSubject(userLoginDTO.getEmail())
                .withExpiresAt(expiration())
                .sign(algorithm)
                .strip();

    }

    private Date expiration() {
        return new Date(System.currentTimeMillis() + 30 * 60 * 1000);
    }

    public String recoverUser(String header) {
        if (header == null || !header.startsWith("Bearer ")) {
            throw new SecurityException("Token invalid or expired!");
        }

        String token = header.substring(TokenFilter.TOKEN_INDEX);
        String subject;

        try {
            Algorithm algorithm = Algorithm.HMAC256(TOKEN_KEY.getBytes());
            JWTVerifier verifier = JWT.require(algorithm).build();
            DecodedJWT decodedJWT = verifier.verify(token);
            subject = decodedJWT.getSubject();
        } catch (TokenExpiredException | MissingClaimException
                | SignatureVerificationException | AlgorithmMismatchException | IllegalArgumentException error) {
            throw new SecurityException("Token invalid or expired!");
        }

        return subject;
    }

    public String tokenBearerFormat(String token) {
        return "Bearer " + token;
    }
}
