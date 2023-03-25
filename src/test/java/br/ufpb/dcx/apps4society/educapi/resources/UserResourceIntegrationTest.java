package br.ufpb.dcx.apps4society.educapi.resources;

import br.ufpb.dcx.apps4society.educapi.EducApiApplicationTests;
import br.ufpb.dcx.apps4society.educapi.domain.User;
import br.ufpb.dcx.apps4society.educapi.dto.user.UserLoginDTO;
import br.ufpb.dcx.apps4society.educapi.dto.user.UserRegisterDTO;
import br.ufpb.dcx.apps4society.educapi.repositories.UserRepository;
import br.ufpb.dcx.apps4society.educapi.services.UserService;
import br.ufpb.dcx.apps4society.educapi.unit.domain.builder.UserBuilder;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultHandlers;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

//@ExtendWith(SpringExtension.class)
@RunWith(SpringRunner.class)
//@SpringBootTest
@SpringBootTest(classes = UserResource.class)
//@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
//@AutoConfigureTestEntityManager
@ContextConfiguration(classes = EducApiApplicationTests.class)
@ActiveProfiles("test")
    //Carrega só o resource(controller)
//@WebMvcTest(controllers = UserResource.class)
//@ComponentScan(basePackageClasses={UserResource.class})
class UserResourceIntegrationTest {// extends EducApiApplicationTests {

    //Faz as requisições
    @Autowired
    private MockMvc mockMvc;
//    @Autowired
//    private JWTService jwtService = ServicesBuilder.anService().buildJwtService();
    @Autowired
    private UserResource userResource;

    @MockBean
    private UserRepository userRepository;
    @MockBean
    private UserService userService;


    private static String NAME = "Jose";
    private static String EMAIL = "Jose@educapi.com";
    private static String PASSWORD = "12345678";

    private final UserLoginDTO userLoginDTO = UserBuilder.anUser().buildUserLoginDTO();
    private final UserLoginDTO userLoginEmailEmptyDTO = UserBuilder.anUser().withName("User3").withEmail("").buildUserLoginDTO();

    private final UserRegisterDTO userRegisterDTO = UserBuilder.anUser()
            .withName(NAME)
            .withEmail(EMAIL)
            .withPassword(PASSWORD).buildUserRegisterDTO();

    private User user = userRegisterDTO.userRegisterDtoToUser();

    public PageRequest pageable = PageRequest.of(0, 20, Sort.by("name").ascending());

    @BeforeEach
    public void setUp(){
        mockMvc = MockMvcBuilders.standaloneSetup(userResource).build();
    }

     @Test
     public void insertUserWithCorrectsInputs_ThenReturnStatus201() throws Exception {

         ObjectMapper mapper= new ObjectMapper();
         String json = mapper.writeValueAsString(userRegisterDTO);
         URI uri = new URI("http://localhost:8080/v1/api/users");

         mockMvc.perform(MockMvcRequestBuilders

                         .post(uri)
                         .contentType(MediaType.APPLICATION_JSON)
                         .content(json))
                         .andExpect(MockMvcResultMatchers.status().isCreated())
                         .andExpect(MockMvcResultMatchers.header().string(
                         "locations", Matchers.containsString("http://localhost:8080/v1/api/users")))
                 .andDo(MockMvcResultHandlers.print());

     }

//    @Test
//    public void findByUserEmailTest() throws InvalidUserException {
//
//        Mockito.when(userRepository.findByEmail("user@educapi.com")).thenReturn(userOptional);
//        Mockito.when(userRepository.findByEmailAndPassword("user@educapi.com", "testpassword")).thenReturn(userOptional);
//
//        LoginResponse loginResponse = jwtService.authenticate(userLoginDTO);
//
//        String token = loginResponse.getToken();
//        String email = userLoginDTO.getEmail();
//        String bearedToken = jwtService.tokenBearerFormat(token);
//        User expectedUser = userOptional.get();
//
//        when(jwtService.recoverUser(bearedToken)).thenReturn(Optional.of(email));
//        when(userRepository.findByEmail(email)).thenReturn(Optional.of(expectedUser));
//
//        User actualUser = userService.find(bearedToken);
//
//        assertEquals(expectedUser, actualUser);
//        verify(jwtService.recoverUser(bearedToken));
//        verify(userRepository.findByEmail(email));
//
//    }

//    @Test
//    public void findByInvalidUserEmailTest(){
//
//    }



    // @Test
    // void update() {
    // }

    // @Test
    // void delete() {
    // }

    //https://www.youtube.com/watch?v=Y4_LmPhx1Jc
    //https://www.youtube.com/watch?v=l5WfHfHvqo8

}
//
//package br.ufpb.dcx.apps4society.educapi.resources;
//
//        import br.ufpb.dcx.apps4society.educapi.dto.user.UserRegisterDTO;
//        import io.restassured.http.ContentType;
//        import org.junit.jupiter.api.Test;
//        import static io.restassured.RestAssured.*;
//        import static io.restassured.matcher.RestAssuredMatchers.*;
//        import static org.hamcrest.Matchers.*;
//
//public class UserResourceIntegrationTest{
//
//    @Test
//    public void insertUserTest(){
//
//        baseURI = "http://localhost";
//        port = 8080;
//        basePath = "/v1/api/";
//
//        UserRegisterDTO userRegisterDTO = given()
//                .body("{\n" +
//                        "  \"email\": \"string\",\n" +
//                        "  \"name\": \"string\",\n" +
//                        "  \"password\": \"string\"\n" +
//                        "}")
//                .contentType(ContentType.JSON)
//                .when()
//                .post(baseURI+":"+port+basePath+"users")
//                .then()
//                .extract().path("id","name", "email", "password");
//
//        given()
//                .
//    }
//}