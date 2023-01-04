package br.ufpb.dcx.apps4society.educapi.unit.contextService;

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
import br.ufpb.dcx.apps4society.educapi.services.exceptions.ContextAlreadyExistsException;
import br.ufpb.dcx.apps4society.educapi.services.exceptions.InvalidContextException;
import br.ufpb.dcx.apps4society.educapi.services.exceptions.InvalidUserException;
import br.ufpb.dcx.apps4society.educapi.services.exceptions.ObjectNotFoundException;
import br.ufpb.dcx.apps4society.educapi.unit.domain.builder.ContextBuilder;
import br.ufpb.dcx.apps4society.educapi.unit.domain.builder.UserBuilder;
import br.ufpb.dcx.apps4society.educapi.util.Messages;

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

import java.util.ArrayList;
import java.util.List;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("ContextServiceTest")
public class ContextServiceTest {
    
    @Mock
    private ContextRepository contextRepository;
    @Mock
    private UserRepository userRepository;
    @Mock
    private JWTService jwtService;        
    // Classe a qual as injeções dos mocks serão aplicadas
    @InjectMocks
    private ContextService contextService;
    @Value("${app.token.key}")
    private String TOKEN_KEY = "Bearer eyJhbGciOiJIUzUxMiJ9.eyJzdWIiOiJtYWlhd2VlZUB0ZXN0LmNvbSIsImV4cCI6MTYxNTM";

    private final ContextRegisterDTO contextRegisterDTO = ContextBuilder.anContext().withId((long) 1).buildContextRegisterDTO();
    private final Context context = ContextBuilder.anContext().buildContextRegisterDTO().contextRegisterDTOToContext();
    
    private final User user = UserBuilder.anUser().buildUserRegisterDTO().userRegisterDtoToUser();
    private final UserLoginDTO userLoginDTO =
            UserBuilder.anUser().withEmail(user.getEmail()).withPassword(user.getPassword()).buildUserLoginDTO();

    // Usado nos testes que utilizam busca 'theReturn'
    private final Optional<User> userOptional = UserBuilder.anUser().buildOptionalUser();
    private final Optional<Context> contextOptional = ContextBuilder.anContext().withId((long) 1).buildOptionalContext();  

    private final Pageable pageable = PageRequest.of(0, 20);
    private final List<Context> contexts = new ArrayList<>();
    private final Page<Context> page = new PageImpl<>(contexts, pageable, pageable.getPageSize());

    //https://www.youtube.com/watch?v=AKT9FYJBOEo

    @Test
    @DisplayName("Teste de encontrar um contexto pelo autor")
    public void findContextsByCreatorTest() throws InvalidContextException, ObjectNotFoundException, InvalidUserException {

        //ADICIONAL: .lenient() faz parar de reclamar de 'unecessary stubbins'
        // .thenReturn() é um como se fosse um retorno fake
        Mockito.lenient().when(this.contextRepository.findContextsByCreator(this.user)).thenReturn(contexts);

        for (Context context : contexts) {
            assertEquals(this.user, context.getCreator());
        }
    }
    @Test
    @DisplayName("Teste de inserir um contexto")
    public void insertAContextTest() throws ContextAlreadyExistsException, InvalidUserException, ObjectNotFoundException {
        
        //Acho que não precisa pq quem vai gerar o token é o insert()
        //userRepository.save(user);

        LoginResponse loginResponse = jwtService.authenticate(this.userLoginDTO);
        // authenticate não está gerando token logo loginResponse ta null!
        // Para inserir ele precisa de um token gerado previamente
        ContextDTO response = this.contextService.insert(loginResponse.getToken(), this.contextRegisterDTO);

        assertEquals(response.getName(),this.contextRegisterDTO.getName());
        assertEquals(response.getImageUrl(),this.contextRegisterDTO.getImageUrl());
        assertEquals(response.getSoundUrl(),this.contextRegisterDTO.getSoundUrl());
        assertEquals(response.getVideoUrl(),this.contextRegisterDTO.getVideoUrl());

    }
    @Test
    @DisplayName("Teste de inserir um contexto já existente")
    public void insertAContextAlreadyExistTest() throws ContextAlreadyExistsException, InvalidUserException {        
        // quando
        Mockito.when(this.contextRepository.findById(context.getId())).thenReturn(this.contextOptional);
        // então
        Exception exception = assertThrows(ContextAlreadyExistsException.class, () -> {
            contextService.insert(String.valueOf(this.jwtService.authenticate(userLoginDTO)), this.contextRegisterDTO);
        });
        // teste
        assertEquals(Messages.CONTEXT_ALREADY_EXISTS, exception.getMessage());
        }
        
    @Test
    @DisplayName("Teste de atualizar um contexto")
    public void updateAContextTest() throws ObjectNotFoundException, InvalidUserException{
        // quando
        //Mockito.when(this.contextRepository.findById(this.context.getId())).thenReturn(contextOptional);
        // então
        contextService.update(TOKEN_KEY, contextRegisterDTO, context.getId());

        //teste
        assertNotEquals(contextRegisterDTO, null);
    }

    @Test
    @DisplayName("Teste de deletar um contexto")
    public void deleteAContextByIdTest() throws InvalidUserException, ContextAlreadyExistsException, ObjectNotFoundException {
        //preparação
        LoginResponse loginResponse = this.jwtService.authenticate(userLoginDTO);
        //quando
        //OBS: insert() é para inserir um CONTEXTO logo porisso que ja tem que haver um USER no repositório
        userRepository.save(user);
        //OBS: JWTService tem uma String TOKEN_KEY inclusa
        Mockito.lenient().when(this.contextRepository.findById(this.context.getId())).thenReturn(this.contextOptional);
        //então
        contextService.delete(loginResponse.getToken(), this.context.getId());
        //teste
        assertThrows(ObjectNotFoundException.class, () -> {
            contextService.find(this.context.getId());
        });
    }
    @Test
    @DisplayName("Teste de encontrar um contexto por parâmetros")
    public void findContextsByParametersTest(){
        //quando
        //Mockito.when(this.contextRepository.findContextsByParams(this.context.getCreator().getEmail(), this.context.getName(), pageable))
        //        .thenReturn(page);
        //então
        Page pageTemp = contextService.findContextsByParams(this.context.getCreator().getEmail(), this.context.getName(), pageable);
        //teste
        assertNotNull(pageTemp);
    }

    // Não sei se esse é o jeito correto de passa o pageable no construtor
    // os temas sao contexto, q tem um conjunto de challenge q sao palavras
    // No app a procura é feita pelo id e nos testes são feitas por email, author e nome?!
    // OBS: não enganchar e ir fazendo o que da para fazer
    // OBS1: passar tokens para variáveis de ambiente e ao fazer os testes so fazer a chamada deles    
}
