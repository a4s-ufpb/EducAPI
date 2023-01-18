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

import java.security.Key;
import java.util.ArrayList;
import java.util.List;

import java.util.Optional;

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
    JWTService jwtService = ServicesBuilder.anService().withUserRepository(this.userRepository).buildJwtService();
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

    private final Pageable pageable = PageRequest.of(0, 20);
    private final List<Context> contexts = new ArrayList<>();
    private Page<Context> page = new PageImpl<>(contexts, pageable, pageable.getPageSize());

    private String tokenBearerFormat(String token){
        this.TOKEN_KEY = "Bearer " + token;
        return this.TOKEN_KEY;
    }

    @BeforeEach
    public void setUp() {

        ReflectionTestUtils.setField(jwtService, "TOKEN_KEY", "it's a token key");

    }
//    @Test
//    @DisplayName("Teste de encontrar um contexto pelo autor")
//    public void findContextsByCreatorTest() throws InvalidContextException, ObjectNotFoundException, InvalidUserException, ContextAlreadyExistsException {
//
//        Mockito.when(this.userRepository.findByEmailAndPassword(this.userLoginDTO.getEmail(), this.userLoginDTO.getPassword())).thenReturn(this.userOptional);
//        Mockito.when(this.userRepository.findByEmail(this.userLoginDTO.getEmail())).thenReturn(this.userOptional);
//        //Mockito.when(this.contextRepository.findContextsByCreator(this.creator)).thenReturn(contexts);
//
//        LoginResponse loginResponse = this.jwtService.authenticate(this.userLoginDTO);
//        String bearedToken = this.tokenBearerFormat(loginResponse.getToken());
//        this.contextService.insert(bearedToken, this.contextRegisterDTO);
//        this.contextService.insert(bearedToken, this.contextRegisterDTO);
//
//        assertEquals(contextRepository.getOne(1L).getCreator(), this.creator);
//        assertEquals(contextRepository.getOne(2L).getCreator(), this.creator);
//
//    }
    @Test
    @DisplayName("Teste de inserir um contexto")
    public void insertAContextTest() throws ContextAlreadyExistsException, InvalidUserException, ObjectNotFoundException {

        Mockito.when(this.userRepository.findByEmailAndPassword(this.userLoginDTO.getEmail(), this.userLoginDTO.getPassword())).thenReturn(this.userOptional);
        Mockito.when(this.userRepository.findByEmail(this.userLoginDTO.getEmail())).thenReturn(this.userOptional);

        LoginResponse loginResponse = this.jwtService.authenticate(this.userLoginDTO);
        ContextDTO response = this.contextService.insert(tokenBearerFormat(loginResponse.getToken()), this.contextRegisterDTO);

        assertNotNull(loginResponse.getToken());
        assertEquals(response.getName(),this.contextRegisterDTO.getName());
        assertEquals(response.getImageUrl(),this.contextRegisterDTO.getImageUrl());
        assertEquals(response.getSoundUrl(),this.contextRegisterDTO.getSoundUrl());
        assertEquals(response.getVideoUrl(),this.contextRegisterDTO.getVideoUrl());

    }

    @Test
    @DisplayName("Teste de inserir um contexto já existente")
    public void insertAContextAlreadyExistTest() throws InvalidUserException {

        Mockito.when(contextRepository.findContextByNameIgnoreCase(this.context.getName())).thenReturn(contextOptional);
        Mockito.when(this.userRepository.findByEmail(this.userLoginDTO.getEmail())).thenReturn(this.userOptional);
        Mockito.when(this.userRepository.findByEmailAndPassword(this.userLoginDTO.getEmail(), this.userLoginDTO.getPassword()))
                .thenReturn(this.userOptional);

        LoginResponse loginResponse = this.jwtService.authenticate(this.userLoginDTO);

        Exception exception = assertThrows(ContextAlreadyExistsException.class, () -> {
            contextService.insert(tokenBearerFormat(loginResponse.getToken()), this.contextRegisterDTO);
        });
        assertEquals(Messages.CONTEXT_ALREADY_EXISTS, exception.getMessage());
        assertEquals(contextRepository.findContextByNameIgnoreCase("Context"), contextOptional);

    }

//    @Test
//    @DisplayName("Teste de atualizar um contexto")
//    public void updateAContextTest() throws ObjectNotFoundException, InvalidUserException, ContextAlreadyExistsException {
//
//        //Mockito.when(this.contextRepository.findContextByNameIgnoreCase(this.context.getName())).thenReturn(contextOptional);
//        Mockito.when(this.userRepository.findByEmail(this.userLoginDTO.getEmail())).thenReturn(this.userOptional);
//        Mockito.when(userRepository.findByEmailAndPassword(userLoginDTO.getEmail(), userLoginDTO.getPassword())).thenReturn(userOptional);
//
//        LoginResponse loginResponse = this.jwtService.authenticate(this.userLoginDTO);
//        contextService.insert(tokenBearerFormat(loginResponse.getToken()), contextRegisterDTO);
//        //assertEquals(this.contextRegisterDTO, this.contextOptional.get().getId());
//        assertEquals(contextRepository.findById(1L), this.contextOptional);
//        contextService.update(tokenBearerFormat(loginResponse.getToken()), contextRegisterDTO, 1L);
//
//    }

//    @Test
//    @DisplayName("Teste de deletar um contexto")
//    public void deleteAContextByIdTest() throws InvalidUserException, ContextAlreadyExistsException, ObjectNotFoundException, UserAlreadyExistsException {
//
//        Mockito.when(this.userRepository.findByEmailAndPassword(this.userLoginDTO.getEmail(), this.userLoginDTO.getPassword())).thenReturn(this.userOptional);
//        LoginResponse loginResponse = this.jwtService.authenticate(this.userLoginDTO);
//
//        contextService.insert(loginResponse.getToken(), contextRegisterDTO);
//        Mockito.when(this.contextRepository.findById(this.context.getId())).thenReturn(this.contextOptional);
//
//        contextService.delete(loginResponse.getToken(), this.context.getId());
//
//        Mockito.verify(this.contextRepository.findById(this.context.getId()));
//        assertThrows(ObjectNotFoundException.class, () -> {
//            contextService.find(this.context.getId());
//        });
//    }

    @Test
    @DisplayName("Teste de encontrar um contexto por parâmetros")
    public void findContextsByParamsTest() throws UserAlreadyExistsException, InvalidUserException, ContextAlreadyExistsException, ObjectNotFoundException{

        Mockito.when(this.userRepository.findByEmail(this.userLoginDTO.getEmail())).thenReturn(this.userOptional);
        Mockito.when(userRepository.findByEmailAndPassword(userLoginDTO.getEmail(), userLoginDTO.getPassword())).thenReturn(userOptional);
        Mockito.when(contextRepository.findAllByCreatorEmailLikeAndNameStartsWithIgnoreCase(creator.getEmail(), creator.getName(), pageable))
                .thenReturn(this.page);

        LoginResponse loginResponse = this.jwtService.authenticate(this.userLoginDTO);
        contextService.insert(tokenBearerFormat(loginResponse.getToken()), contextRegisterDTO);

        //creator do contextOptional ta nulo
        page = contextService.findContextsByParams(this.contextOptional.get().getCreator().getEmail(), this.contextOptional.get().getName(), pageable);

        assertEquals(this.contextRepository.findAll(), page);
        assertNotNull(contextRepository.findAllByCreatorEmailLikeAndNameStartsWithIgnoreCase("user@educapi.com", "User", pageable));
        assertNotNull(contextRepository.findAllByCreatorEmailEqualsIgnoreCase("user@educapi.com", pageable));
        assertNotNull(contextRepository.findAllByNameStartsWithIgnoreCase("User", pageable));
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
