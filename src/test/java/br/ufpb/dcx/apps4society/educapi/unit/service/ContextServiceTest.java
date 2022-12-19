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
import br.ufpb.dcx.apps4society.educapi.services.exceptions.InvalidUserException;
import br.ufpb.dcx.apps4society.educapi.unit.domain.builder.ContextBuilder;
import br.ufpb.dcx.apps4society.educapi.unit.domain.builder.UserBuilder;
import br.ufpb.dcx.apps4society.educapi.util.Messages;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

@ExtendWith(MockitoExtension.class)
public class ContextServiceTest {

    @Mock
    private ContextRepository contextRepository;

    @Mock
    private JWTService jwtService;

    @InjectMocks
    private ContextService service;

    private final ContextRegisterDTO contextRegisterDTO = ContextBuilder.anContext().buildContextRegisterDTO();

    private final Optional<Context> contextOptional = ContextBuilder.anContext().buildOptionalContext();
    private final Optional<User> userOptional = UserBuilder.anUser().buildOptionalUser();

    private final UserLoginDTO userLoginDTO = UserBuilder.anUser().buildUserLoginDTO();

    @Test
    public void insertAContext() throws ContextAlreadyExistsException, InvalidUserException {
        //OBS: Construtor precisa de (Token e contextRegisterDTO)
        //OBS2: os tokens são de User, Challenge e context?
        //OBS3: como eu puxo o token pro construtor insert?
        ContextDTO response = service.insert(jwtService.authenticate() , this.contextRegisterDTO);
    }

    @Test
    public void insertAContextAlreadyExistTest(){
        // ** IgnoreCase(Define as strings como iguais quando a diferença entre as letras são somente o fato de serem minusculas ou maiusculas **
        // OBS: como vou introduzir o parâmetro pageable? ja que o construtor é (String nome, Pageable pageable)
        Mockito.when(this.contextRepository.findAllByNameStartsWithIgnoreCase(this.contextRegisterDTO.getName())).thenReturn(this.contextOptional);

        Exception exception = assertThrows(ContextAlreadyExistsException.class, () -> {
            //OBS: Construtor precisa de (Token e contextRegisterDTO)
            //OBS2: os tokens são de User, Challenge e context?
            //OBS3: como eu puxo o token pro construtor insert?
            service.insert(jwtService.authenticate(userLoginDTO), this.contextRegisterDTO);
        });

        assertEquals(Messages.CONTEXT_ALREADY_EXISTS, exception.getMessage());
    }

}
