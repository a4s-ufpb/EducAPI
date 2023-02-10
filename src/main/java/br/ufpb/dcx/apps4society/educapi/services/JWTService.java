package br.ufpb.dcx.apps4society.educapi.services;

import br.ufpb.dcx.apps4society.educapi.domain.User;
import br.ufpb.dcx.apps4society.educapi.dto.user.UserLoginDTO;
import br.ufpb.dcx.apps4society.educapi.filter.TokenFilter;
import br.ufpb.dcx.apps4society.educapi.repositories.UserRepository;
import br.ufpb.dcx.apps4society.educapi.response.LoginResponse;
import br.ufpb.dcx.apps4society.educapi.services.exceptions.InvalidUserException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.SignatureException;
import org.apache.commons.validator.routines.EmailValidator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.stereotype.Service;


import java.nio.channels.UnsupportedAddressTypeException;
import java.util.Date;
import java.util.Optional;

@Service
public class JWTService {

    @Autowired
    private UserRepository userRepository;

    @Value("${app.token.key}")
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
        return Jwts.builder()
                .setSubject(userLoginDTO.getEmail())
                .signWith(SignatureAlgorithm.HS512, TOKEN_KEY)
                .setExpiration(new Date(System.currentTimeMillis() + 30 * 60 * 1000)).compact();

    }

    public Optional<String> recoverUser(String header){
        if (header == null || !header.startsWith("Bearer ")){
            throw new SecurityException();
        }

        String token = header.substring(TokenFilter.TOKEN_INDEX);
        String subject;

        try {
            subject = Jwts.parser().setSigningKey(TOKEN_KEY).parseClaimsJws(token).getBody().getSubject();
            if(!emailValidator(subject)){
                return Optional.empty();
            }
        }catch (SignatureException error){
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


