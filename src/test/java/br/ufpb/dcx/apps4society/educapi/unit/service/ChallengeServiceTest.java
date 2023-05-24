package br.ufpb.dcx.apps4society.educapi.unit.service;

import br.ufpb.dcx.apps4society.educapi.domain.Challenge;
import br.ufpb.dcx.apps4society.educapi.domain.Context;
import br.ufpb.dcx.apps4society.educapi.domain.User;
import br.ufpb.dcx.apps4society.educapi.dto.challenge.ChallengeRegisterDTO;
import br.ufpb.dcx.apps4society.educapi.dto.user.UserLoginDTO;
import br.ufpb.dcx.apps4society.educapi.repositories.ChallengeRepository;
import br.ufpb.dcx.apps4society.educapi.repositories.ContextRepository;
import br.ufpb.dcx.apps4society.educapi.repositories.UserRepository;
import br.ufpb.dcx.apps4society.educapi.response.LoginResponse;
import br.ufpb.dcx.apps4society.educapi.services.ChallengeService;
import br.ufpb.dcx.apps4society.educapi.services.JWTService;
import br.ufpb.dcx.apps4society.educapi.services.exceptions.ChallengeAlreadyExistsException;
import br.ufpb.dcx.apps4society.educapi.services.exceptions.InvalidUserException;
import br.ufpb.dcx.apps4society.educapi.services.exceptions.ObjectNotFoundException;
import br.ufpb.dcx.apps4society.educapi.utils.builder.ChallengeBuilder;
import br.ufpb.dcx.apps4society.educapi.utils.builder.ContextBuilder;
import br.ufpb.dcx.apps4society.educapi.utils.builder.ServicesBuilder;
import br.ufpb.dcx.apps4society.educapi.utils.builder.UserBuilder;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

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
    ChallengeService challengeService = ServicesBuilder.anService()
            .withJwtService(jwtService)
            .withChallengeRepository(challengeRepository)
            .withUserRepository(userRepository)
            .withContextRepository(contextRepository).buildChallengeService();

    @Spy
    private ChallengeRegisterDTO challengeRegisterDTO = ChallengeBuilder
            .anChallenge()
            .withId(1L).buildChallengeRegisterDTO();

    @Spy
    private ChallengeRegisterDTO challengeRegisterDTO2 = ChallengeBuilder
            .anChallenge()
            .withId(1L)
            .withWord("word2").buildChallengeRegisterDTO();
            
    @Spy
    private ChallengeRegisterDTO challengeRegisterDTO3 = ChallengeBuilder
            .anChallenge()
            .withId(1L)
            .withWord("testword2").buildChallengeRegisterDTO();
    
    private final UserLoginDTO userLoginDTOEmptyEmail = UserBuilder.anUser().withEmail("").buildUserLoginDTO();
    private final UserLoginDTO userLoginDTO = UserBuilder.anUser().buildUserLoginDTO();
    private final UserLoginDTO userLoginDTO2 = UserBuilder.anUser()
            .withName("user2")
            .withEmail("user2@educapi.com")
            .withPassword("testpassword2").buildUserLoginDTO();

    private final Optional<User> userEmptyEmailOptional = UserBuilder.anUser().withId(3L). withName("userWithoutEmail").buildOptionalUser();
    private final Optional<User> userOptional = UserBuilder.anUser().withId(1L).buildOptionalUser();
    private final Optional<User> userOptional2 = UserBuilder.anUser()
            .withId(2L)
            .withName("user2")
            .withEmail("user2@educapi.com")
            .withPassword("testpassword2").buildOptionalUser();

    private User creator = userOptional.get();    
    private User creator2 = userOptional2.get();

    private final User userWithEmptyEmail = userEmptyEmailOptional.get();    
    
    private Optional<Challenge> challengeOptional = ChallengeBuilder.anChallenge()
            .withId(1L)
            .withCreator(creator).buildOptionalChallenge();
    private Challenge challenge = challengeOptional.get();

    private final Context context = ContextBuilder.anContext().withCreator(userOptional.get()).withId(1L).buildContext();
    private final Optional<Context> contextOptional = Optional.of(context);

    private List<Challenge> challenges = new ArrayList<>();

    private final Pageable pageable = PageRequest.of(0, 20, Sort.by("name").ascending());

    @BeforeEach
    public void setUp(){

        ReflectionTestUtils.setField(jwtService, "TOKEN_KEY", "it's a token key");

        Mockito.lenient().when(userRepository.findByEmail(userLoginDTO.getEmail())).thenReturn(userOptional);
        Mockito.lenient().when(userRepository.findByEmailAndPassword(userLoginDTO.getEmail(), userLoginDTO.getPassword())).thenReturn(userOptional);
        
    }

    @Test
    public void findChallengeTest() throws InvalidUserException, ObjectNotFoundException, ChallengeAlreadyExistsException{
        
        Mockito.lenient().when(contextRepository.findById(1L)).thenReturn(contextOptional);             
        Mockito.lenient().when(challengeRegisterDTO.challengeRegisterDTOToChallenge()).thenReturn(challengeOptional.get());

        LoginResponse loginResponse = jwtService.authenticate(userLoginDTO);

        challengeService.insert(jwtService.tokenBearerFormat(loginResponse.getToken()), challengeRegisterDTO, context.getId());        

        Mockito.when(challengeRepository.findById(1L)).thenReturn(challengeOptional);

        challengeOptional = Optional.of(challengeService.find(jwtService.tokenBearerFormat(loginResponse.getToken()), 1L));

        assertEquals(challengeOptional.get().getWord(), challengeRegisterDTO.getWord());
        assertEquals(challengeOptional.get().getImageUrl(), challengeRegisterDTO.getImageUrl());
        assertEquals(challengeOptional.get().getSoundUrl(), challengeRegisterDTO.getSoundUrl());
        assertEquals(challengeOptional.get().getVideoUrl(), challengeRegisterDTO.getVideoUrl());        

    }

    @Test
    public void findChallengeInvalidUserTest() throws InvalidUserException, ObjectNotFoundException{

        Mockito.lenient().when(userRepository.findByEmail("")).thenReturn(Optional.of(userWithEmptyEmail));
        Mockito.lenient().when(userRepository.findByEmailAndPassword("", "testpassword")).thenReturn(Optional.of(userWithEmptyEmail));

        LoginResponse loginResponse = jwtService.authenticate(userLoginDTOEmptyEmail);

        assertThrows(InvalidUserException.class, () -> {

            challengeService.find(jwtService.tokenBearerFormat(loginResponse.getToken()), 1L);

        });

    }

    @Test
    public void findNotFoundChallengeTest() throws InvalidUserException, ObjectNotFoundException, ChallengeAlreadyExistsException{

        Mockito.when(challengeRepository.findById(1L)).thenReturn(Optional.empty());

        LoginResponse loginResponse = jwtService.authenticate(userLoginDTO);

        Exception exception = assertThrows(ObjectNotFoundException.class, () -> {

            challengeService.find(jwtService.tokenBearerFormat(loginResponse.getToken()), 1L);

        });

        assertEquals(exception.getMessage(), "Object not found! Id: " + 1L + ", Type: " + Challenge.class.getName());
               
    }

    @Test
    public void insertChallengeTest() throws ChallengeAlreadyExistsException, InvalidUserException, ObjectNotFoundException {        
        
        Mockito.lenient().when(contextRepository.findById(context.getId())).thenReturn(contextOptional);

        LoginResponse loginResponse = jwtService.authenticate(userLoginDTO);

        Challenge challengeResponse = challengeService.insert(jwtService.tokenBearerFormat(loginResponse.getToken()), challengeRegisterDTO, context.getId());

        assertEquals(challengeResponse.getWord(), challengeRegisterDTO.getWord());        
        assertEquals(challengeResponse.getImageUrl(), challengeRegisterDTO.getImageUrl());
        assertEquals(challengeResponse.getSoundUrl(), challengeRegisterDTO.getSoundUrl());
        assertEquals(challengeResponse.getVideoUrl(), challengeRegisterDTO.getVideoUrl());
        
    }

    @Test
    public void insertAChallengeButThereIsNoContextWithTest() throws InvalidUserException, ObjectNotFoundException, ChallengeAlreadyExistsException{

        Mockito.lenient().when(contextRepository.findById(1L)).thenReturn(Optional.empty());

        LoginResponse loginResponse = jwtService.authenticate(userLoginDTO);

        assertThrows(ObjectNotFoundException.class, () -> {
            challengeService.insert(jwtService.tokenBearerFormat(loginResponse.getToken()), challengeRegisterDTO, context.getId());

        });

    }

    @Test
    public void insertChallengeInvalidUserTest() throws InvalidUserException{

        Mockito.lenient().when(userRepository.findByEmail("")).thenReturn(userEmptyEmailOptional);
        Mockito.lenient().when(userRepository.findByEmailAndPassword("", "testpassword")).thenReturn(userEmptyEmailOptional);
        Mockito.lenient().when(contextRepository.findById(1L)).thenReturn(contextOptional);

        LoginResponse loginResponse = jwtService.authenticate(userLoginDTOEmptyEmail);

        assertThrows(InvalidUserException.class, () -> {
            challengeService.insert(jwtService.tokenBearerFormat(loginResponse.getToken()), challengeRegisterDTO, 1L);

        });

    }

    @Test
    public void insertChallengeUserNotFound() throws InvalidUserException{        
        
        Mockito.lenient().when(contextRepository.findById(1L)).thenReturn(contextOptional);

        LoginResponse loginResponse = jwtService.authenticate(userLoginDTO);

        Mockito.lenient().when(userRepository.findByEmail("user@educapi.com")).thenReturn(Optional.empty());

        assertThrows(ObjectNotFoundException.class, () -> {
            challengeService.insert(jwtService.tokenBearerFormat(loginResponse.getToken()), challengeRegisterDTO, 1L);

        });

    }

    @Test
    public void findChallengesByCreatorTest() throws InvalidUserException, ObjectNotFoundException, ChallengeAlreadyExistsException{

        Mockito.when(contextRepository.findById(1L)).thenReturn(contextOptional);
        Mockito.when(challengeRepository.findChallengesByCreator(creator)).thenReturn(challenges);

        LoginResponse loginResponse = jwtService.authenticate(userLoginDTO);

        Challenge challengeResponse = challengeService.insert(jwtService.tokenBearerFormat(loginResponse.getToken()), challengeRegisterDTO, 1L);
        Challenge challengeResponse2 = challengeService.insert(jwtService.tokenBearerFormat(loginResponse.getToken()), challengeRegisterDTO2, 1L);

        ServicesBuilder.insertSimulator(challengeResponse, challenges);
        ServicesBuilder.insertSimulator(challengeResponse2, challenges);

        List<Challenge> challengesResponse = challengeService.findChallengesByCreator(jwtService.tokenBearerFormat(loginResponse.getToken()));        

        Assertions.assertFalse(challengesResponse.isEmpty());

    }

    @Test
    public void updateChallengeTest() throws InvalidUserException, ObjectNotFoundException, ChallengeAlreadyExistsException{
        
        Mockito.when(contextRepository.findById(1L)).thenReturn(contextOptional);
        Mockito.when(challengeRegisterDTO.challengeRegisterDTOToChallenge()).thenReturn(challengeOptional.get());

        LoginResponse loginResponse = jwtService.authenticate(userLoginDTO);

        challengeService.insert(jwtService.tokenBearerFormat(loginResponse.getToken()), challengeRegisterDTO, context.getId());

        Mockito.when(challengeRepository.findById(1L)).thenReturn(challengeOptional);

        Challenge challengeResponse = challengeService
                .update(jwtService.tokenBearerFormat(loginResponse.getToken()), challengeRegisterDTO2, context.getId());

        assertEquals("word2", challengeResponse.getWord());

    }

    @Test
    public void updateChallengeInvalidUserTest() throws InvalidUserException, ObjectNotFoundException, ChallengeAlreadyExistsException{

        Mockito.when(userRepository.findByEmail("user2@educapi.com")).thenReturn(Optional.of(creator2));
        Mockito.when(userRepository.findByEmailAndPassword("user2@educapi.com", "testpassword2"))
                .thenReturn(Optional.of(creator2));
        Mockito.when(contextRepository.findById(1L)).thenReturn(contextOptional);
        Mockito.when(challengeRegisterDTO.challengeRegisterDTOToChallenge()).thenReturn(challengeOptional.get());

        LoginResponse loginResponse = jwtService.authenticate(userLoginDTO);
        LoginResponse loginResponse2 = jwtService.authenticate(userLoginDTO2);

        challengeService.insert(jwtService.tokenBearerFormat(loginResponse.getToken()), challengeRegisterDTO, context.getId());

        Mockito.when(challengeRepository.findById(1L)).thenReturn(challengeOptional);

        Exception exception = assertThrows(InvalidUserException.class, () -> {

            challengeService.update(jwtService.tokenBearerFormat(loginResponse2.getToken()), challengeRegisterDTO, challenge.getId());

        });

    }

    @Test
    public void deleteChallengeTest() throws InvalidUserException, ObjectNotFoundException, ChallengeAlreadyExistsException{

        Mockito.lenient().when(challengeRepository.findById(1L)).thenReturn(challengeOptional);

        LoginResponse loginResponse = jwtService.authenticate(userLoginDTO);
        
        Set<Context> contextsSet = new HashSet<>();
        contextsSet.add(context);
        challenge.setContexts(contextsSet);
        
        challengeService.delete(jwtService.tokenBearerFormat(loginResponse.getToken()), challenge.getId());
        
    }

    @Test
    public void deleteChallengeInvalidUserTest() throws InvalidUserException, ObjectNotFoundException, ChallengeAlreadyExistsException{

        Mockito.lenient().when(challengeRepository.findById(1L)).thenReturn(challengeOptional);
        Mockito.when(userRepository.findByEmail("user2@educapi.com")).thenReturn(userOptional2);
        Mockito.when(userRepository.findByEmailAndPassword("user2@educapi.com", "testpassword2")).thenReturn(userOptional2);
        Mockito.when(contextRepository.findById(1L)).thenReturn(contextOptional);

        LoginResponse loginResponse = jwtService.authenticate(userLoginDTO);
        LoginResponse loginResponse2 = jwtService.authenticate(userLoginDTO2);

        Challenge challengeResponse = challengeService.insert(jwtService.tokenBearerFormat(loginResponse.getToken()), challengeRegisterDTO, context.getId());

        ServicesBuilder.insertSimulator(challengeResponse, challenges);

        assertThrows(InvalidUserException.class, () -> {
            challengeService.delete(jwtService.tokenBearerFormat(loginResponse2.getToken()), challengeResponse.getId());

        });

    }

    @Test
    public void findChallengesByParamsTest() throws ObjectNotFoundException, InvalidUserException, ChallengeAlreadyExistsException{  
       
        Mockito.when(contextRepository.findById(1L)).thenReturn(contextOptional);
        
        LoginResponse loginResponse = jwtService.authenticate(userLoginDTO);

        Challenge challengeResponse = challengeService.insert(jwtService.tokenBearerFormat(loginResponse.getToken()), challengeRegisterDTO, 1L);
        Challenge challengeResponse2 = challengeService.insert(jwtService.tokenBearerFormat(loginResponse.getToken()), challengeRegisterDTO2, 1L);
        
        List <Challenge> itensListEmpty = new ArrayList<>();
        List <Challenge> itensList = new ArrayList<>();
        List <Challenge> itensList2 = new ArrayList<>();
        List <Challenge> itensListAll = new ArrayList<>();

        Page emptyPage = new PageImpl<>(itensListEmpty, pageable, pageable.getPageSize());
        Page page;
        Page page2;
        Page pageAll;

        ServicesBuilder.insertSimulator(challengeResponse, itensList);
        page = new PageImpl<>(itensList, pageable, pageable.getPageSize());

        ServicesBuilder.insertSimulator(challengeResponse2, itensList2);
        page2 = new PageImpl<>(itensList2, pageable, pageable.getPageSize());        
        pageAll = new PageImpl<>(itensListAll, pageable, pageable.getPageSize());

        Mockito.when(challengeRepository.findByWordStartsWithIgnoreCase("inexistentChallengeWord", pageable)).thenReturn(emptyPage);
        Mockito.when(challengeRepository.findByWordStartsWithIgnoreCase("w", pageable)).thenReturn(page);
        Mockito.when(challengeRepository.findByWordStartsWithIgnoreCase("t", pageable)).thenReturn(page2);
        Mockito.when(challengeRepository.findAll(pageable)).thenReturn(pageAll);
        
        Page pageResponse1 = challengeService.findChallengesByParams("inexistentChallengeWord", emptyPage.getPageable());
        Page pageResponse2 = challengeService.findChallengesByParams("w", page.getPageable());
        Page pageResponse3 = challengeService.findChallengesByParams("t", page2.getPageable());
        Page pageResponse4 = challengeService.findChallengesByParams(null, pageAll.getPageable());
        
        assertEquals(true, emptyPage.isEmpty());
        assertEquals(challengeResponse, pageResponse2.getContent().get(0));
        assertEquals(challengeResponse2, pageResponse3.getContent().get(0));
        assertEquals(challenges, pageAll.getContent());

    }

}
