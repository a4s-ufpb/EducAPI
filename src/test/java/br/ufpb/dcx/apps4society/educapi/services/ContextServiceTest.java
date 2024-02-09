package br.ufpb.dcx.apps4society.educapi.services;

import br.ufpb.dcx.apps4society.educapi.domain.Context;
import br.ufpb.dcx.apps4society.educapi.domain.User;
import br.ufpb.dcx.apps4society.educapi.dto.context.ContextDTO;
import br.ufpb.dcx.apps4society.educapi.dto.context.ContextRegisterDTO;
import br.ufpb.dcx.apps4society.educapi.dto.user.UserLoginDTO;
import br.ufpb.dcx.apps4society.educapi.repositories.ContextRepository;
import br.ufpb.dcx.apps4society.educapi.repositories.UserRepository;
import br.ufpb.dcx.apps4society.educapi.response.LoginResponse;
import br.ufpb.dcx.apps4society.educapi.services.exceptions.*;
import br.ufpb.dcx.apps4society.educapi.utils.builder.ContextBuilder;
import br.ufpb.dcx.apps4society.educapi.utils.builder.ServicesBuilder;
import br.ufpb.dcx.apps4society.educapi.utils.builder.UserBuilder;

import br.ufpb.dcx.apps4society.educapi.util.Messages;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.*;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.ArrayList;
import java.util.List;

import java.util.Optional;
import java.util.stream.Collectors;

import static org.assertj.core.api.AssertionsForClassTypes.catchThrowableOfType;
import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
public class ContextServiceTest {

    @Mock
    UserRepository userRepository;
    @Mock
    ContextRepository contextRepository;

    @InjectMocks
    JWTService jwtService = ServicesBuilder.anService().withUserRepository(userRepository).buildJwtService();
    @InjectMocks
    ContextService contextService = ServicesBuilder.anService()
            .withJwtService(jwtService)
            .withContextRepository(contextRepository)
            .withUserRepository(userRepository).buildContextService();

    public User creator = UserBuilder.anUser().buildUserRegisterDTO().userRegisterDtoToUser();

    public User creator2 = UserBuilder.anUser().withName("User2").withEmail("user2@educapi.com").buildUserRegisterDTO().userRegisterDtoToUser();

    private final ContextRegisterDTO contextRegisterDTO = ContextBuilder.anContext().withId(1L).buildContextRegisterDTO();
    private final ContextRegisterDTO contextRegisterDTO2 = ContextBuilder.anContext().withId(2L).withName("Context2").buildContextRegisterDTO();
    private Context context = ContextBuilder.anContext().withCreator(creator).buildContext();
    private Context context2 = ContextBuilder.anContext().withName("Context2").withCreator(creator).buildContext();
    private final UserLoginDTO userLoginDTO = UserBuilder.anUser().buildUserLoginDTO();
    private final UserLoginDTO userLoginDTO2 = UserBuilder.anUser().withName("User2").withEmail(creator2.getEmail()).buildUserLoginDTO();
    private final UserLoginDTO userLoginDTO3 = UserBuilder.anUser().withName("User3").withEmail("").buildUserLoginDTO();

    private Optional<User> userOptional = UserBuilder.anUser().withId(1L).buildOptionalUser();
    private Optional<User> userOptional2 = UserBuilder.anUser().withId(2L).withName("User2").withEmail(creator2.getEmail()).buildOptionalUser();
    private Optional<User> userOptional3 = UserBuilder.anUser().withId(3L).withName("User3").withEmail(userLoginDTO3.getEmail()).buildOptionalUser();

    private Optional<Context> contextOptional = ContextBuilder.anContext().withId(1L).withCreator(userOptional.get()).buildOptionalContext();

    private List<Context> contexts = new ArrayList<>();
    private List<Context> contextListByCreator = new ArrayList<>();
    private List<ContextDTO> contextDTOListByCreator = new ArrayList<>();
    private Pageable pageable = PageRequest.of(0, 20, Sort.by("name").ascending());

    LoginResponse loginResponse;

    @BeforeEach
    public void setUp() {

        Mockito.lenient().when(userRepository.findByEmail(userLoginDTO.getEmail())).thenReturn(userOptional);
        Mockito.lenient().when(userRepository.findByEmailAndPassword(userLoginDTO.getEmail(), userLoginDTO.getPassword())).thenReturn(userOptional);

        ReflectionTestUtils.setField(jwtService, "TOKEN_KEY", "it's a token key");

    }

    @Test
    public void findTest() throws ObjectNotFoundException {

        Mockito.lenient().when(contextRepository.findById(1L)).thenReturn(Optional.empty());

        ObjectNotFoundException throwable = catchThrowableOfType(() ->
                contextService.find(1L), ObjectNotFoundException.class);

        String messageResponse = throwable.getMessage();
        assertEquals(messageResponse, "Object not found! Id: " + 1L + ", Type: br.ufpb.dcx.apps4society.educapi.domain.Context");
    }

    @Test
    public void findContextsByCreatorTest() throws ObjectNotFoundException, InvalidUserException{

        Mockito.when(contextRepository.findContextsByCreator(creator)).thenReturn(contextListByCreator);

        LoginResponse loginResponse = jwtService.authenticate(userLoginDTO);
        creator.setId(1L);

        ContextDTO contextDTO = contextService.insert(jwtService.tokenBearerFormat(loginResponse.getToken()), contextRegisterDTO);
        Context contextResponse = contextDTO.contextDTOToContext();
        contextResponse.setCreator(creator);
        ServicesBuilder.insertSimulator(contextResponse, contextListByCreator);

        ContextDTO contextDTO2 = contextService.insert(jwtService.tokenBearerFormat(loginResponse.getToken()), contextRegisterDTO2);
        Context contextResponse2 = contextDTO2.contextDTOToContext();
        contextResponse2.setCreator(creator);
        ServicesBuilder.insertSimulator(contextResponse2, contextListByCreator);

        contextDTOListByCreator = contextService.findContextsByCreator(jwtService.tokenBearerFormat(loginResponse.getToken()));
        contextListByCreator.stream().map(ContextDTO::new).collect(Collectors.toList());

        assertEquals(creator, contextListByCreator.get(0).getCreator());
        assertEquals(context.getName(), contextDTOListByCreator.get(0).getName());

        assertEquals(creator, contextListByCreator.get(1).getCreator());
        assertEquals(context2.getName(), contextDTOListByCreator.get(1).getName());

    }

    @Test
    public void findInexistentContextsByCreatorTest() throws InvalidUserException {

        Mockito.lenient().when(userRepository.findByEmail(userLoginDTO.getEmail())).thenReturn(userOptional);

        LoginResponse loginResponse = jwtService.authenticate(userLoginDTO);

        Mockito.lenient().when(userRepository.findByEmailAndPassword(userLoginDTO.getEmail(), userLoginDTO.getPassword()))
                .thenReturn(Optional.empty());

        catchThrowableOfType(() ->
                contextService.findContextsByCreator(jwtService.tokenBearerFormat(loginResponse.getToken())), ObjectNotFoundException.class);

    }

    @Test
    public void findContextsByInvalidCreatorTest() throws InvalidUserException {

        Mockito.lenient().when(userRepository.findByEmailAndPassword(userLoginDTO3.getEmail(), userLoginDTO3.getPassword())).thenReturn(userOptional3);
        Mockito.lenient().when(userRepository.findByEmail(userLoginDTO.getEmail())).thenReturn(Optional.empty());
        Mockito.lenient().when(userRepository.findByEmail(userLoginDTO3.getEmail())).thenReturn(userOptional3);

        LoginResponse loginResponse = jwtService.authenticate(userLoginDTO);
        LoginResponse loginResponse3 = jwtService.authenticate(userLoginDTO3);

        Mockito.lenient().when(userRepository.findByEmailAndPassword(userLoginDTO.getEmail(), userLoginDTO.getPassword()))
                .thenReturn(Optional.empty());

        catchThrowableOfType(() ->
                contextService.findContextsByCreator(jwtService.tokenBearerFormat(loginResponse3.getToken())), InvalidUserException.class);

        catchThrowableOfType(() ->
                contextService.findContextsByCreator(jwtService.tokenBearerFormat(loginResponse.getToken())), ObjectNotFoundException.class);

        catchThrowableOfType(() ->
                contextService.findContextsByCreator((loginResponse3.getToken())), SecurityException.class);

    }

    @Test
    public void insertAContextTest() throws  InvalidUserException, ObjectNotFoundException {

        Mockito.when(userRepository.findByEmailAndPassword(userLoginDTO.getEmail(), userLoginDTO.getPassword())).thenReturn(userOptional);
        Mockito.when(userRepository.findByEmail(userLoginDTO.getEmail())).thenReturn(userOptional);

        LoginResponse loginResponse = jwtService.authenticate(userLoginDTO);
        ContextDTO response = contextService.insert(jwtService.tokenBearerFormat(loginResponse.getToken()), contextRegisterDTO);

        assertNotNull(loginResponse.getToken());
        assertEquals(response.getName(),contextRegisterDTO.getName());
        assertEquals(response.getImageUrl(),contextRegisterDTO.getImageUrl());
        assertEquals(response.getSoundUrl(),contextRegisterDTO.getSoundUrl());
        assertEquals(response.getVideoUrl(),contextRegisterDTO.getVideoUrl());

    }

    @Test
    public void insertAContextAlreadyExistTest() throws InvalidUserException {

        Mockito.when(contextRepository.findContextByNameIgnoreCase(context.getName())).thenReturn(contextOptional);

        LoginResponse loginResponse = jwtService.authenticate(userLoginDTO);

        assertEquals(contextRepository.findContextByNameIgnoreCase("Context"), contextOptional);

    }

    @Test
    public void updateAContextTest() throws ObjectNotFoundException, InvalidUserException{

        Mockito.when(contextRepository.findById(1L)).thenReturn(contextOptional);

        LoginResponse loginResponse = jwtService.authenticate(userLoginDTO);
        ContextDTO contextDTO = contextService.insert(jwtService.tokenBearerFormat(loginResponse.getToken()), contextRegisterDTO);
        contextDTO.setId(1L);

        Context contextResponse = contextDTO.contextDTOToContext();
        contextResponse.setId(1L);

        ContextDTO updatedContextDTO = contextService.update(jwtService.tokenBearerFormat(loginResponse.getToken()), contextRegisterDTO2, 1L);

        assertEquals(contextResponse, contextOptional.get());
        assertEquals(contextResponse, contextRepository.findById(1L).get());
        assertEquals(Optional.empty(), contextRepository.findById(2L));
        assertNotEquals(contextDTO, updatedContextDTO);

    }

    @Test
    public void updateAInvalidContextTest() throws InvalidUserException, ObjectNotFoundException{

        Mockito.lenient().when(userRepository.findByEmail(userLoginDTO2.getEmail())).thenReturn(userOptional2);
        Mockito.lenient().when(userRepository.findByEmailAndPassword(userLoginDTO2.getEmail(), userLoginDTO2.getPassword())).thenReturn(userOptional2);
        Mockito.lenient().when(contextRepository.findById(1L)).thenReturn(contextOptional);
        Mockito.lenient().when(contextRepository.findById(2L)).thenReturn(Optional.empty());

        LoginResponse loginResponse = jwtService.authenticate(userLoginDTO);
        LoginResponse loginResponse2 = jwtService.authenticate(userLoginDTO2);
        contextService.insert(jwtService.tokenBearerFormat(loginResponse.getToken()), contextRegisterDTO);

        InvalidUserException throwable = catchThrowableOfType(() ->
                contextService.update(jwtService.tokenBearerFormat(loginResponse2.getToken()), contextRegisterDTO2, 1L), InvalidUserException.class);
        ObjectNotFoundException throwable2 = catchThrowableOfType(() ->
                contextService.update(jwtService.tokenBearerFormat(loginResponse.getToken()), contextRegisterDTO2, 2L), ObjectNotFoundException.class);

        String messageResponse = throwable.getMessage();
        assertEquals(messageResponse, "User: " + userOptional2.get().getName() + " is not the owner of the context: "
                + contextRegisterDTO.getName() + ".");

        String messageResponse2 = throwable2.getMessage();
        assertNull(messageResponse2);

    }

    @Test
    public void deleteAContextByIdTest() throws InvalidUserException,  ObjectNotFoundException {

        LoginResponse loginResponse = jwtService.authenticate(userLoginDTO);
        ContextDTO contextDTO = contextService.insert(jwtService.tokenBearerFormat(loginResponse.getToken()), contextRegisterDTO);
        context = contextDTO.contextDTOToContext();
        creator.setId(1L);
        context.setCreator(creator);
        context.setId(1L);
        ServicesBuilder.insertSimulator(context, contexts);

        Mockito.when(contextRepository.findById(1L)).thenReturn(contextOptional);
        Optional<Context> contextResponse =  contextRepository.findById(1L);
        assertEquals(contexts.get(0), contextResponse.get());

        contextService.delete(jwtService.tokenBearerFormat(loginResponse.getToken()), 1L);
        Mockito.when(contextRepository.findById(1L)).thenReturn(Optional.empty());
        Optional<Context> contextResponse2 = contextRepository.findById(1L);
        assertEquals(Optional.empty(), contextResponse2);

    }

    @Test
    public void deleteAInvalidContextTest() throws InvalidUserException,  ObjectNotFoundException {

        Mockito.lenient().when(userRepository.findByEmail(userLoginDTO2.getEmail())).thenReturn(userOptional2);
        Mockito.lenient().when(userRepository.findByEmailAndPassword(userLoginDTO2.getEmail(), userLoginDTO2.getPassword())).thenReturn(userOptional2);
        Mockito.lenient().when(contextRepository.findById(1L)).thenReturn(contextOptional);
        Mockito.lenient().when(contextRepository.findById(2L)).thenReturn(Optional.empty());

        LoginResponse loginResponse = jwtService.authenticate(userLoginDTO);
        LoginResponse loginResponse2 = jwtService.authenticate(userLoginDTO2);
        contextService.insert(jwtService.tokenBearerFormat(loginResponse.getToken()), contextRegisterDTO);

        InvalidUserException throwable = catchThrowableOfType(() ->
                contextService.delete(jwtService.tokenBearerFormat(loginResponse2.getToken()), 1L), InvalidUserException.class);

        ObjectNotFoundException throwable2 = catchThrowableOfType(() ->
                contextService.delete(jwtService.tokenBearerFormat(loginResponse.getToken()), 2L), ObjectNotFoundException.class);

        String messageResponse = throwable.getMessage();
        String messageResponse2 = throwable2.getMessage();
        assertEquals(messageResponse, "User: " + userOptional2.get().getName() + " is not the owner of the context: "
                + contextOptional.get().getName() + ".");
        assertNull(messageResponse2);

    }
    
    @Test
    public void findContextsByParamsTest() throws InvalidUserException,  ObjectNotFoundException{
        
        loginResponse = jwtService.authenticate(userLoginDTO);
        ContextDTO contextDTO = contextService.insert(jwtService.tokenBearerFormat(loginResponse.getToken()), contextRegisterDTO);

        context = contextDTO.contextDTOToContext();
        context.setCreator(creator);
        ServicesBuilder.insertSimulator(context, contexts);
        context2 = contextDTO.contextDTOToContext();
        context2.setCreator(creator2);
        ServicesBuilder.insertSimulator(context2, contexts);        
        
        Page<Context> page = new PageImpl<>(contexts, pageable, pageable.getPageSize());

        Mockito.when(contextRepository.findAllByCreatorEmailLikeAndNameStartsWithIgnoreCase("user@educapi.com", "User", pageable))
        .thenReturn(page);
        Mockito.when(contextRepository.findAllByCreatorEmailEqualsIgnoreCase("user@educapi.com", pageable)).thenReturn(page);
        Mockito.when(contextRepository.findAllByNameStartsWithIgnoreCase("User", pageable)).thenReturn(page);
        Mockito.lenient().when(contextRepository.findAll(pageable)).thenReturn(page);
        
        assertEquals(page, contextService.findContextsByParams("user@educapi.com", "User", pageable));
        assertEquals(page, contextService.findContextsByParams("user@educapi.com", null, pageable));
        assertEquals(page, contextService.findContextsByParams( null, "User", pageable));
        assertEquals(page, contextService.findContextsByParams(null, null, pageable));

        // WARN: EACH SEARCH RESULTS MUST BE AN INDEPENDENT PAGE

    }

}
