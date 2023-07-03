package br.ufpb.dcx.apps4society.educapi.unit.service;

import br.ufpb.dcx.apps4society.educapi.domain.User;
import br.ufpb.dcx.apps4society.educapi.dto.user.UserDTO;
import br.ufpb.dcx.apps4society.educapi.dto.user.UserLoginDTO;
import br.ufpb.dcx.apps4society.educapi.dto.user.UserRegisterDTO;
import br.ufpb.dcx.apps4society.educapi.repositories.UserRepository;
import br.ufpb.dcx.apps4society.educapi.response.LoginResponse;
import br.ufpb.dcx.apps4society.educapi.services.JWTService;
import br.ufpb.dcx.apps4society.educapi.services.UserService;
import br.ufpb.dcx.apps4society.educapi.services.exceptions.InvalidUserException;
import br.ufpb.dcx.apps4society.educapi.services.exceptions.UserAlreadyExistsException;
import br.ufpb.dcx.apps4society.educapi.utils.builder.ServicesBuilder;
import br.ufpb.dcx.apps4society.educapi.utils.builder.UserBuilder;
import br.ufpb.dcx.apps4society.educapi.util.Messages;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.data.domain.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.AssertionsForClassTypes.catchThrowableOfType;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

@ExtendWith(MockitoExtension.class)
public class UserServiceTest {

    @Mock
    UserRepository userRepository;

    @InjectMocks
    JWTService jwtService = ServicesBuilder.anService().withUserRepository(userRepository).buildJwtService();
    @InjectMocks
    UserService userService = ServicesBuilder.anService()
            .withJwtService(jwtService)
            .withUserRepository(userRepository).buildUserService();

    private final UserLoginDTO userLoginDTO = UserBuilder.anUser().buildUserLoginDTO();    
    private final UserLoginDTO userLoginEmailEmptyDTO = UserBuilder.anUser().withName("User3").withEmail("").buildUserLoginDTO();

    private final Optional<User> userOptional = UserBuilder.anUser().withId(1L).buildOptionalUser();
    private final Optional<User> userEmailEmptyOptional = UserBuilder.anUser().withId(3L).withName("User3").withEmail(userLoginEmailEmptyDTO.getEmail()).buildOptionalUser();    

    private final UserRegisterDTO userRegisterDTO = UserBuilder.anUser().buildUserRegisterDTO();
    private final UserRegisterDTO userRegisterDTO2 = UserBuilder.anUser().withId(2L).withName("User2").buildUserRegisterDTO();

    private List<User> users = new ArrayList<>();

    public PageRequest pageable = PageRequest.of(0, 20, Sort.by("name").ascending());
    

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

    Mockito.lenient().when(userRepository.findByEmailAndPassword("", "testpassword")).thenReturn(userEmailEmptyOptional);
    
    LoginResponse loginResponse = jwtService.authenticate(userLoginEmailEmptyDTO);

    catchThrowableOfType(() ->
            userService.find(jwtService.tokenBearerFormat(loginResponse.getToken())), InvalidUserException.class);

    assertNotNull(loginResponse.getToken());

    }

    @Test
    public void insertAUserTest() throws UserAlreadyExistsException {

        Mockito.when(userRepository.findByEmail(this.userRegisterDTO.getEmail())).thenReturn(Optional.empty());

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

    @Test
    public void updateUserTest() throws InvalidUserException, UserAlreadyExistsException {

        Mockito.when(userRepository.findByEmail(this.userRegisterDTO.getEmail())).thenReturn(Optional.empty());

        LoginResponse loginResponse = jwtService.authenticate(userLoginDTO);
        UserDTO userDTO = userService.insert(this.userRegisterDTO);

        Mockito.when(userRepository.findByEmail(this.userRegisterDTO.getEmail())).thenReturn(userOptional);

        UserDTO userDTO2 = userService.update(jwtService.tokenBearerFormat(loginResponse.getToken()), userRegisterDTO2);

        assertNotNull(userDTO2);
        assertNotEquals(userDTO, userDTO2);

    }

    @Test
    public void deleteUserTest() throws InvalidUserException, UserAlreadyExistsException{

        Mockito.when(userRepository.findByEmail(this.userRegisterDTO.getEmail())).thenReturn(Optional.empty());

        LoginResponse loginResponse = jwtService.authenticate(userLoginDTO);
        UserDTO userDTOResponse = userService.insert(this.userRegisterDTO);

        Mockito.when(userRepository.findByEmail(this.userRegisterDTO.getEmail())).thenReturn(userOptional);

        userService.delete(jwtService.tokenBearerFormat(loginResponse.getToken()));

        catchThrowableOfType(() ->
                userService.find(jwtService.tokenBearerFormat(loginResponse.getToken())), InvalidUserException.class);

        assertNotNull(userDTOResponse);

    }

    @Test
    public void findAllUsersTest(){  

        Mockito.when(userRepository.findAll()).thenReturn(users);

        User user = userRegisterDTO.userRegisterDtoToUser();
        User user2 = userRegisterDTO2.userRegisterDtoToUser();

        ServicesBuilder.insertSimulator(user, users);
        ServicesBuilder.insertSimulator(user2, users);

        List<User> usersList = userService.findAll();

        assertEquals(users, usersList);    
        assertEquals(user, usersList.get(0));
        assertEquals(user2, usersList.get(1));    

    }

    @Test
    public void findPageOfUsersTest(){

        User user = userRegisterDTO.userRegisterDtoToUser();
        User user2 = userRegisterDTO2.userRegisterDtoToUser();

        ServicesBuilder.insertSimulator(user, users);
        ServicesBuilder.insertSimulator(user2, users);

        Mockito.lenient().when(userRepository.findAll(pageable)).thenReturn(new PageImpl<>(users, pageable, pageable.getPageSize()));

        Page<User> pageResponse = userService.findPage(pageable.getPageNumber(), pageable.getPageSize(), "name", "ASC");

        assertEquals(pageResponse, userRepository.findAll(pageable));
        assertEquals(user, pageResponse.getContent().get(0));
        assertEquals(user2, pageResponse.getContent().get(1));

    }
        
}
