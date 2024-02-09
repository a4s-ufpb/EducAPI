package br.ufpb.dcx.apps4society.educapi.services;

import br.ufpb.dcx.apps4society.educapi.domain.User;
import br.ufpb.dcx.apps4society.educapi.dto.user.UserLoginDTO;
import br.ufpb.dcx.apps4society.educapi.filter.TokenFilter;
import br.ufpb.dcx.apps4society.educapi.repositories.UserRepository;
import br.ufpb.dcx.apps4society.educapi.response.LoginResponse;
import br.ufpb.dcx.apps4society.educapi.services.exceptions.InvalidUserException;
import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTVerificationException;
import org.apache.commons.validator.routines.EmailValidator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.Optional;

@Service
public class JWTService {

    @Autowired
    private UserRepository userRepository;

    @Value("${app.token.key:secret}")
    private String TOKEN_KEY;

    public JWTService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }
    
    public LoginResponse authenticate(UserLoginDTO userLoginDTO) throws InvalidUserException {

        Optional<User> userOptional = userRepository.findByEmailAndPassword(userLoginDTO.getEmail(), userLoginDTO.getPassword());
        if (userOptional.isEmpty()){
            throw new InvalidUserException();
        }

        return new LoginResponse(generateToken(userLoginDTO));
    }

    private String generateToken(UserLoginDTO userLoginDTO){
        Algorithm algorithm = Algorithm.HMAC256(TOKEN_KEY.getBytes());
        return JWT.create()
                .withSubject(userLoginDTO.getEmail())
                .withExpiresAt(expirationToken())
                .sign(algorithm)
                .strip();

    }

    private Instant expirationToken() {
        return LocalDateTime.now().plusHours(1).toInstant(ZoneOffset.of("-03:00"));
    }

    public Optional<String> recoverUser(String header){
        if (header == null || !header.startsWith("Bearer ")){
            throw new SecurityException();
        }

        String token = header.substring(TokenFilter.TOKEN_INDEX);
        String subject;

        try {
            Algorithm algorithm = Algorithm.HMAC256(TOKEN_KEY.getBytes());
            subject = JWT.require(algorithm).build().verify(token).getSubject();
            if(!emailValidator(subject)){
                return Optional.empty();
            }
        }catch (JWTVerificationException error){
           throw new SecurityException("Token invalid or expired!");
        }        

        return Optional.of(subject);
    }    

    public String tokenBearerFormat(String token){
        String bearedToken = "Bearer " + token;
        return bearedToken;
    }

    public boolean emailValidator(String email){
        boolean valid = EmailValidator.getInstance().isValid(email);
        return valid;
    }
}


