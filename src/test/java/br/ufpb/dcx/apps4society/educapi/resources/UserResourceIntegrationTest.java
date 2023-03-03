package br.ufpb.dcx.apps4society.educapi.resources;

import br.ufpb.dcx.apps4society.educapi.domain.User;
import br.ufpb.dcx.apps4society.educapi.dto.user.UserLoginDTO;
import br.ufpb.dcx.apps4society.educapi.dto.user.UserRegisterDTO;
import br.ufpb.dcx.apps4society.educapi.repositories.UserRepository;
import br.ufpb.dcx.apps4society.educapi.response.LoginResponse;
import br.ufpb.dcx.apps4society.educapi.services.JWTService;
import br.ufpb.dcx.apps4society.educapi.services.UserService;
import br.ufpb.dcx.apps4society.educapi.services.exceptions.InvalidUserException;
import br.ufpb.dcx.apps4society.educapi.unit.domain.builder.ServicesBuilder;
import br.ufpb.dcx.apps4society.educapi.unit.domain.builder.UserBuilder;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.mockito.Spy;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(SpringExtension.class)
class UserResourceIntegrationTest {    

    @Mock
    public UserRepository userRepository;

    @InjectMocks
    private JWTService jwtService = ServicesBuilder.anService().withUserRepository(userRepository).buildJwtService();
    @InjectMocks
    public UserService userService = ServicesBuilder.anService()
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
    public void setup() {
        
        ReflectionTestUtils.setField(jwtService, "TOKEN_KEY", "it's a token key");
    }

    @Test
    public void findByUserEmailTest() throws InvalidUserException {        

        Mockito.when(userRepository.findByEmail("user@educapi.com")).thenReturn(userOptional);
        Mockito.when(userRepository.findByEmailAndPassword("user@educapi.com", "testpassword")).thenReturn(userOptional);

        LoginResponse loginResponse = jwtService.authenticate(userLoginDTO);

        String token = loginResponse.getToken();
        String email = userLoginDTO.getEmail();
        String bearedToken = jwtService.tokenBearerFormat(token);
        User expectedUser = userOptional.get();

        when(jwtService.recoverUser(bearedToken)).thenReturn(Optional.of(email));
        when(userRepository.findByEmail(email)).thenReturn(Optional.of(expectedUser));
        
        User actualUser = userService.find(bearedToken);

        assertEquals(expectedUser, actualUser);

        //Diz que ele não é um mock
        verify(jwtService.recoverUser(bearedToken));
        verify(userRepository.findByEmail(email));

    }

    public void findByInvalidUserEmailTest(){

    }

    // @Test
    // void insert() {
    // }

    // @Test
    // void update() {
    // }

    // @Test
    // void delete() {
    // }

}