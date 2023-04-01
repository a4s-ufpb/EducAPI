package br.ufpb.dcx.apps4society.educapi.resources;

import br.ufpb.dcx.apps4society.educapi.domain.User;
import br.ufpb.dcx.apps4society.educapi.dto.user.UserRegisterDTO;
import br.ufpb.dcx.apps4society.educapi.unit.domain.builder.UserBuilder;
import br.ufpb.dcx.apps4society.educapi.utils.FileUtils;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.restassured.http.ContentType;
import io.restassured.path.xml.element.PathElement;
import io.restassured.response.*;
import org.json.JSONObject;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.data.web.JsonPath;
import org.springframework.http.StreamingHttpOutputMessage;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.util.Optional;

import static io.restassured.RestAssured.*;

public class UserResourceIntegrationTest{

    private static String NAME = "jose";
    private static String EMAIL = "jose@educapi.com";
    private static String PASSWORD = "12345678";
    private static String USER_POST_ENDPOINT = baseURI+":"+port+basePath+"users";
    private static String USER_AUTENTICATION_ENDPOINT = baseURI+":"+port+basePath+"auth/login";

    @BeforeEach
    public void setUp(){

        baseURI = "http://localhost";
        port = 8080;
        basePath = "/v1/api/";

    }

    @Test
    public void insertUserByNameEmailPassword_shouldReturn201Test() throws Exception {

        Response userOptionalResponse = given()
                .body(FileUtils.getJsonFromFile("CreateUserBody.json"))
               .contentType(ContentType.JSON)
       .when()
               .post(baseURI+":"+port+basePath+"users")
       .then()
               .assertThat()
               .statusCode(201)
               .log().all()
               .extract().response();

        ObjectMapper mapper = new ObjectMapper();
        JSONObject userOptionalJSONActual = new JSONObject(userOptionalResponse.getBody().prettyPrint());

        String actualId = userOptionalJSONActual.getString("id");
        String actualName = userOptionalJSONActual.getString("name");
        String actualPassword = userOptionalJSONActual.getString("password");
        String actualEmail = userOptionalJSONActual.getString("email");

        Optional<User> userOptional = UserBuilder.anUser()
                .withId(Long.valueOf(actualId))
                .withName(actualName)
                .withEmail(actualEmail)
                .withPassword(actualPassword).buildOptionalUser();

        mapper.writeValue(new File("ActualUser.json"), userOptional.get());
        JSONObject userRegisterDTOJSONExpected = new JSONObject(FileUtils.getJsonFromFile("CreateUserBody.json"));

        Assertions.assertNotNull(userOptionalJSONActual.getString("id"));
        Assertions.assertEquals(userRegisterDTOJSONExpected.getString("name"), userOptionalJSONActual.getString("name"));
        Assertions.assertEquals(userRegisterDTOJSONExpected.getString("email"), userOptionalJSONActual.getString("email"));
        Assertions.assertEquals(userRegisterDTOJSONExpected.getString("password"), userOptionalJSONActual.getString("password"));

    }

    @Test
    public void insertUserByNameEmailPasswordAlreadyExists_shouldReturn201Test() throws Exception {

        given()
                .body(FileUtils.getJsonFromFile("CreateUserBody.json"))
                .contentType(ContentType.JSON)
        .when()
                .post(baseURI+":"+port+basePath+"users");

        given().body(FileUtils.getJsonFromFile("CreateUserBody.json"))
                .contentType(ContentType.JSON)
        .when()
                .post(baseURI+":"+port+basePath+"users")
        .then()
                .assertThat()
                .statusCode(204)
                .log().all();

    }

    @Test
    public void authenticateUserByEmailPassword_shouldReturn201() throws Exception {

        String token = given().body(FileUtils.getJsonFromFile("AuthenticateUserBody.json"))
                .contentType(ContentType.JSON)
        .when()
                .post(baseURI+":"+port+basePath+"auth/login")
        .then()
                .assertThat()
                .statusCode(200)
                .log().all().extract().path("token");

        Assertions.assertNotNull(token);

    }


//package br.ufpb.dcx.apps4society.educapi.resources;
//
//import br.ufpb.dcx.apps4society.educapi.EducApiApplicationTests;
//import br.ufpb.dcx.apps4society.educapi.domain.User;
//import br.ufpb.dcx.apps4society.educapi.dto.user.UserLoginDTO;
//import br.ufpb.dcx.apps4society.educapi.dto.user.UserRegisterDTO;
//import br.ufpb.dcx.apps4society.educapi.repositories.UserRepository;
//import br.ufpb.dcx.apps4society.educapi.services.JWTService;
//import br.ufpb.dcx.apps4society.educapi.services.UserService;
//import br.ufpb.dcx.apps4society.educapi.utils.FileUtils;
//import br.ufpb.dcx.apps4society.educapi.unit.domain.builder.UserBuilder;
//
//import java.net.URI;
//
//import org.junit.jupiter.api.BeforeEach;
//import org.junit.jupiter.api.Test;
//import org.junit.runner.RunWith;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
//import org.springframework.boot.test.context.SpringBootTest;
//import org.springframework.boot.test.mock.mockito.MockBean;
//import org.springframework.http.MediaType;
//import org.springframework.test.context.ActiveProfiles;
//import org.springframework.test.context.ContextConfiguration;
//import org.springframework.test.context.junit4.SpringRunner;
//import org.springframework.test.util.ReflectionTestUtils;
//import org.springframework.test.web.servlet.MockMvc;
//import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
//import org.springframework.test.web.servlet.result.MockMvcResultHandlers;
//import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
//import org.springframework.test.web.servlet.setup.MockMvcBuilders;
//
//@RunWith(SpringRunner.class)
//@SpringBootTest(classes = UserResource.class)
//@AutoConfigureMockMvc(addFilters = false)
//@ContextConfiguration(classes = EducApiApplicationTests.class)
//@ActiveProfiles("test")
//class UserResourceIntegrationTest {
//
//    @Autowired
//    private MockMvc mockMvc;
//    @Autowired
//    private UserResource userResource;
//
//    @Autowired
//    private UserRepository userRepository;
//
//    //Fail to load application context
//
//    @MockBean
//    private JWTService jwtService;
//    @MockBean
//    private UserService userService;
//   /*     = ServicesBuilder.anService()
//            .withJwtService(jwtService)
//            .withUserRepository(userRepository).buildUserService();
//
//*/
//    private static String NAME = "Jose";
//    private static String EMAIL = "jose@educapi.com";
//    private static String PASSWORD = "12345678";
//
//    private static String USER_POST_ENDPOINT = "http://localhost:8080/v1/api/users";
//
//    private final UserLoginDTO userLoginDTO = UserBuilder.anUser().buildUserLoginDTO();
//    private final UserLoginDTO userLoginEmailEmptyDTO = UserBuilder.anUser().withName("User3").withEmail("").buildUserLoginDTO();
//
//    private final UserRegisterDTO userRegisterDTO = UserBuilder.anUser()
//            .withName(NAME)
//            .withEmail(EMAIL)
//            .withPassword(PASSWORD).buildUserRegisterDTO();
//
//    private User user = userRegisterDTO.userRegisterDtoToUser();
//
//    @BeforeEach
//    public void setUp(){
//
//        ReflectionTestUtils.setField(jwtService, "TOKEN_KEY", "it's a token key");
//        mockMvc = MockMvcBuilders.standaloneSetup(userResource).build();
//    }
//
//     @Test
//     public void insertUserWithCorrectsInputs_ThenReturnStatus201() throws Exception {
//
//         URI uri = new URI(USER_POST_ENDPOINT);
//         userRepository.save(user);
//
//         String requestBody = FileUtils.getJsonFromFile("CreateUserBody.json");
//
//         mockMvc.perform(MockMvcRequestBuilders
//                         .post(uri)
//                         .contentType(MediaType.APPLICATION_JSON)
//                         .content(requestBody)
//                 )
//                 .andExpect(MockMvcResultMatchers.status().isCreated())
//                 .andDo(MockMvcResultHandlers.print());
//
//     }
//
//    @Test
//    public void insertUserWithEmptyBody_ThenReturnStatus400() throws Exception {
//
//        URI uri = new URI(USER_POST_ENDPOINT);
//
//        String requestBody = FileUtils.getJsonFromFile("CreateUserBody.json");
//
//        mockMvc.perform(MockMvcRequestBuilders
//                        .post(uri)
//                        .contentType(MediaType.APPLICATION_JSON)
//                        .content("")
//                )
//                .andExpect(MockMvcResultMatchers.status().isBadRequest())
//                .andDo(MockMvcResultHandlers.print());
//
//    }
//
//    public void insertUserAlreadyExists_ThenReturnStatus() throws Exception {
//
//        URI uri = new URI(USER_POST_ENDPOINT);
//
//        String requestBody = FileUtils.getJsonFromFile("CreateUserBody.json");
//
//        mockMvc.perform(MockMvcRequestBuilders
//                        .post(uri)
//                        .contentType(MediaType.APPLICATION_JSON)
//                        .content(requestBody)
//                )
//                .andExpect(MockMvcResultMatchers.status().isCreated())
//                .andDo(MockMvcResultHandlers.print());
//
//    }
//
////     @Test
////     public void findByUserEmailTest() throws InvalidUserException {
////
////     }
////
////     @Test
////     void update() {
////
////     //Should update a user and return 200 ok
////     //Should return 500
////     }
////
////     @Test
////     void delete() {
////     }
//
////    https://www.youtube.com/watch?v=Y4_LmPhx1Jc
////    https://www.youtube.com/watch?v=l5WfHfHvqo8
////    https://www.youtube.com/watch?v=3duamjhP7NM

}