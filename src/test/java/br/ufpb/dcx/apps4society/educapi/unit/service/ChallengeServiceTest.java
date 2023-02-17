package br.ufpb.dcx.apps4society.educapi.unit.service;

import br.ufpb.dcx.apps4society.educapi.domain.Challenge;
import br.ufpb.dcx.apps4society.educapi.domain.Context;
import br.ufpb.dcx.apps4society.educapi.domain.User;
import br.ufpb.dcx.apps4society.educapi.dto.challenge.ChallengeDTO;
import br.ufpb.dcx.apps4society.educapi.dto.challenge.ChallengeRegisterDTO;
import br.ufpb.dcx.apps4society.educapi.dto.context.ContextRegisterDTO;
import br.ufpb.dcx.apps4society.educapi.dto.user.UserLoginDTO;
import br.ufpb.dcx.apps4society.educapi.dto.user.UserRegisterDTO;
import br.ufpb.dcx.apps4society.educapi.repositories.ChallengeRepository;
import br.ufpb.dcx.apps4society.educapi.repositories.ContextRepository;
import br.ufpb.dcx.apps4society.educapi.repositories.UserRepository;
import br.ufpb.dcx.apps4society.educapi.response.LoginResponse;
import br.ufpb.dcx.apps4society.educapi.services.ChallengeService;
import br.ufpb.dcx.apps4society.educapi.services.ContextService;
import br.ufpb.dcx.apps4society.educapi.services.JWTService;
import br.ufpb.dcx.apps4society.educapi.services.exceptions.ChallengeAlreadyExistsException;
import br.ufpb.dcx.apps4society.educapi.services.exceptions.InvalidUserException;
import br.ufpb.dcx.apps4society.educapi.services.exceptions.ObjectNotFoundException;
import br.ufpb.dcx.apps4society.educapi.unit.domain.builder.ChallengeBuilder;
import br.ufpb.dcx.apps4society.educapi.unit.domain.builder.ContextBuilder;
import br.ufpb.dcx.apps4society.educapi.unit.domain.builder.ServicesBuilder;
import br.ufpb.dcx.apps4society.educapi.unit.domain.builder.UserBuilder;
import br.ufpb.dcx.apps4society.educapi.util.Messages;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentMatcher;
import org.mockito.ArgumentMatchers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.ArrayList;
import java.util.List;
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


    private final ChallengeRegisterDTO challengeRegisterDTO = ChallengeBuilder.anChallenge().withId(1L).buildChallengeRegisterDTO();
    private final Challenge challenge = challengeRegisterDTO.toChallenge();

    private final Optional<Challenge> challengeOptional = ChallengeBuilder.anChallenge().withId(1L).buildOptionalChallenge();

    private final Optional<User> userOptional = UserBuilder.anUser().buildOptionalUser();
    private final UserLoginDTO userLoginDTO = UserBuilder.anUser().buildUserLoginDTO();
    private final UserRegisterDTO userRegisterDTO = UserBuilder.anUser().buildUserRegisterDTO();
    private final User creator = userRegisterDTO.userRegisterDtoToUser();

    private final Context context = ContextBuilder.anContext().withCreator(userOptional.get()).withId(1L).buildContext();
    private final Optional<Context> contextOptional = ContextBuilder.anContext().withId(1L).buildOptionalContext();
    private final ContextRegisterDTO contextRegisterDTO = ContextBuilder.anContext().buildContextRegisterDTO();

    private List<Challenge> challenges = new ArrayList<>();

    @BeforeEach
    public void setUp(){

        //Métodos de origem devem estar com optional.isPresent() implementados, quando buscar pelo 'Long', ele retorna optional ou empty.
        ReflectionTestUtils.setField(jwtService, "TOKEN_KEY", "it's a token key");
    }

    @Test
    public void findChallengeTest(){

    }

    @Test
    public void insertChallengeTest() throws ChallengeAlreadyExistsException, InvalidUserException, ObjectNotFoundException {        
        
        Mockito.lenient().when(userRepository.findByEmail(userLoginDTO.getEmail())).thenReturn(userOptional);
        Mockito.lenient().when(userRepository.findByEmailAndPassword(userLoginDTO.getEmail(), userLoginDTO.getPassword())).thenReturn(userOptional);
        Mockito.lenient().when(contextRepository.findById(context.getId())).thenReturn(contextOptional);

        LoginResponse loginResponse = jwtService.authenticate(userLoginDTO);

        Challenge challengeResponse = challengeService.insert(jwtService.tokenBearerFormat(loginResponse.getToken()), challengeRegisterDTO, context.getId());

        assertEquals(challengeResponse.getWord(), challengeRegisterDTO.getWord());
        assertEquals(challengeResponse.getSoundUrl(), challengeRegisterDTO.getSoundUrl());
        assertEquals(challengeResponse.getImageUrl(), challengeRegisterDTO.getImageUrl());
        assertEquals(challengeResponse.getVideoUrl(), challengeRegisterDTO.getVideoUrl());
    }

    @Test
    public void insertChallengeAlreadyExistTest() throws InvalidUserException, ObjectNotFoundException, ChallengeAlreadyExistsException {

        Mockito.lenient().when(userRepository.findByEmail(userLoginDTO.getEmail())).thenReturn(userOptional);
        Mockito.lenient().when(userRepository.findByEmailAndPassword(userLoginDTO.getEmail(), userLoginDTO.getPassword())).thenReturn(userOptional);
        Mockito.lenient().when(contextRepository.findById(1L)).thenReturn(contextOptional);
        Mockito.lenient().when(challengeRepository.findById(1L)).thenReturn(challengeOptional);

        LoginResponse loginResponse = jwtService.authenticate(userLoginDTO);

        Challenge challenge = challengeService.insert(jwtService.tokenBearerFormat(loginResponse.getToken()), challengeRegisterDTO, context.getId());
    
        challenge.setId(1L);
        //INICIO DE SIMULAÇÃO DA TRAMITAÇÃO DO MÉTODO INSERT()        
        //Context context = contextOptional.get();
        challenge.setCreator(creator);
        challenge.getContexts().add(context);
        //INICIO DE SIMULAÇÃO DA TRAMITAÇÃO DO MÉTODO INSERT() 

        Mockito.lenient().when(challengeRepository.findById(1L)).thenReturn(Optional.empty());

        //Challenge não está com id na classe ChallengeService
        Exception exception = assertThrows(ChallengeAlreadyExistsException.class, () -> {

            challengeService.insert(jwtService.tokenBearerFormat(loginResponse.getToken()), challengeRegisterDTO, context.getId());

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
