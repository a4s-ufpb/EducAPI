package br.ufpb.dcx.apps4society.educapi.unit.service;

import br.ufpb.dcx.apps4society.educapi.domain.User;
import br.ufpb.dcx.apps4society.educapi.dto.context.ContextDTO;
import br.ufpb.dcx.apps4society.educapi.dto.context.ContextRegisterDTO;
import br.ufpb.dcx.apps4society.educapi.dto.user.UserDTO;
import br.ufpb.dcx.apps4society.educapi.dto.user.UserLoginDTO;
import br.ufpb.dcx.apps4society.educapi.dto.user.UserRegisterDTO;
import br.ufpb.dcx.apps4society.educapi.repositories.UserRepository;
import br.ufpb.dcx.apps4society.educapi.response.LoginResponse;
import br.ufpb.dcx.apps4society.educapi.services.JWTService;
import br.ufpb.dcx.apps4society.educapi.services.UserService;
import br.ufpb.dcx.apps4society.educapi.services.exceptions.InvalidUserException;
import br.ufpb.dcx.apps4society.educapi.services.exceptions.UserAlreadyExistsException;
import br.ufpb.dcx.apps4society.educapi.unit.domain.builder.ContextBuilder;
import br.ufpb.dcx.apps4society.educapi.unit.domain.builder.ServicesBuilder;
import br.ufpb.dcx.apps4society.educapi.unit.domain.builder.UserBuilder;
import br.ufpb.dcx.apps4society.educapi.util.Messages;
import ch.qos.logback.core.Context;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.data.web.SpringDataWebProperties.Pageable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.data.domain.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.AssertionsForClassTypes.catchThrowableOfType;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

@ExtendWith(MockitoExtension.class)
public class UserServiceTest {

    // @Mock
    // private UserRepository userRepository;

    // @Mock
    // private JWTService jwtService;

    // @InjectMocks
    // private UserService userService;

    @Mock
    UserRepository userRepository;

    @InjectMocks
    JWTService jwtService = ServicesBuilder.anService().withUserRepository(userRepository).buildJwtService();
    @InjectMocks
    UserService userService = ServicesBuilder.anService()
            .withJwtService(jwtService)
            .withUserRepository(userRepository).buildUserService();

    @Value("${app.token.key}")
    private String TOKEN_KEY;

    private final UserLoginDTO userLoginDTO = UserBuilder.anUser().buildUserLoginDTO();
    private final UserLoginDTO userLoginDTO2 = UserBuilder.anUser().withName("User2").buildUserLoginDTO();
    private final UserLoginDTO userLoginDTO3 = UserBuilder.anUser().withName("User3").withEmail("").buildUserLoginDTO();

    private Optional<User> userOptional = UserBuilder.anUser().withId(1L).buildOptionalUser();
    private Optional<User> userOptional2 = UserBuilder.anUser().withId(2L).withName("User2").withEmail("user2@educapi.com").buildOptionalUser();
    private Optional<User> userOptional3 = UserBuilder.anUser().withId(3L).withName("User3").withEmail(userLoginDTO3.getEmail()).buildOptionalUser();    

    private final UserRegisterDTO userRegisterDTO = UserBuilder.anUser().buildUserRegisterDTO();

    @BeforeEach
    public void setUp(){

        Mockito.lenient().when(userRepository.findByEmail("user@educapi.com")).thenReturn(userOptional);
        Mockito.lenient().when(userRepository.findByEmailAndPassword("user@educapi.com", "testpassword")).thenReturn(userOptional);
        
        ReflectionTestUtils.setField(jwtService, "TOKEN_KEY", "it's a token key");
        
    }
    @Test
    public void findUserTest() throws InvalidUserException, UserAlreadyExistsException{

        LoginResponse loginResponse = jwtService.authenticate(userLoginDTO);
        User user = userService.find(jwtService.tokenBearerFormat(loginResponse.getToken()));

        assertNotNull(user.getName());
        assertNotNull(user.getEmail());
        assertEquals("User", user.getName());
        assertEquals("user@educapi.com", user.getEmail());
        
    }

    @Test
    public void findInvalidUserTest() throws InvalidUserException{

    // Mockito.lenient().when(userRepository.findByEmail("")).thenReturn(Optional.empty());
    Mockito.lenient().when(userRepository.findByEmailAndPassword("", "testpassword")).thenReturn(userOptional3);

    //Não gerou o token de usuário com email vazio
    LoginResponse loginResponse = jwtService.authenticate(userLoginDTO3);

        InvalidUserException throwable = catchThrowableOfType(() ->
                userService.find(jwtService.tokenBearerFormat(TOKEN_KEY)), InvalidUserException.class);
    }

    @Test
    public void insertAUserTest() throws UserAlreadyExistsException {

        Mockito.when(userRepository.findByEmail("user@educapi.com")).thenReturn(Optional.empty());

        UserDTO response = userService.insert(this.userRegisterDTO);

        assertEquals(response.getName(), this.userRegisterDTO.getName());
        assertEquals(response.getEmail(), this.userRegisterDTO.getEmail());
        assertEquals(response.getPassword(), this.userRegisterDTO.getPassword());
    }

    @Test
    public void insertAUserAlreadyExistTest() {
        Mockito.when(this.userRepository.findByEmail(this.userRegisterDTO.getEmail())).thenReturn(this.userOptional);

        Exception exception = assertThrows(UserAlreadyExistsException.class, () -> {
            userService.insert(this.userRegisterDTO);
        });

        assertEquals(Messages.USER_ALREADY_EXISTS, exception.getMessage());
    }

    // public void updateUserTest(){

    // }

    // public void deleteUserTest(){
        
    // }

    // public void findAllUsersTest(){
        
    // }

    // public void findPageOfUsersTest(){
    
    // }
        
}
