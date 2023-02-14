package br.ufpb.dcx.apps4society.educapi.unit.service;

import br.ufpb.dcx.apps4society.educapi.domain.Challenge;
import br.ufpb.dcx.apps4society.educapi.domain.Context;
import br.ufpb.dcx.apps4society.educapi.domain.User;
import br.ufpb.dcx.apps4society.educapi.dto.challenge.ChallengeDTO;
import br.ufpb.dcx.apps4society.educapi.dto.challenge.ChallengeRegisterDTO;
import br.ufpb.dcx.apps4society.educapi.dto.context.ContextRegisterDTO;
import br.ufpb.dcx.apps4society.educapi.dto.user.UserLoginDTO;
import br.ufpb.dcx.apps4society.educapi.repositories.ChallengeRepository;
import br.ufpb.dcx.apps4society.educapi.repositories.ContextRepository;
import br.ufpb.dcx.apps4society.educapi.repositories.UserRepository;
import br.ufpb.dcx.apps4society.educapi.services.ChallengeService;
import br.ufpb.dcx.apps4society.educapi.services.ContextService;
import br.ufpb.dcx.apps4society.educapi.services.JWTService;
import br.ufpb.dcx.apps4society.educapi.services.exceptions.ChallengeAlreadyExistsException;
import br.ufpb.dcx.apps4society.educapi.unit.domain.builder.ChallengeBuilder;
import br.ufpb.dcx.apps4society.educapi.unit.domain.builder.ContextBuilder;
import br.ufpb.dcx.apps4society.educapi.unit.domain.builder.ServicesBuilder;
import br.ufpb.dcx.apps4society.educapi.unit.domain.builder.UserBuilder;
import br.ufpb.dcx.apps4society.educapi.util.Messages;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

@ExtendWith(MockitoExtension.class)
public class ChallengeServiceTest {

    @Mock
    UserRepository userRepository;
    @Mock
    ContextRepository contextRepository;
    @Mock
    ChallengeRepository challengeRepository;

    @InjectMocks
    JWTService jwtService = ServicesBuilder.anService().withUserRepository(userRepository).buildJwtService();
    @InjectMocks
    ContextService contextService = ServicesBuilder.anService()
            .withJwtService(jwtService)
            .withContextRepository(contextRepository)
            .withUserRepository(userRepository).buildContextService();
    @InjectMocks
    ChallengeService challengeService = ServicesBuilder.anService()
            .withJwtService(jwtService)
            .withChallengeRepository(challengeRepository)
            .withUserRepository(userRepository)
            .withContextRepository(contextRepository).buildChallengeService();

    private final ChallengeRegisterDTO challengeRegisterDTO = ChallengeBuilder.anChallenge().buildChallengeRegisterDTO();
    private final Optional<Challenge> challengeOptional = ChallengeBuilder.anChallenge().buildOptionalChallenge();

    private final Optional<User> userOptional = UserBuilder.anUser().buildOptionalUser();
    private final UserLoginDTO userLoginDTO = UserBuilder.anUser().buildUserLoginDTO();

    private final Optional<Context> contextRegisterDTO = ContextBuilder.anContext().buildOptionalContext();

    @BeforeEach
    public void setUp(){

        //Métodos de origem devem estar com optional.isPresent() implementados, quando buscar pelo 'Long', ele retorna optional ou empty.
        ReflectionTestUtils.setField(jwtService, "TOKEN_KEY", "it's a token key");
    }

    @Test
    public void findChallengeTest(){

    }

    @Test
    public void insertChallengeTest() throws ChallengeAlreadyExistsException {        
        
        Mockito.lenient().when(userRepository.findByEmail(userLoginDTO.getEmail())).thenReturn(userOptional);
        Mockito.lenient().when(userRepository.findByEmailAndPassword(userLoginDTO.getEmail(), userLoginDTO.getPassword())).thenReturn(userOptional);

        ChallengeDTO response = challengeService
                .insert(jwtService.authenticate(userLoginDTO), challengeRegisterDTO);

        assertEquals(response.getWord(), challengeRegisterDTO.getWord());
        assertEquals(response.getSoundUrl(), challengeRegisterDTO.getSoundUrl());
        assertEquals(response.getImageUrl(), challengeRegisterDTO.getImageUrl());
        assertEquals(response.getVideoUrl(), challengeRegisterDTO.getVideoUrl());
    }

    @Test
    public void insertChallengeAlreadyExistTest() {

        Mockito.when(this.challengeRepository.findByWord(challengeRegisterDTO.getWord())).thenReturn(this.challengeOptional);

        Exception exception = assertThrows(ChallengeAlreadyExistsException.class, () -> {

            challengeService.insert(jwtService.authenticate(userLoginDTO), challengeRegisterDTO,
                    challengeRegisterDTO.getContext());
        });
        assertEquals(Messages.CHALLENGE_ALREADY_EXISTS, exception.getMessage());
    }

    @Test
    public void findChallengesByCreatorTest(){

    }

    @Test
    public void updateChallengeTest(){

    }

    @Test
    public void deleteChallengeTest(){
        
    }

    @Test
    public void findChallengesByParamsTest(){
        
    }

    @Test
    public void validateUserTest(){
        
    }

}
