package br.ufpb.dcx.apps4society.educapi.unit.service;

import br.ufpb.dcx.apps4society.educapi.domain.User;
import br.ufpb.dcx.apps4society.educapi.dto.user.UserDTO;
import br.ufpb.dcx.apps4society.educapi.dto.user.UserRegisterDTO;
import br.ufpb.dcx.apps4society.educapi.repositories.UserRepository;
import br.ufpb.dcx.apps4society.educapi.services.JWTService;
import br.ufpb.dcx.apps4society.educapi.services.UserService;
import br.ufpb.dcx.apps4society.educapi.services.exceptions.UserAlreadyExistsException;
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
public class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private JWTService jwtService;

    @InjectMocks
    private UserService service;

    private final UserRegisterDTO userRegisterDTO = UserBuilder.anUser().buildUserRegisterDTO();
    private final Optional<User> userOptional = UserBuilder.anUser().buildOptionalUser();


    @Test
    public void insertAUserTest() throws UserAlreadyExistsException {
        UserDTO response = service.insert(this.userRegisterDTO);

        assertEquals(response.getName(), this.userRegisterDTO.getName());
        assertEquals(response.getEmail(), this.userRegisterDTO.getEmail());
        assertEquals(response.getPassword(), this.userRegisterDTO.getPassword());
    }

    @Test
    public void insertAUserAlreadyExistTest() {
        Mockito.when(this.userRepository.findByEmail(this.userRegisterDTO.getEmail())).thenReturn(this.userOptional);

        Exception exception = assertThrows(UserAlreadyExistsException.class, () -> {
            service.insert(this.userRegisterDTO);
        });

        assertEquals(Messages.USER_ALREADY_EXISTS, exception.getMessage());
    }
}
