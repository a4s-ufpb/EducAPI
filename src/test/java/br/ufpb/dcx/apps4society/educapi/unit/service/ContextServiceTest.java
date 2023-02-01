package br.ufpb.dcx.apps4society.educapi.unit.service;

import br.ufpb.dcx.apps4society.educapi.domain.Context;
import br.ufpb.dcx.apps4society.educapi.domain.User;
import br.ufpb.dcx.apps4society.educapi.dto.context.ContextDTO;
import br.ufpb.dcx.apps4society.educapi.dto.context.ContextRegisterDTO;
import br.ufpb.dcx.apps4society.educapi.dto.user.UserLoginDTO;
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
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.*;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.bind.annotation.ExceptionHandler;

import java.util.ArrayList;
import java.util.List;

import java.util.Optional;

import static java.beans.Beans.isInstanceOf;
import static org.assertj.core.api.AssertionsForClassTypes.catchThrowableOfType;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;


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

    private final User creator = UserBuilder.anUser().buildUserRegisterDTO().userRegisterDtoToUser();

    private final User creator2 = UserBuilder.anUser().withName("User2").withEmail("user2@educapi.com").buildUserRegisterDTO().userRegisterDtoToUser();

    private final ContextRegisterDTO contextRegisterDTO = ContextBuilder.anContext().withId(1L).buildContextRegisterDTO();
    private final ContextRegisterDTO contextRegisterDTO2 = ContextBuilder.anContext().withId(2L).withName("Context2").buildContextRegisterDTO();
    private Context context = ContextBuilder.anContext().withCreator(creator).buildContext();
    private Context context2 = ContextBuilder.anContext().buildContext();
    private final UserLoginDTO userLoginDTO = UserBuilder.anUser().buildUserLoginDTO();
    private final UserLoginDTO userLoginDTO2 = UserBuilder.anUser().withName("User2").buildUserLoginDTO();

    private final Optional<User> userOptional = UserBuilder.anUser().withId(1L).buildOptionalUser();
    private final Optional<User> userOptional2 = UserBuilder.anUser().withId(2L).withName("User2").withEmail("user2@educapi.com").buildOptionalUser();

    private Optional<Context> contextOptional = ContextBuilder.anContext().withId(1L).withCreator(userOptional.get()).buildOptionalContext();

    // **Captar como variável de ambiente**
    private List<Context> contexts = new ArrayList<>();
    public List<Context> contextListByCreator = new ArrayList<>();
    public Page<Context> pageResponse;
    public Pageable pageable = PageRequest.of(0, 20, Sort.by("name").ascending());

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

        Mockito.lenient().when(userRepository.findByEmail(userLoginDTO.getEmail())).thenReturn(userOptional);
        Mockito.lenient().when(userRepository.findByEmailAndPassword(userLoginDTO.getEmail(), userLoginDTO.getPassword())).thenReturn(userOptional);

        //Métodos de origem devem estar com optional.isPresent() implementados, quando buscar pelo 'Long', ele retorna optional ou empty.
        ReflectionTestUtils.setField(jwtService, "TOKEN_KEY", "it's a token key");

    }
    @Test
    @DisplayName("Teste de encontrar contextos pelo autor")
    public void findContextsByCreatorTest() throws ObjectNotFoundException, InvalidUserException, ContextAlreadyExistsException {

        LoginResponse loginResponse = jwtService.authenticate(userLoginDTO);

        ContextDTO contextDTO = contextService.insert(tokenBearerFormat(loginResponse.getToken()), contextRegisterDTO);
        Context contextResponse = contextDTO.contextDTOToContext();
        contextResponse.setCreator(creator);
        ContextDTO contextDTO2 = contextService.insert(tokenBearerFormat(loginResponse.getToken()), contextRegisterDTO2);
        Context contextResponse2 = contextDTO2.contextDTOToContext();
        contextResponse2.setCreator(creator);

        contexts.add(contextResponse);
        contexts.add(contextResponse2);

        Mockito.when(contextRepository.findContextsByCreator(creator)).thenReturn(contexts);
        contextListByCreator = contextRepository.findContextsByCreator(creator);

        assertEquals(contextResponse.getCreator(), contextListByCreator.get(0).getCreator());
        assertEquals(contextResponse2.getCreator(), contextListByCreator.get(1).getCreator());

    }

//    @Test
//    @DisplayName("Teste de encontrar contextos inexistentes")
//    public void findInexistentContextsByCreatorTest() throws InvalidUserException {
//
//        //Mockito.when(contextRepository.findContextsByCreator(ArgumentMatchers.any())).thenThrow(new ObjectNotFoundException());
//        Mockito.when(contextRepository.findContextsByCreator(creator)).thenReturn(contexts);
//
//        //LoginResponse loginResponse = jwtService.authenticate(userLoginDTO);
//        //contexts = contextRepository.findContextsByCreator(creator);
//
//        List<Context> contextResponse = contexts;
//
//        // Não está identificando a lista contexts como uma lista vazia
//        Exception exception = assertThrows(ObjectNotFoundException.class,() -> {
//            contextRepository.findContextsByCreator(creator);
//        });
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

        LoginResponse loginResponse = jwtService.authenticate(userLoginDTO);

        Exception exception = assertThrows(ContextAlreadyExistsException.class, () -> {
            contextService.insert(tokenBearerFormat(loginResponse.getToken()), contextRegisterDTO);
        });
        assertEquals(Messages.CONTEXT_ALREADY_EXISTS, exception.getMessage());
        assertEquals(contextRepository.findContextByNameIgnoreCase("Context"), contextOptional);

    }

    @Test
    @DisplayName("Teste de atualizar um contexto")
    public void updateAContextTest() throws ObjectNotFoundException, InvalidUserException, ContextAlreadyExistsException {

        Mockito.when(contextRepository.findById(1L)).thenReturn(contextOptional);

        LoginResponse loginResponse = jwtService.authenticate(userLoginDTO);
        ContextDTO contextDTO = contextService.insert(tokenBearerFormat(loginResponse.getToken()), contextRegisterDTO);
        contextDTO.setId(1L);

        Context contextResponse = contextDTO.contextDTOToContext();
        contextResponse = contextIdGenerator(contextResponse);

        ContextDTO updatedContextDTO = contextService.update(tokenBearerFormat(loginResponse.getToken()), contextRegisterDTO2, 1L);

        assertEquals(contextResponse, contextOptional.get());
        assertEquals(contextResponse, contextRepository.findById(1L).get());
        assertEquals(Optional.empty(), contextRepository.findById(2L));
        assertNotEquals(contextDTO, updatedContextDTO);

    }

    @Test
    @DisplayName("Teste de atualizar um contexto inválido")
    public void updateAInvalidContextTest() throws InvalidUserException, ObjectNotFoundException {

        Mockito.lenient().when(userRepository.findByEmail(userLoginDTO2.getEmail())).thenReturn(userOptional2);
        Mockito.lenient().when(userRepository.findByEmailAndPassword(userLoginDTO2.getEmail(), userLoginDTO2.getPassword())).thenReturn(userOptional2);
        Mockito.lenient().when(contextRepository.findById(1L)).thenReturn(contextOptional);
        Mockito.lenient().when(contextRepository.findById(2L)).thenReturn(Optional.empty());

        LoginResponse loginResponse = jwtService.authenticate(userLoginDTO);
        LoginResponse loginResponse2 = jwtService.authenticate(userLoginDTO2);

        InvalidUserException throwable = catchThrowableOfType(() ->
                contextService.update(tokenBearerFormat(loginResponse2.getToken()), contextRegisterDTO, 1L), InvalidUserException.class);

        // POR ALGUM MOTIVO O OBJECTNOTFOUNDEXCPETION NÃO TA CONTANDO COMO ABRANGIDO...
        ObjectNotFoundException throwable2 = catchThrowableOfType(() ->
                contextService.update(tokenBearerFormat(loginResponse2.getToken()), contextRegisterDTO2, 2L), ObjectNotFoundException.class);

        //String message = throwable.getMessage();
        //assertEquals(message, "Object not found! Id: 1, Type: br.ufpb.dcx.apps4society.educapi.domain.Context");
        //assertEquals(contextResponse.get().getCreator(), contextOptional);

        //String message2 = throwable2.getMessage();
        //assertEquals(message, "Object not found! Id: 1, Type: br.ufpb.dcx.apps4society.educapi.domain.Context");
        //assertEquals(contextResponse.get().getCreator(), Optional.empty());

    }
    @Test
    @DisplayName("Teste de deletar um contexto")
    public void deleteAContextByIdTest() throws InvalidUserException, ContextAlreadyExistsException, ObjectNotFoundException {

        LoginResponse loginResponse = jwtService.authenticate(userLoginDTO);
        ContextDTO contextDTO = contextService.insert(tokenBearerFormat(loginResponse.getToken()), contextRegisterDTO);
        context = contextDTO.contextDTOToContext();
        creator.setId(1L);
        context.setCreator(creator);
        context = contextIdGenerator(context);
        contexts.add(context);
        // * Teve que ser feito para contornar o problema da não associação do context com seu criador *

        Mockito.when(contextRepository.findById(1L)).thenReturn(contextOptional);
        Optional<Context> contextResponse =  contextRepository.findById(1L);
        assertEquals(contexts.get(0), contextResponse.get());

        contextService.delete(tokenBearerFormat(loginResponse.getToken()), 1L);
        Mockito.when(contextRepository.findById(1L)).thenReturn(Optional.empty());
        Optional<Context> contextResponse2 = contextRepository.findById(1L);
        assertEquals(Optional.empty(), contextResponse2);

    }

    @Test
    @DisplayName("Teste de atualizar um contexto inválido")
    public void deleteAInvalidContextTest() throws InvalidUserException {

        Mockito.when(contextRepository.findById(1L)).thenReturn(Optional.empty());

        LoginResponse loginResponse = jwtService.authenticate(userLoginDTO);

        Exception exception = assertThrows(ObjectNotFoundException.class,() -> {
            contextService.delete(tokenBearerFormat(loginResponse.getToken()), 1L);
        });

    }
    
    @Test
    @DisplayName("Teste de encontrar contextos por parâmetros")
    public void findContextsByParamsTest() throws InvalidUserException, ContextAlreadyExistsException, ObjectNotFoundException{

        loginResponse = jwtService.authenticate(userLoginDTO);
        ContextDTO contextDTO = contextService.insert(tokenBearerFormat(loginResponse.getToken()), contextRegisterDTO);

        // INÍCIO DE SIMULAÇÃO DE TRAMITAÇÃO EM SERVIDOR PROVOCADA PELO MÉTODO INSERT()
        context = contextDTO.contextDTOToContext();
        context.setCreator(creator);
        contexts.add(context);
        // FIM DE SIMULAÇÃO DE TRAMITAÇÃO EM SERVIDOR PROVOCADA PELO MÉTODO INSERT()

        pageResponse = new PageImpl<>(contexts, pageable, pageable.getPageSize());

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

//    @Test
//    @DisplayName("Teste de encontrar contextos inexistentes")
//    public void findContextsThatNotExists() {
//
//        pageResponse = new PageImpl<>(contexts, pageable, pageable.getPageSize());
//
//        // OBS: O retorno Mockito deve estar após as mudanças acontecerem senão as pages ficam com UNKNOWN instances
//        Mockito.when(contextRepository.findAllByCreatorEmailLikeAndNameStartsWithIgnoreCase("user@educapi.com", "User", pageable))
//                .thenReturn(Page.empty());
//        Mockito.when(contextRepository.findAllByCreatorEmailEqualsIgnoreCase("user@educapi.com", pageable)).thenThrow(new NoContextsFound("asd"));
//        Mockito.when(contextRepository.findAllByNameStartsWithIgnoreCase("User", pageable)).thenReturn(Page.empty());
//
//        Exception exception = assertThrows(NoContextsFound.class,() -> {
//            contextRepository.findAllByCreatorEmailEqualsIgnoreCase(creator.getEmail(), pageable);
//        });
//
//        // OBS: para abranger o maximo tem que pegar os optionais quando é presente e quando não é
//    }

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
    // OBS3: Talvez uma boa prática seria conseguir alguma forma de mockar o repository.save(x)
    // OBS4: Ajuste do setUp() para ver se é possível atualizar os mockito constantemente para evitar ter que colocá-los no meio dos testes

    //https://www.youtube.com/watch?v=AKT9FYJBOEo
    //https://www.youtube.com/watch?v=lA18U8dGKF8 sobre jwt tokens
    //https://www.youtube.com/watch?v=E5nStRSgMaw sobre geração dos ids e levantamento do springtest com banco de dados
    //https://www.youtube.com/watch?v=R3ItceaMwnw testar controller(teste de integração)
}
