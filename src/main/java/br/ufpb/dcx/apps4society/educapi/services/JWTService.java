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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.Optional;

@Service
public class JWTService {

    @Autowired
    //private
    public UserRepository userRepository;

    @Value("${app.token.key}")
    private String TOKEN_KEY;

    public JWTService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    // REMOVER NA REFATORAÇÃO getTOKEN_KEY() e setTOKEN_KEY()
    public String getTOKEN_KEY(){
        return this.TOKEN_KEY;
    }
    public void setTOKEN_KEY(String token_key){
        this.TOKEN_KEY = token_key;
    }

    public void setUserRepository(UserRepository userRepository){
        this.userRepository = userRepository;
    }

//    public void initialize(ConfigurableApplicationContext applicationContext){
//        System.setProperty("app.token.key", "value");
//    }
    
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
        }catch (SignatureException error){
           throw new SecurityException("Token invalid or expired!");
        }

        return Optional.of(subject);
    }
}
