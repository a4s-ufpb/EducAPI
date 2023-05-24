package br.ufpb.dcx.apps4society.educapi.unit.service;

import br.ufpb.dcx.apps4society.educapi.domain.User;
import br.ufpb.dcx.apps4society.educapi.dto.user.UserLoginDTO;
import br.ufpb.dcx.apps4society.educapi.repositories.UserRepository;
import br.ufpb.dcx.apps4society.educapi.response.LoginResponse;
import br.ufpb.dcx.apps4society.educapi.services.JWTService;
import br.ufpb.dcx.apps4society.educapi.services.exceptions.InvalidUserException;
import br.ufpb.dcx.apps4society.educapi.unit.domain.builder.UserBuilder;
import br.ufpb.dcx.apps4society.educapi.util.Messages;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
public class JWTServiceTest {

    private final UserLoginDTO userLoginDTO = UserBuilder.anUser().buildUserLoginDTO();
    private final Optional<User> userOptional = UserBuilder.anUser().buildOptionalUser();
    private final String invalidToken = "Bearer eyJhbGciOiJIUzUxMiJ9.eyJzdWIiOiJtYWlhd2VlZUB0ZXN0LmNvbSIsImV4cCI6MTYxNTM" +
            "5OTkyN30.1qNJIgwjlnm6YcZuIDFLZrQLs58qOwLFkCtXOcaUD-fQZyTa4usOMVgGa19Em_e8WdoXfnaJSv9O-c8IRp-C9Q";
    @Mock
    UserRepository userRepository;
    @InjectMocks
    JWTService service;

    private String tokenFormat(String token) {
        return "Bearer " + token;
    }

    @BeforeEach
    public void setUp() {
        ReflectionTestUtils.setField(service, "TOKEN_KEY", "it's a token key");
    }

    @Test
    public void authenticateTest() throws InvalidUserException {
        Mockito.when(this.userRepository.findByEmailAndPassword(this.userLoginDTO.getEmail(), this.userLoginDTO.getPassword())).thenReturn(this.userOptional);

        LoginResponse response = this.service.authenticate(this.userLoginDTO);

        assertNotNull(response.getToken());
    }

    @Test
    public void authenticateUnregisterUserTest() {
        assertThrows(InvalidUserException.class, () -> {
            this.service.authenticate(this.userLoginDTO);
        });
    }

    @Test
    public void recoverUserTest() throws InvalidUserException {
        Mockito.when(this.userRepository.findByEmailAndPassword(this.userLoginDTO.getEmail(), this.userLoginDTO.getPassword()))
                .thenReturn(this.userOptional);

        LoginResponse response = this.service.authenticate(userLoginDTO);
        String token = tokenFormat(response.getToken());

        String userRecovered = this.service.recoverUser(token).get();

        assertEquals(userRecovered, this.userLoginDTO.getEmail());
    }

    @Test
    public void revocerUserWithNullTokenTest() {
        Exception exception = assertThrows(SecurityException.class, () -> {
            this.service.recoverUser(null);
        });
        assertNotNull(exception);
    }


    @Test
    public void revocerUserWithInvalidTokenTest() {
        Exception exception = assertThrows(SecurityException.class, () -> {
            this.service.recoverUser(invalidToken);
        });
        assertEquals(Messages.TOKEN_INVALID_OR_EXPIRED, exception.getMessage());
    }
}
