package br.ufpb.dcx.apps4society.educapi.unit.domain.builder;

import br.ufpb.dcx.apps4society.educapi.repositories.ChallengeRepository;
import br.ufpb.dcx.apps4society.educapi.repositories.ContextRepository;
import br.ufpb.dcx.apps4society.educapi.repositories.UserRepository;
import br.ufpb.dcx.apps4society.educapi.services.ChallengeService;
import br.ufpb.dcx.apps4society.educapi.services.ContextService;
import br.ufpb.dcx.apps4society.educapi.services.JWTService;
import br.ufpb.dcx.apps4society.educapi.services.UserService;
import org.springframework.beans.factory.annotation.Autowired;

public class ServicesBuilder {

    @Autowired
    private ChallengeRepository challengeRepository;
    @Autowired
    private ContextRepository contextRepository;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private JWTService jwtService;

    public static ServicesBuilder anService(){
        return new ServicesBuilder();
    }
    public ServicesBuilder withJwtService(JWTService jwtService){
        this.jwtService = jwtService;
        return this;
    }    
    public ServicesBuilder withChallengeRepository(ChallengeRepository challengeRepository) {
        this.challengeRepository = challengeRepository;
        return this;
    }
    public ServicesBuilder withContextRepository(ContextRepository contextRepository){
        this.contextRepository = contextRepository;
        return this;
    }
    public ServicesBuilder withUserRepository(UserRepository userRepository){
        this.userRepository = userRepository;
        return this;
    }
    public ChallengeService buildChallengeService(){
        return new ChallengeService(this.jwtService, this.challengeRepository, this.contextRepository, this.userRepository);
    }
    public ContextService buildContextService(){
        return new ContextService(this.jwtService, this.contextRepository, this.userRepository);
    }
    public UserService buildUserService(){
        return new UserService(this.jwtService, this.userRepository);
    }
    public JWTService buildJwtService(){
        return new JWTService(this.userRepository);
    }

}
