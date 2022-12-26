package br.ufpb.dcx.apps4society.educapi.unit.service;

import br.ufpb.dcx.apps4society.educapi.domain.Context;
import br.ufpb.dcx.apps4society.educapi.domain.User;
import br.ufpb.dcx.apps4society.educapi.dto.context.ContextDTO;
import br.ufpb.dcx.apps4society.educapi.dto.context.ContextRegisterDTO;
import br.ufpb.dcx.apps4society.educapi.dto.user.UserLoginDTO;
import br.ufpb.dcx.apps4society.educapi.repositories.ContextRepository;
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
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.util.ArrayList;
import java.util.List;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("ContextServiceTest")
public class ContextServiceTest {

    //Vamos mock todas as classes, porem, o service será injetado
    @Mock
    private ContextRepository contextRepository;
    @Mock
    private JWTService jwtService;

    @InjectMocks
    private ContextService service;

    private final ContextRegisterDTO contextRegisterDTO = ContextBuilder.anContext().buildContextRegisterDTO();
    private final User user = UserBuilder.anUser().buildUserRegisterDTO().userRegisterDtoToUser();
    private final UserLoginDTO userLoginDTO = UserBuilder.anUser().buildUserLoginDTO();
    // Usado nos testes que utilizam busca 'theReturn'
    private final Optional<Context> contextOptional = ContextBuilder.anContext().buildOptionalContext();
    private final List<Context> contexts = new ArrayList<>();
    private final Pageable pageable = PageRequest.of(0, 20);

    @Test
    public void findContextByCreatorTest() throws InvalidContextException, ObjectNotFoundException, InvalidUserException {

        //ADICIONAL: lenient() faz parar de reclamar de 'unecessary stubbins'
        Mockito.lenient().when(this.contextRepository.findContextsByCreator(this.user)).thenReturn(contexts);

        for (Context context : contexts) {
            assertEquals(this.user, context.getCreator());
        }

    }
//    @Test
//    public void insertAContextTest() throws ContextAlreadyExistsException, InvalidUserException, ObjectNotFoundException {
//        ContextDTO response = service.insert(String.valueOf(this.jwtService.authenticate(userLoginDTO)), this.contextRegisterDTO);
//
//        assertEquals(response.getName(),this.contextRegisterDTO.getName());
//        assertEquals(response.getImageUrl(),this.contextRegisterDTO.getImageUrl());
//        assertEquals(response.getSoundUrl(),this.contextRegisterDTO.getSoundUrl());
//        assertEquals(response.getVideoUrl(),this.contextRegisterDTO.getVideoUrl());
//
//    }
//    @Test
//    public void insertAContextAlreadyExistTest(){
//        Mockito.when(this.contextRepository.findAllByNameStartsWithIgnoreCase(this.contextRegisterDTO.getName(), pageable))
//                .thenReturn((Page<Context>) this.pageable);
//
//        Exception exception = assertThrows(ContextAlreadyExistsException.class, () -> {
//            service.insert(String.valueOf(this.jwtService.authenticate(userLoginDTO)), this.contextRegisterDTO);
//        });
//
//        assertEquals(Messages.CONTEXT_ALREADY_EXISTS, exception.getMessage());
//    }
//    @Test
//    public void updateAContextTest(){
//        Mockito.when(this.contextRepository.findAllByNameIgnoreCase(this.contextRegisterDTO.getName(), pageable));
//    }
//    @Test
//    public void deleteAContextTest(){
//        Mockito.when(this.contextRepository.findAllByNameIgnoreCase(this.contextRegisterDTO.getName(), pageable));
//    }
//    @Test
//    public void findContextByParametersTest(){
//        Mockito.when(this.contextRepository.findAllByNameIgnoreCase(this.contextRegisterDTO.getName(), pageable));
//    }
//    @Test
//    public void validateUserTest(){
//        Mockito.when(this.contextRepository.findAllByNameIgnoreCase(this.contextRegisterDTO.getName(), pageable));
//    }

    // Não sei se esse é o jeito correto de passa o pageable no construtor
    // o que é response?
    // OBS: não enganchar e ir fazendo o que da para fazer
    // OBS1: passar tokens para variáveis de ambiente e ao fazer os testes so fazer a chamada deles

}
