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
import br.ufpb.dcx.apps4society.educapi.services.exceptions.ContextAlreadyExistsException;
import br.ufpb.dcx.apps4society.educapi.services.exceptions.InvalidContextException;
import br.ufpb.dcx.apps4society.educapi.services.exceptions.InvalidUserException;
import br.ufpb.dcx.apps4society.educapi.services.exceptions.ObjectNotFoundException;
import br.ufpb.dcx.apps4society.educapi.services.exceptions.UserAlreadyExistsException;
import br.ufpb.dcx.apps4society.educapi.unit.domain.builder.ContextBuilder;
import br.ufpb.dcx.apps4society.educapi.unit.domain.builder.UserBuilder;
import br.ufpb.dcx.apps4society.educapi.util.Messages;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.test.util.ReflectionTestUtils;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.ArrayList;
import java.util.List;

import java.util.Optional;

@ExtendWith(MockitoExtension.class)
@DisplayName("ContextServiceTest")
public class ContextServiceTest {    
    
    @Mock
    UserRepository userRepository;
    @InjectMocks
    JWTService jwtService;
    @InjectMocks
    UserService userService;

    @Mock
    ContextRepository contextRepository;  
    @InjectMocks
    ContextService contextService;
    
    @Value("${app.token.key}")
    private final String TOKEN_KEY = "Bearer eyJhbGciOiJIUzUxMiJ9.eyJzdWIiOiJtYWlhd2VlZUB0ZXN0LmNvbSIsImV4cCI6MTYxNTM" +
    "5OTkyN30.1qNJIgwjlnm6YcZuIDFLZrQLs58qOwLFkCtXOcaUD-fQZyTa4usOMVgGa19Em_e8WdoXfnaJSv9O-c8IRp-C9Q";

    private final ContextRegisterDTO contextRegisterDTO = ContextBuilder.anContext().buildContextRegisterDTO();
    private final Context context = ContextBuilder.anContext().buildContextRegisterDTO().contextRegisterDTOToContext();
    
    private final UserRegisterDTO userRegisterDTO = UserBuilder.anUser().buildUserRegisterDTO();
    private final User creator = UserBuilder.anUser().buildUserRegisterDTO().userRegisterDtoToUser();
    private final UserLoginDTO userLoginDTO = UserBuilder.anUser().buildUserLoginDTO();

    // Usado nos testes que utilizam busca 'theReturn'
    private final Optional<Context> contextOptional = ContextBuilder.anContext().withId((long) 1).buildOptionalContext();  
    private final Optional<User> userOptional = UserBuilder.anUser().buildOptionalUser();

    private final Pageable pageable = PageRequest.of(0, 20);
    private final List<Context> contexts = new ArrayList<>();
    private Page<Context> page = new PageImpl<>(contexts, pageable, pageable.getPageSize());

    @BeforeEach
    public void setUp() {
        ReflectionTestUtils.setField(jwtService, "TOKEN_KEY", "it's a token key");
    }

    @Test
    @DisplayName("Teste de encontrar um contexto pelo autor")
    public void findContextsByCreatorTest() throws InvalidContextException, ObjectNotFoundException, InvalidUserException {
        
        Mockito.when(this.contextRepository.findContextsByCreator(this.creator)).thenReturn(contexts);

        for (Context context : contexts) {
            assertEquals(this.creator, context.getCreator());
        }
    }

    @Test
    @DisplayName("Teste de inserir um contexto")
    public void insertAContextTest() throws ContextAlreadyExistsException, InvalidUserException, ObjectNotFoundException, UserAlreadyExistsException {

        Mockito.when(this.userRepository.findByEmailAndPassword(this.userLoginDTO.getEmail(), this.userLoginDTO.getPassword())).thenReturn(this.userOptional);
        LoginResponse loginResponse = this.jwtService.authenticate(this.userLoginDTO);
        
        userService.insert(userRegisterDTO);
        // Não valida o usuário
        ContextDTO response = this.contextService.insert(loginResponse.getToken(), this.contextRegisterDTO);        

        assertEquals(response.getName(),this.contextRegisterDTO.getName());
        assertEquals(response.getImageUrl(),this.contextRegisterDTO.getImageUrl());
        assertEquals(response.getSoundUrl(),this.contextRegisterDTO.getSoundUrl());
        assertEquals(response.getVideoUrl(),this.contextRegisterDTO.getVideoUrl());
        Assertions.assertThat(loginResponse.getToken()).isNotNull();
    }

    @Test
    @DisplayName("Teste de inserir um contexto já existente")
    public void insertAContextAlreadyExistTest() throws ContextAlreadyExistsException, InvalidUserException {   

        Mockito.when(this.userRepository.findByEmailAndPassword(this.userLoginDTO.getEmail(), this.userLoginDTO.getPassword())).thenReturn(this.userOptional);
        LoginResponse loginResponse = this.jwtService.authenticate(this.userLoginDTO);

        Mockito.when(this.contextRepository.findById(context.getId())).thenReturn(this.contextOptional);
        
        Exception exception = assertThrows(ContextAlreadyExistsException.class, () -> {
            contextService.insert(loginResponse.getToken(), this.contextRegisterDTO);
        });
        
        Mockito.verify(contextRepository.findById(context.getId()));
        assertEquals(Messages.CONTEXT_ALREADY_EXISTS, exception.getMessage());
    }
        
    @Test
    @DisplayName("Teste de atualizar um contexto")
    public void updateAContextTest() throws ObjectNotFoundException, InvalidUserException{

        Mockito.when(this.userRepository.findByEmailAndPassword(this.userLoginDTO.getEmail(), this.userLoginDTO.getPassword())).thenReturn(this.userOptional);
        LoginResponse loginResponse = this.jwtService.authenticate(this.userLoginDTO);
        
        contextService.update(loginResponse.getToken(), contextRegisterDTO, context.getId());
        
        assertNotEquals(this.contextRegisterDTO, null);
    }

    @Test
    @DisplayName("Teste de deletar um contexto")
    public void deleteAContextByIdTest() throws InvalidUserException, ContextAlreadyExistsException, ObjectNotFoundException, UserAlreadyExistsException {
        
        Mockito.when(this.userRepository.findByEmailAndPassword(this.userLoginDTO.getEmail(), this.userLoginDTO.getPassword())).thenReturn(this.userOptional);
        LoginResponse loginResponse = this.jwtService.authenticate(this.userLoginDTO);

        contextService.insert(loginResponse.getToken(), contextRegisterDTO);
        //OBS: insert() é para inserir um CONTEXTO logo porisso que ja tem que haver um USER no repositório        
        Mockito.when(this.contextRepository.findById(this.context.getId())).thenReturn(this.contextOptional);

        contextService.delete(loginResponse.getToken(), this.context.getId());

        Mockito.verify(this.contextRepository.findById(this.context.getId()));
        assertThrows(ObjectNotFoundException.class, () -> {
            contextService.find(this.context.getId());
        });        
    }

    @Test
    @DisplayName("Teste de encontrar um contexto por parâmetros")
    public void findContextsByParametersTest() throws UserAlreadyExistsException, InvalidUserException, ContextAlreadyExistsException, ObjectNotFoundException{
        
        Mockito.when(this.userRepository.findByEmailAndPassword(this.userLoginDTO.getEmail(), this.userLoginDTO.getPassword())).thenReturn(this.userOptional);
        LoginResponse loginResponse = this.jwtService.authenticate(this.userLoginDTO);
        UserRegisterDTO userRegisterDTO = UserBuilder.anUser().buildUserRegisterDTO();

        userService.insert(userRegisterDTO);
        contextService.insert(loginResponse.getToken(), contextRegisterDTO);
        page = contextService.findContextsByParams(this.context.getCreator().getEmail(), this.context.getName(), pageable);
        
        assertEquals(userService.find(loginResponse.getToken()), userRegisterDTO);
        assertEquals(this.contextRepository.findAll(), page);
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
    // No app a procura é feita pelo id e nos testes são feitas por email, author e nome?!
    // OBS: não enganchar e ir fazendo o que da para fazer
    // OBS1: passar tokens para variáveis de ambiente e ao fazer os testes so fazer a chamada deles
    //https://www.youtube.com/watch?v=AKT9FYJBOEo   
}
