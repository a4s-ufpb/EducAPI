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
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.*;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.bind.annotation.ExceptionHandler;

import javax.swing.text.html.parser.Entity;
import java.util.ArrayList;
import java.util.List;

import java.util.Optional;
import java.util.stream.Collectors;

import static org.assertj.core.api.AssertionsForClassTypes.catchThrowableOfType;
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

    public User creator = UserBuilder.anUser().buildUserRegisterDTO().userRegisterDtoToUser();

    public User creator2 = UserBuilder.anUser().withName("User2").withEmail("user2@educapi.com").buildUserRegisterDTO().userRegisterDtoToUser();

    private final ContextRegisterDTO contextRegisterDTO = ContextBuilder.anContext().withId(1L).buildContextRegisterDTO();
    private final ContextRegisterDTO contextRegisterDTO2 = ContextBuilder.anContext().withId(2L).withName("Context2").buildContextRegisterDTO();
    private Context context = ContextBuilder.anContext().withCreator(creator).buildContext();
    private Context context2 = ContextBuilder.anContext().withName("Context2").withCreator(creator).buildContext();
    private final UserLoginDTO userLoginDTO = UserBuilder.anUser().buildUserLoginDTO();
    private final UserLoginDTO userLoginDTO2 = UserBuilder.anUser().withName("User2").buildUserLoginDTO();
    private final UserLoginDTO userLoginDTO3 = UserBuilder.anUser().withName("User3").withEmail("").buildUserLoginDTO();

    private Optional<User> userOptional = UserBuilder.anUser().withId(1L).buildOptionalUser();
    private Optional<User> userOptional2 = UserBuilder.anUser().withId(2L).withName("User2").withEmail("user2@educapi.com").buildOptionalUser();
    private Optional<User> userOptional3 = UserBuilder.anUser().withId(3L).withName("User3").withEmail(userLoginDTO3.getEmail()).buildOptionalUser();

    private Optional<Context> contextOptional = ContextBuilder.anContext().withId(1L).withCreator(userOptional.get()).buildOptionalContext();

    // **Captar como variável de ambiente**
    private List<Context> contexts = new ArrayList<>();
    public List<ContextDTO> contextListByCreator = new ArrayList<>();
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
    @DisplayName("Encontrar contextos")
    public void findTest() throws ObjectNotFoundException {

        Mockito.lenient().when(contextRepository.findById(1L)).thenReturn(Optional.empty());

        ObjectNotFoundException throwable = catchThrowableOfType(() ->
                contextService.find(1L), ObjectNotFoundException.class);

        String messageResponse = throwable.getMessage();
        assertEquals(messageResponse, "Object not found! Id: " + 1L + ", Type: br.ufpb.dcx.apps4society.educapi.domain.Context");
    }

    @Test
    @DisplayName("Encontrar contextos por criador")
    public void findContextsByCreatorTest() throws ObjectNotFoundException, InvalidUserException, ContextAlreadyExistsException {

        // REDUNDANTE Mockito.when(contextRepository.findContextByNameIgnoreCase(context.getName())).thenReturn(Optional.empty());

        // No passo a passo existe o context porém não sei como capturar ele para os testes
        Mockito.when(contextRepository.save(context)).thenReturn(context);

        LoginResponse loginResponse = jwtService.authenticate(userLoginDTO);

        ContextDTO contextDTO = contextService.insert(tokenBearerFormat(loginResponse.getToken()), contextRegisterDTO);
        Context contextResponse = contextDTO.contextDTOToContext();
        creator.setId(1L);
        contextResponse.setCreator(creator);
        ContextDTO contextDTO2 = contextService.insert(tokenBearerFormat(loginResponse.getToken()), contextRegisterDTO2);
        Context contextResponse2 = contextDTO2.contextDTOToContext();
        contextResponse2.setCreator(creator);

        contexts.add(context);
        contexts.add(context2);

        Mockito.lenient().when(contextRepository.findContextsByCreator(creator)).thenReturn(contexts);

        // User não está sendo o mesmo que o creator
        contextListByCreator = contextService.findContextsByCreator(tokenBearerFormat(loginResponse.getToken()));

        // contextListByCreator é uma lista de Context mas essa ultima linha converte para uma lista de ContextDTO
        // pois em uma lista de exibição só é necessário exibir o básico de um contexto
        contexts.stream().map(ContextDTO::new).collect(Collectors.toList());

        //assertEquals(creator, contextListByCreator.get(0).getCreator());
        assertEquals(context.getName(), contextListByCreator.get(0).getName());

        //assertEquals(creator, contextListByCreator.get(1).getCreator());
        assertEquals(context2.getName(), contextListByCreator.get(1).getName());

    }

    @Test
    @DisplayName("Encontrar contextos inexistentes por criador")
    public void findInexistentContextsByCreatorTest() throws InvalidUserException {

        Mockito.lenient().when(userRepository.findByEmail(userLoginDTO.getEmail())).thenReturn(userOptional);

        LoginResponse loginResponse = jwtService.authenticate(userLoginDTO);

        Mockito.lenient().when(userRepository.findByEmailAndPassword(userLoginDTO.getEmail(), userLoginDTO.getPassword()))
                .thenReturn(Optional.empty());

        ObjectNotFoundException throwable = catchThrowableOfType(() ->
                contextService.findContextsByCreator(tokenBearerFormat(loginResponse.getToken())), ObjectNotFoundException.class);

    }

    @Test
    @DisplayName("Encontrar contextos com criador inválido")
    public void findContextsByInvalidCreatorTest() throws InvalidUserException {

        Mockito.lenient().when(userRepository.findByEmailAndPassword(userLoginDTO3.getEmail(), userLoginDTO3.getPassword())).thenReturn(userOptional3);
        Mockito.lenient().when(userRepository.findByEmail(userLoginDTO.getEmail())).thenReturn(Optional.empty());
        Mockito.lenient().when(userRepository.findByEmail(userLoginDTO3.getEmail())).thenReturn(userOptional3);

        LoginResponse loginResponse = jwtService.authenticate(userLoginDTO);
        LoginResponse loginResponse3 = jwtService.authenticate(userLoginDTO3);

        Mockito.lenient().when(userRepository.findByEmailAndPassword(userLoginDTO.getEmail(), userLoginDTO.getPassword()))
                .thenReturn(Optional.empty());

        InvalidUserException throwable = catchThrowableOfType(() ->
                contextService.findContextsByCreator(tokenBearerFormat(loginResponse3.getToken())), InvalidUserException.class);

        ObjectNotFoundException throwable2 = catchThrowableOfType(() ->
                contextService.findContextsByCreator(tokenBearerFormat(loginResponse.getToken())), ObjectNotFoundException.class);

    }

    @Test
    @DisplayName("Inserir um contexto")
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
    @DisplayName("Inserir um contexto já existente")
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
    @DisplayName("Atualizar um contexto")
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
    @DisplayName("Atualizar um contexto inválido")
    public void updateAInvalidContextTest() throws InvalidUserException, ObjectNotFoundException, ContextAlreadyExistsException {

        Mockito.lenient().when(userRepository.findByEmail(userLoginDTO2.getEmail())).thenReturn(userOptional2);
        Mockito.lenient().when(userRepository.findByEmailAndPassword(userLoginDTO2.getEmail(), userLoginDTO2.getPassword())).thenReturn(userOptional2);
        Mockito.lenient().when(contextRepository.findById(1L)).thenReturn(contextOptional);
        Mockito.lenient().when(contextRepository.findById(2L)).thenReturn(Optional.empty());

        LoginResponse loginResponse = jwtService.authenticate(userLoginDTO);
        LoginResponse loginResponse2 = jwtService.authenticate(userLoginDTO2);
        contextService.insert(tokenBearerFormat(loginResponse.getToken()), contextRegisterDTO);

        InvalidUserException throwable = catchThrowableOfType(() ->
                contextService.update(tokenBearerFormat(loginResponse2.getToken()), contextRegisterDTO, 1L), InvalidUserException.class);
        ObjectNotFoundException throwable2 = catchThrowableOfType(() ->
                contextService.update(tokenBearerFormat(loginResponse.getToken()), contextRegisterDTO2, 2L), ObjectNotFoundException.class);

        String messageResponse = throwable.getMessage();
        assertEquals(messageResponse, "User: " + userOptional2.get().getName() + " is not the owner of the context: "
                + contextRegisterDTO.getName() + ".");

        String messageResponse2 = throwable2.getMessage();
        assertNull(messageResponse2);

    }

    @Test
    @DisplayName("Deletar um contexto")
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
    @DisplayName("Atualizar um contexto inválido")
    public void deleteAInvalidContextTest() throws InvalidUserException, ContextAlreadyExistsException, ObjectNotFoundException {

        Mockito.lenient().when(userRepository.findByEmail(userLoginDTO2.getEmail())).thenReturn(userOptional2);
        Mockito.lenient().when(userRepository.findByEmailAndPassword(userLoginDTO2.getEmail(), userLoginDTO2.getPassword())).thenReturn(userOptional2);
        Mockito.lenient().when(contextRepository.findById(1L)).thenReturn(contextOptional);
        Mockito.lenient().when(contextRepository.findById(2L)).thenReturn(Optional.empty());

        LoginResponse loginResponse = jwtService.authenticate(userLoginDTO);
        LoginResponse loginResponse2 = jwtService.authenticate(userLoginDTO2);
        contextService.insert(tokenBearerFormat(loginResponse.getToken()), contextRegisterDTO);

        InvalidUserException throwable = catchThrowableOfType(() ->
                contextService.delete(tokenBearerFormat(loginResponse2.getToken()), 1L), InvalidUserException.class);

        ObjectNotFoundException throwable2 = catchThrowableOfType(() ->
                contextService.delete(tokenBearerFormat(loginResponse.getToken()), 2L), ObjectNotFoundException.class);

        String messageResponse = throwable.getMessage();
        String messageResponse2 = throwable2.getMessage();
        assertEquals(messageResponse, "User: " + userOptional2.get().getName() + " is not the owner of the context: "
                + contextOptional.get().getName() + ".");
        assertNull(messageResponse2);

    }
    
    @Test
    @DisplayName("Encontrar contextos por parâmetros")
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
//    @DisplayName("Encontrar contextos inexistentes por parâmetros")
//    public void findContextsByParamsThatNotExists() {
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
