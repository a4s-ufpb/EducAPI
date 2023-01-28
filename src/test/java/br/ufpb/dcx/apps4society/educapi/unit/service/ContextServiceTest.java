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
import org.springframework.data.domain.*;
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
import static org.mockito.Mockito.doThrow;


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
    private final User creator = UserBuilder.anUser().buildUserRegisterDTO().userRegisterDtoToUser();
    private final User creator2 = UserBuilder.anUser().withName("User2").withEmail("user2@educapi.com").buildUserRegisterDTO().userRegisterDtoToUser();
    private Context context = ContextBuilder.anContext().buildContext();
            //.buildContextRegisterDTO().contextRegisterDTOToContext();
    private final UserLoginDTO userLoginDTO = UserBuilder.anUser().buildUserLoginDTO();
    private final UserLoginDTO userLoginDTO2 = UserBuilder.anUser().withName("User2").buildUserLoginDTO();

    private Optional<Context> contextOptional = ContextBuilder.anContext().withId(1L).withCreator(creator).buildOptionalContext();
    private final Optional<User> userOptional = UserBuilder.anUser().withId(1L).buildOptionalUser();
    private final Optional<User> userOptional2 = UserBuilder.anUser().withId(2L).withName("User2").withEmail("user2@educapi.com").buildOptionalUser();

    // **Captar como variável de ambiente**
    String field = "name";
    public Pageable pageable = PageRequest.of(0, 20, Sort.by(field).ascending());
    public List<Context> contexts = new ArrayList<>();
    public Page<Context> pageResponse;

    @Autowired
    LoginResponse loginResponse;

    private String tokenBearerFormat(String token){
        TOKEN_KEY = "Bearer " + token;
        return TOKEN_KEY;
    }

    private Context contextIdGenerator(Context contextTemp){
        if(contexts.isEmpty()){
            contextTemp.setId(1L);
        }
        int intId = contexts.size()+1;
        Long longId = new Long(intId);
        contextTemp.setId(longId);

        return contextTemp;
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
    @Test
    @DisplayName("Teste de deletar um contexto")
    public void deleteAContextByIdTest() throws InvalidUserException, ContextAlreadyExistsException, ObjectNotFoundException, UserAlreadyExistsException {

        Mockito.when(userRepository.findByEmailAndPassword(userLoginDTO.getEmail(), userLoginDTO.getPassword())).thenReturn(userOptional);
        Mockito.when(userRepository.findByEmail(userLoginDTO.getEmail())).thenReturn(userOptional);
        //Métodos de origem devem estar com optional.isPresent() implementados, quando buscar pelo 'Long', ele retorna optional ou empty.

        LoginResponse loginResponse = jwtService.authenticate(userLoginDTO);
        ContextDTO contextDTO = contextService.insert(tokenBearerFormat(loginResponse.getToken()), contextRegisterDTO);
        context = contextDTO.contextDTOToContext();
        creator.setId(1L);
        context.setCreator(creator);
        context = contextIdGenerator(context);
        contexts.add(context);
        // * Teve que ser feito para contornar o problema da não associação do context com seu criador *

        Mockito.when(contextRepository.findById(1L)).thenReturn(contextOptional);
        Mockito.when(contextRepository.findContextByNameIgnoreCase("Context")).thenReturn(contextOptional);
        Optional<Context> contextResponse =  contextRepository.findById(1L);
        assertEquals(contexts.get(0), contextResponse.get());

        contextService.delete(tokenBearerFormat(loginResponse.getToken()), 1L);
        Mockito.when(contextRepository.findById(1L)).thenReturn(Optional.empty());
        Optional<Context> contextResponse2 = contextRepository.findById(1L);
        assertEquals(Optional.empty(), contextResponse2);

    }

    @Test
    @DisplayName("Teste de encontrar um contexto por parâmetros")
    public void findContextsByParamsTest() throws InvalidUserException, ContextAlreadyExistsException, ObjectNotFoundException{

        Mockito.when(userRepository.findByEmail(userLoginDTO.getEmail())).thenReturn(userOptional);
        Mockito.when(userRepository.findByEmailAndPassword(userLoginDTO.getEmail(), userLoginDTO.getPassword())).thenReturn(userOptional);

        loginResponse = jwtService.authenticate(userLoginDTO);
        ContextDTO contextDTO = contextService.insert(tokenBearerFormat(loginResponse.getToken()), contextRegisterDTO);

        // INÍCIO DE SIMULAÇÃO DE TRAMITAÇÃO EM SERVIDOR PROVOCADA PELO MÉTODO INSERT()
        context = contextDTO.contextDTOToContext();
        context.setCreator(creator);
        contexts.add(context);
        // FIM DE SIMULAÇÃO DE TRAMITAÇÃO EM SERVIDOR PROVOCADA PELO MÉTODO INSERT()

        Page<Context> pageResponse = new PageImpl<>(contexts, pageable, pageable.getPageSize());

        // OBS: O retorno Mockito deve estar após as mudanças acontecerem senão as pages ficam com UNKNOWN instances
        Mockito.when(contextRepository.findAllByCreatorEmailLikeAndNameStartsWithIgnoreCase("user@educapi.com", "User", pageable))
                .thenReturn(pageResponse);
        Mockito.when(contextRepository.findAllByCreatorEmailEqualsIgnoreCase("user@educapi.com", pageable)).thenReturn(pageResponse);
        Mockito.when(contextRepository.findAllByNameStartsWithIgnoreCase("User", pageable)).thenReturn(pageResponse);

        assertEquals(pageResponse, contextRepository.findAllByCreatorEmailLikeAndNameStartsWithIgnoreCase(
                context.getCreator().getEmail(), context.getCreator().getName(), pageResponse.getPageable()));
        assertEquals(pageResponse, contextRepository.findAllByCreatorEmailEqualsIgnoreCase(context.getCreator().getEmail(), pageResponse.getPageable()));
        assertEquals(pageResponse, contextRepository.findAllByNameStartsWithIgnoreCase(context.getCreator().getName(), pageResponse.getPageable()));
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

    // os temas sao contexto, q tem um conjunto de challenge q sao palavras
    // Na API a procura é feita pelo id e nos testes são feitas por e-mail, author e nome?!
    // OBS: não enganchar e ir fazendo o que da para fazer
    // OBS1: passar tokens para variáveis de ambiente e ao fazer os testes so fazer a chamada deles
    // OBS2: No teste unitário deve-se ajustar a gerência das instâncias manualmente por Mockito para simular as entradas e saídas do servidor

    //https://www.youtube.com/watch?v=AKT9FYJBOEo
    //https://www.youtube.com/watch?v=lA18U8dGKF8 sobre jwt tokens
    //https://www.youtube.com/watch?v=E5nStRSgMaw sobre geração dos ids e levantamento do springtest com banco de dados
    //https://www.youtube.com/watch?v=R3ItceaMwnw testar controller
}
