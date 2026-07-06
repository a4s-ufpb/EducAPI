package br.ufpb.dcx.apps4society.educapi.utils.builder;

import java.util.List;

import static org.mockito.Mockito.mock;
import org.springframework.beans.factory.annotation.Autowired;

import br.ufpb.dcx.apps4society.educapi.domain.Challenge;
import br.ufpb.dcx.apps4society.educapi.domain.Context;
import br.ufpb.dcx.apps4society.educapi.domain.User;
import br.ufpb.dcx.apps4society.educapi.repositories.ChallengeRepository;
import br.ufpb.dcx.apps4society.educapi.repositories.ContextRepository;
import br.ufpb.dcx.apps4society.educapi.repositories.UserRepository;
import br.ufpb.dcx.apps4society.educapi.services.ChallengeService;
import br.ufpb.dcx.apps4society.educapi.services.ContextService;
import br.ufpb.dcx.apps4society.educapi.services.JWTService;
import br.ufpb.dcx.apps4society.educapi.services.LogAuditoriaService;
import br.ufpb.dcx.apps4society.educapi.services.UploadImageService;
import br.ufpb.dcx.apps4society.educapi.services.UserService;

public class ServicesBuilder {

    @Autowired
    private ChallengeRepository challengeRepository = mock(ChallengeRepository.class);
    @Autowired
    private ContextRepository contextRepository = mock(ContextRepository.class);
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private JWTService jwtService;
    private LogAuditoriaService logAuditoriaService = mock(LogAuditoriaService.class);

    public static ServicesBuilder anService() {
        return new ServicesBuilder();
    }

    public ServicesBuilder withJwtService(JWTService jwtService) {
        this.jwtService = jwtService;
        return this;
    }

    public ServicesBuilder withChallengeRepository(ChallengeRepository challengeRepository) {
        this.challengeRepository = challengeRepository;
        return this;
    }

    public ServicesBuilder withContextRepository(ContextRepository contextRepository) {
        this.contextRepository = contextRepository;
        return this;
    }

    public ServicesBuilder withUserRepository(UserRepository userRepository) {
        this.userRepository = userRepository;
        return this;
    }

    public ServicesBuilder withLogAuditoriaService(LogAuditoriaService logAuditoriaService) {
        this.logAuditoriaService = logAuditoriaService;
        return this;
    }

    public ChallengeService buildChallengeService() {
        return new ChallengeService(
                this.jwtService,
                this.challengeRepository,
                this.contextRepository,
                this.userRepository,
                mock(UploadImageService.class),
                this.logAuditoriaService
        );
    }

    public ContextService buildContextService() {
        return new ContextService(
                this.jwtService,
                this.contextRepository,
                this.userRepository,
                mock(UploadImageService.class),
                this.logAuditoriaService
        );
    }

    public UserService buildUserService() {
        return new UserService(
                this.jwtService,
                this.userRepository,
                this.contextRepository,
                this.challengeRepository,
                this.logAuditoriaService
        );
    }

    public JWTService buildJwtService() {
        return new JWTService(this.userRepository, this.logAuditoriaService);
    }

    public static void insertSimulator(Object obj, List list) {
        if (obj.getClass() == Challenge.class) {

            IdGenerator((Challenge) obj, list);
            list.add((Challenge) obj);
        } else if (obj.getClass() == Context.class) {

            IdGenerator((Context) obj, list);
            list.add((Context) obj);
        } else if (obj.getClass() == User.class) {

            IdGenerator((User) obj, list);
            list.add((User) obj);
        }

    }

    private static Object IdGenerator(Object obj, List list) {
        if (obj.getClass() == Challenge.class) {

            Challenge clg = (Challenge) obj;
            if (list.isEmpty()) {
                clg.setId(1L);
            }

            int intId = list.size() + 1;
            Long longId = Long.valueOf(intId);
            clg.setId(longId);
        } else if (obj.getClass() == Context.class) {
            Context ctt = (Context) obj;

            if (list.isEmpty()) {
                ctt.setId(1L);
            }

            int intId = list.size() + 1;
            Long longId = Long.valueOf(intId);
            ctt.setId(longId);
        } else if (obj.getClass() == User.class) {
            User user = (User) obj;

            if (list.isEmpty()) {
                user.setId(1L);
            }

            int intId = list.size() + 1;
            Long longId = Long.valueOf(intId);
            user.setId(longId);
        }

        return obj;

    }

}
