package br.ufpb.dcx.apps4society.educapi.unit.service;

import br.ufpb.dcx.apps4society.educapi.domain.Context;
import br.ufpb.dcx.apps4society.educapi.domain.User;
import br.ufpb.dcx.apps4society.educapi.dto.context.ContextDTO;
import br.ufpb.dcx.apps4society.educapi.dto.context.ContextRegisterDTO;
import br.ufpb.dcx.apps4society.educapi.dto.user.UserLoginDTO;
import br.ufpb.dcx.apps4society.educapi.dto.user.UserRegisterDTO;
import br.ufpb.dcx.apps4society.educapi.repositories.ContextRepository;
import br.ufpb.dcx.apps4society.educapi.repositories.UserRepository;
import br.ufpb.dcx.apps4society.educapi.response.LoginResponse;
import br.ufpb.dcx.apps4society.educapi.services.ContextService;
import br.ufpb.dcx.apps4society.educapi.services.JWTService;
import br.ufpb.dcx.apps4society.educapi.services.UserService;
import br.ufpb.dcx.apps4society.educapi.services.exceptions.*;
import br.ufpb.dcx.apps4society.educapi.unit.domain.builder.ContextBuilder;
import br.ufpb.dcx.apps4society.educapi.unit.domain.builder.ServicesBuilder;
import br.ufpb.dcx.apps4society.educapi.unit.domain.builder.UserBuilder;

import br.ufpb.dcx.apps4society.educapi.util.Messages;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.impl.crypto.MacProvider;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.bind.annotation.RequestParam;

import java.security.Key;
import java.util.ArrayList;
import java.util.List;

import java.util.Optional;

import javax.management.Query;
import javax.persistence.EntityManager;

import static org.junit.jupiter.api.Assertions.*;


@ExtendWith(MockitoExtension.class)
@WebAppConfiguration
@DisplayName("ContextServiceTest")
public class ContextServiceTest {

    @Mock
    UserRepository userRepository;
    @Mock
    ContextRepository contextRepository;

    @InjectMocks
    JWTService jwtService = ServicesBuilder.anService().withUserRepository(userRepository).buildJwtService();
    @InjectMocks
    UserService userService = ServicesBuilder.anService()
            .withJwtService(jwtService)
            .withUserRepository(userRepository).buildUserService();
    @InjectMocks
    ContextService contextService = ServicesBuilder.anService()
            .withJwtService(jwtService)
            .withContextRepository(contextRepository)
            .withUserRepository(userRepository).buildContextService();

    @Value("${app.token.key}")
    private String TOKEN_KEY;

    private final ContextRegisterDTO contextRegisterDTO = ContextBuilder.anContext().buildContextRegisterDTO();
    private final ContextRegisterDTO contextRegisterDTO2 = ContextBuilder.anContext().withName("Context2").buildContextRegisterDTO();
    private final Context context = ContextBuilder.anContext().buildContextRegisterDTO().contextRegisterDTOToContext();
    private final User creator = UserBuilder.anUser().buildUserRegisterDTO().userRegisterDtoToUser();
    private final UserLoginDTO userLoginDTO = UserBuilder.anUser().buildUserLoginDTO();

    private Optional<Context> contextOptional = ContextBuilder.anContext().withCreator(creator).buildOptionalContext();
    private final Optional<User> userOptional = UserBuilder.anUser().withId(1L).buildOptionalUser();

    // **Captar com variável de ambiente**
    public Pageable pageable = PageRequest.of(0, 20);
    public List<Context> contexts = new ArrayList<>();
    //Page<Context> page = new PageImpl<>(contexts, pageable, pageable.getPageSize());

    private String tokenBearerFormat(String token){
        TOKEN_KEY = "Bearer " + token;
        return TOKEN_KEY;
    }

    @BeforeEach
    public void setUp() {

        ReflectionTestUtils.setField(jwtService, "TOKEN_KEY", "it's a token key");

    }
//    @Test
//    @DisplayName("Teste de encontrar um contexto pelo autor")
//    public void findContextsByCreatorTest() throws InvalidContextException, ObjectNotFoundException, InvalidUserException, ContextAlreadyExistsException {
//
//        Mockito.when(userRepository.findByEmailAndPassword(userLoginDTO.getEmail(), userLoginDTO.getPassword())).thenReturn(userOptional);
//        Mockito.when(userRepository.findByEmail(userLoginDTO.getEmail())).thenReturn(userOptional);
//        //Mockito.when(contextRepository.findContextsByCreator(creator)).thenReturn(contexts);
//
//        LoginResponse loginResponse = jwtService.authenticate(userLoginDTO);
//        String bearedToken = tokenBearerFormat(loginResponse.getToken());
//        contextService.insert(bearedToken, contextRegisterDTO);
//        contextService.insert(bearedToken, contextRegisterDTO);
//
//        assertEquals(contextRepository.getOne(1L).getCreator(), creator);
//        assertEquals(contextRepository.getOne(2L).getCreator(), creator);
//
//    }
    @Test
    @DisplayName("Teste de inserir um contexto")
    public void insertAContextTest() throws ContextAlreadyExistsException, InvalidUserException, ObjectNotFoundException {

        Mockito.when(userRepository.findByEmailAndPassword(userLoginDTO.getEmail(), userLoginDTO.getPassword())).thenReturn(userOptional);
        Mockito.when(userRepository.findByEmail(userLoginDTO.getEmail())).thenReturn(userOptional);

        LoginResponse loginResponse = jwtService.authenticate(userLoginDTO);
        ContextDTO response = contextService.insert(tokenBearerFormat(loginResponse.getToken()), contextRegisterDTO);

        assertNotNull(loginResponse.getToken());
        assertEquals(response.getName(),contextRegisterDTO.getName());
        assertEquals(response.getImageUrl(),contextRegisterDTO.getImageUrl());
        assertEquals(response.getSoundUrl(),contextRegisterDTO.getSoundUrl());
        assertEquals(response.getVideoUrl(),contextRegisterDTO.getVideoUrl());

    }

    @Test
    @DisplayName("Teste de inserir um contexto já existente")
    public void insertAContextAlreadyExistTest() throws InvalidUserException {

        Mockito.when(contextRepository.findContextByNameIgnoreCase(context.getName())).thenReturn(contextOptional);
        Mockito.when(userRepository.findByEmail(userLoginDTO.getEmail())).thenReturn(userOptional);
        Mockito.when(userRepository.findByEmailAndPassword(userLoginDTO.getEmail(), userLoginDTO.getPassword()))
                .thenReturn(userOptional);

        LoginResponse loginResponse = jwtService.authenticate(userLoginDTO);

        Exception exception = assertThrows(ContextAlreadyExistsException.class, () -> {
            contextService.insert(tokenBearerFormat(loginResponse.getToken()), contextRegisterDTO);
        });
        assertEquals(Messages.CONTEXT_ALREADY_EXISTS, exception.getMessage());
        assertEquals(contextRepository.findContextByNameIgnoreCase("Context"), contextOptional);

    }
//    @Test
//    @DisplayName("Teste de atualizar um contexto")
//    public void updateAContextTest() throws ObjectNotFoundException, InvalidUserException, ContextAlreadyExistsException {
//
//        //Mockito.when(contextRepository.findContextByNameIgnoreCase(context.getName())).thenReturn(contextOptional);
//        Mockito.when(userRepository.findByEmail(userLoginDTO.getEmail())).thenReturn(userOptional);
//        Mockito.when(userRepository.findByEmailAndPassword(userLoginDTO.getEmail(), userLoginDTO.getPassword())).thenReturn(userOptional);
//
//        LoginResponse loginResponse = jwtService.authenticate(userLoginDTO);
//        contextService.insert(tokenBearerFormat(loginResponse.getToken()), contextRegisterDTO);
//        //assertEquals(contextRegisterDTO, contextOptional.get().getId());
//        assertEquals(contextRepository.findById(1L), contextOptional);
//        contextService.update(tokenBearerFormat(loginResponse.getToken()), contextRegisterDTO, 1L);
//
//    }
//
//    @Test
//    @DisplayName("Teste de deletar um contexto")
//    public void deleteAContextByIdTest() throws InvalidUserException, ContextAlreadyExistsException, ObjectNotFoundException, UserAlreadyExistsException {
//
//        Mockito.when(userRepository.findByEmailAndPassword(userLoginDTO.getEmail(), userLoginDTO.getPassword())).thenReturn(userOptional);
//        LoginResponse loginResponse = jwtService.authenticate(userLoginDTO);
//
//        contextService.insert(loginResponse.getToken(), contextRegisterDTO);
//        Mockito.when(contextRepository.findById(context.getId())).thenReturn(contextOptional);
//
//        contextService.delete(loginResponse.getToken(), context.getId());
//
//        Mockito.verify(contextRepository.findById(context.getId()));
//        assertThrows(ObjectNotFoundException.class, () -> {
//            contextService.find(context.getId());
//        });
//    }
//
//    public Page<Context> getContexts(String email, String nome, Pageable pageable){
//
//        pageable = PageRequest.of(0, 20);
//        List<Context> contexts = new ArrayList<>();
//        Page<Context> page = new PageImpl<>(contexts, pageable, pageable.getPageSize());
//        contextService.findContextsByParams(email, nome, pageable);//        return page;
//    }
    @Test
    @DisplayName("Teste de encontrar um contexto por parâmetros")
    public void findContextsByParamsTest() throws InvalidUserException, ContextAlreadyExistsException, ObjectNotFoundException{

        Page<Context> page = new PageImpl<>(contexts, pageable, pageable.getPageSize());
        List<Context> contexts = new ArrayList<>(C)

        Mockito.when(userRepository.findByEmail(userLoginDTO.getEmail())).thenReturn(userOptional);
        Mockito.when(userRepository.findByEmailAndPassword(userLoginDTO.getEmail(), userLoginDTO.getPassword())).thenReturn(userOptional);

        Mockito.when(contextRepository.findAllByCreatorEmailLikeAndNameStartsWithIgnoreCase(creator.getEmail(), creator.getName(), pageable))
                .thenReturn(page);
        Mockito.when(contextRepository.findAllByCreatorEmailEqualsIgnoreCase(creator.getEmail(), pageable)).thenReturn(page);
        Mockito.when(contextRepository.findAllByNameStartsWithIgnoreCase(creator.getName(), pageable)).thenReturn(page);
        Mockito.when(contextRepository.findAll()).thenReturn(contexts);

        LoginResponse loginResponse = jwtService.authenticate(userLoginDTO);
        ContextDTO contextDTO = contextService.insert(tokenBearerFormat(loginResponse.getToken()), contextRegisterDTO);
        ContextDTO contextDTO2 = contextService.insert(tokenBearerFormat(loginResponse.getToken()), contextRegisterDTO);
        contexts.add(contextDTO.contextDTOToContext());
        contexts.add(contextDTO2.contextDTOToContext());
        Page<Context> pageResponse = new PageImpl<>(contexts, pageable, pageable.getPageSize());

        // O page não adiciona objetos no content e da UNKNOWN instance
        Page<Context> pageSoParaTest =
                contextRepository.findAllByCreatorEmailLikeAndNameStartsWithIgnoreCase("user@educapi.com", "User", pageResponse.getPageable());

        assertNotNull(contextRepository.findAllByCreatorEmailLikeAndNameStartsWithIgnoreCase(
                "user@educapi.com", "User", pageResponse.getPageable()));

        assertEquals(pageResponse, contextRepository.findAllByCreatorEmailEqualsIgnoreCase("user@educapi.com", pageResponse.getPageable()));
        assertEquals(pageResponse, contextRepository.findAllByNameStartsWithIgnoreCase("User", pageResponse.getPageable()));
        assertNotNull(contextRepository.findAll());

    }

    public void template(){
        // *** Arrange(new, sets, mockito.when...thenReturn())
        // Then(buscar, salvar, inserir, deletar)
        // Assertions

        //ADICIONAL: .lenient() faz parar de reclamar de 'unecessary stubbins'
        // .thenReturn() é um como se fosse um retorno fake
        // Mock Instancia com valor nulo
        // InjectMock Classe a qual as injeções dos mocks serão aplicadas***
    }

    // Não sei se esse é o jeito correto de passa o pageable no construtor
    // os temas sao contexto, q tem um conjunto de challenge q sao palavras
    // No app a procura é feita pelo id e nos testes são feitas por e-mail, author e nome?!
    // OBS: não enganchar e ir fazendo o que da para fazer
    // OBS1: passar tokens para variáveis de ambiente e ao fazer os testes so fazer a chamada deles
    //https://www.youtube.com/watch?v=AKT9FYJBOEo
    //https://www.youtube.com/watch?v=lA18U8dGKF8 sobre jwt tokens
    //https://www.youtube.com/watch?v=E5nStRSgMaw sobre geração dos ids e levantamento do springtest com banco de dados
}
