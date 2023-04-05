package br.ufpb.dcx.apps4society.educapi.resources;

import br.ufpb.dcx.apps4society.educapi.dto.user.UserDTO;
import br.ufpb.dcx.apps4society.educapi.unit.domain.builder.UserBuilder;
import br.ufpb.dcx.apps4society.educapi.utils.FileUtils;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.restassured.http.ContentType;
import io.restassured.response.*;
import org.json.JSONObject;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.File;

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

        Response userDTOResponse = given()
                .body(FileUtils.getJsonFromFile("UserRegisterDTODefaultBody.json"))
               .contentType(ContentType.JSON)
       .when()
               .post(baseURI+":"+port+basePath+"users")
       .then()
               .assertThat()
               .statusCode(201)
               .log().all()
               .extract().response();

        JSONObject userDTOJSONActual = new JSONObject(userDTOResponse.getBody().prettyPrint());

        String actualUserId = userDTOJSONActual.getString("id");
        String actualUserName = userDTOJSONActual.getString("name");
        String actualUserPassword = userDTOJSONActual.getString("password");
        String actualUserEmail = userDTOJSONActual.getString("email");

        UserDTO userDTO = UserBuilder.anUser()
                .withId(Long.valueOf(actualUserId))
                .withName(actualUserName)
                .withEmail(actualUserEmail)
                .withPassword(actualUserPassword).buildUserDTO();

        ObjectMapper mapper = new ObjectMapper();
        mapper.writeValue(new File("ActualUser.json"), userDTO);
        JSONObject userRegisterDTOJSONExpected = new JSONObject(FileUtils.getJsonFromFile("UserRegisterDTODefaultBody.json"));

        Assertions.assertNotNull(userDTOJSONActual.getString("id"));
        Assertions.assertEquals(userRegisterDTOJSONExpected.getString("name"), userDTOJSONActual.getString("name"));
        Assertions.assertEquals(userRegisterDTOJSONExpected.getString("email"), userDTOJSONActual.getString("email"));
        Assertions.assertEquals(userRegisterDTOJSONExpected.getString("password"), userDTOJSONActual.getString("password"));

        String token = given().body(FileUtils.getJsonFromFile("AuthenticateUserBody.json"))
                .contentType(ContentType.JSON)
                .when()
                .post(baseURI+":"+port+basePath+"auth/login")
                        .then().extract().path("token");

        given()
                .headers(
                        "Authorization",
                        "Bearer " + token,
                        "Content-Type",
                        ContentType.JSON,
                        "Accept",
                        ContentType.JSON)
                .when()
                .delete(baseURI+":"+port+basePath+"auth/users")
                .then()
                .assertThat().statusCode(200)
                .extract().response();
    }

    @Test
    public void insertUserByNameEmailPasswordAlreadyExists_shouldReturn201Test() throws Exception {

        given()
                .body(FileUtils.getJsonFromFile("UserRegisterDTODefaultBody.json"))
                .contentType(ContentType.JSON)
        .when()
                .post(baseURI+":"+port+basePath+"users");

        given()
                .body(FileUtils.getJsonFromFile("UserRegisterDTODefaultBody.json"))
                .contentType(ContentType.JSON)
        .when()
                .post(baseURI+":"+port+basePath+"users")
        .then()
                .assertThat()
                .statusCode(204)
                .log().all();

        String token = given().body(FileUtils.getJsonFromFile("AuthenticateUserBody.json"))
                .contentType(ContentType.JSON)
                .when()
                .post(baseURI+":"+port+basePath+"auth/login")
                .then().extract().path("token");

        given()
                .headers(
                        "Authorization",
                        "Bearer " + token,
                        "Content-Type",
                        ContentType.JSON,
                        "Accept",
                        ContentType.JSON)
                .when()
                .delete(baseURI+":"+port+basePath+"auth/users")
                .then()
                .assertThat().statusCode(200)
                .extract().response();

    }

    @Test
    public void authenticateUserByEmailPassword_shouldReturn201() throws Exception {

        given()
                .body(FileUtils.getJsonFromFile("UserRegisterDTODefaultBody.json"))
                .contentType(ContentType.JSON)
                .when()
                .post(baseURI+":"+port+basePath+"users");

        String token = given().body(FileUtils.getJsonFromFile("AuthenticateUserBody.json"))
                .contentType(ContentType.JSON)
        .when()
                .post(baseURI+":"+port+basePath+"auth/login")
        .then()
                .assertThat()
                .statusCode(200)
                .log().all().extract().path("token");

        Assertions.assertNotNull(token);

        given()
                .headers(
                        "Authorization",
                        "Bearer " + token,
                        "Content-Type",
                        ContentType.JSON,
                        "Accept",
                        ContentType.JSON)
                .when()
                .delete(baseURI+":"+port+basePath+"auth/users");

    }

    @Test
    public void findUserByToken_ShouldReturn200Test() throws Exception {

        given()
                .body(FileUtils.getJsonFromFile("UserRegisterDTODefaultBody.json"))
                .contentType(ContentType.JSON)
                .when()
                .post(baseURI+":"+port+basePath+"users");

        String token = given()
                .body(FileUtils.getJsonFromFile("UserRegisterDTODefaultBody.json"))
                .contentType(ContentType.JSON)
                .when()
                    .post(baseURI+":"+port+basePath+"auth/login")
                .then()
                    .log().all()
                    .extract().path("token");

        Response userDTOResponse = given()
                    .headers(
                        "Authorization",
                        "Bearer " + token,
                        "Content-Type",
                        ContentType.JSON,
                        "Accept",
                        ContentType.JSON)
                .when()
                    .get(baseURI+":"+port+basePath+"auth/users")
                .then()
                    .assertThat().statusCode(200)
                    .extract().response();

        JSONObject userDTOJSONActual = new JSONObject(userDTOResponse.getBody().prettyPrint());

        String actualUserId = userDTOJSONActual.getString("id");
        String actualUserName = userDTOJSONActual.getString("name");
        String actualUserEmail = userDTOJSONActual.getString("email");

        UserDTO userDTO = UserBuilder.anUser()
                .withId(Long.valueOf(actualUserId))
                .withName(actualUserName)
                .withEmail(actualUserEmail)
                .withPassword(null).buildUserDTO();

        ObjectMapper mapper = new ObjectMapper();
        mapper.writeValue(new File("ActualUser.json"), userDTO);
        JSONObject userRegisterDTOJSONExpected = new JSONObject(FileUtils.getJsonFromFile("UserRegisterDTODefaultBody.json"));

        Assertions.assertNotNull(userDTOJSONActual.getString("id"));
        Assertions.assertEquals(userRegisterDTOJSONExpected.getString("name"), userDTOJSONActual.getString("name"));
        Assertions.assertEquals(userRegisterDTOJSONExpected.getString("email"), userDTOJSONActual.getString("email"));

        given()
                .headers(
                        "Authorization",
                        "Bearer " + token,
                        "Content-Type",
                        ContentType.JSON,
                        "Accept",
                        ContentType.JSON)
                .when()
                .delete(baseURI+":"+port+basePath+"auth/users");

    }

    @Test
    public void updateUserByEmailAndNameAndPasswordAndToken_ShouldReturn200Test() throws Exception {

        given()
                .body(FileUtils.getJsonFromFile("UserRegisterDTODefaultBody.json"))
                .contentType(ContentType.JSON)
                .when()
                .post(baseURI+":"+port+basePath+"users");

        String token = given()
                .body(FileUtils.getJsonFromFile("UserRegisterDTODefaultBody.json"))
                .contentType(ContentType.JSON)
                .when()
                .post(baseURI+":"+port+basePath+"auth/login")
                .then()
                .log().all()
                .extract().path("token");

        Response userDTOResponse = given()
                    .headers(
                            "Authorization",
                            "Bearer " + token,
                            "Content-Type",
                            ContentType.JSON,
                            "Accept",
                            ContentType.JSON)
                    .body(FileUtils.getJsonFromFile("UpdateUserBody.json"))
                    .contentType(ContentType.JSON)
                .when()
                    .put(baseURI+":"+port+basePath+"auth/users")
                .then()
                    .assertThat().statusCode(200)
                    .log().all()
                    .extract()
                    .response();

        JSONObject userDTOJSONActual = new JSONObject(userDTOResponse.getBody().prettyPrint());

        String actualUserId = userDTOJSONActual.getString("id");
        String actualUserName = userDTOJSONActual.getString("name");
        String actualUserPassword = userDTOJSONActual.getString("password");
        String actualUserEmail = userDTOJSONActual.getString("email");

        UserDTO userDTO = UserBuilder.anUser()
                .withId(Long.valueOf(actualUserId))
                .withName(actualUserName)
                .withEmail(actualUserEmail)
                .withPassword(actualUserPassword).buildUserDTO();

        ObjectMapper mapper = new ObjectMapper();
        mapper.writeValue(new File("ActualUser.json"), userDTO);
        JSONObject userRegisterDTOJSONExpected = new JSONObject(FileUtils.getJsonFromFile("UserRegisterDTODefaultBody.json"));

        Assertions.assertNotNull(userDTOJSONActual.getString("id"));
        Assertions.assertNotEquals(userRegisterDTOJSONExpected.getString("name"), userDTOJSONActual.getString("name"));
        Assertions.assertNotEquals(userRegisterDTOJSONExpected.getString("email"), userDTOJSONActual.getString("email"));
        Assertions.assertNotEquals(userRegisterDTOJSONExpected.getString("password"), userDTOJSONActual.getString("password"));

        given()
                .headers(
                        "Authorization",
                        "Bearer " + token,
                        "Content-Type",
                        ContentType.JSON,
                        "Accept",
                        ContentType.JSON)
                .when()
                .delete(baseURI+":"+port+basePath+"auth/users");

    }

    @Test
    public void deleteUserByToken_ShouldReturn200Test() throws Exception {

        given()
                .body(FileUtils.getJsonFromFile("UserRegisterDTODefaultBody.json"))
                .contentType(ContentType.JSON)
                .when()
                .post(baseURI+":"+port+basePath+"users");

        String token = given()
                .body(FileUtils.getJsonFromFile("UserRegisterDTODefaultBody.json"))
                .contentType(ContentType.JSON)
                .when()
                .post(baseURI+":"+port+basePath+"auth/login")
                .then()
                .log().all()
                .extract().path("token");

        Response userDTOResponse = given()
                .headers(
                        "Authorization",
                        "Bearer " + token,
                        "Content-Type",
                        ContentType.JSON,
                        "Accept",
                        ContentType.JSON)
                .when()
                .delete(baseURI+":"+port+basePath+"auth/users")
                .then()
                .assertThat().statusCode(200)
                .extract().response();

        JSONObject userDTOJSONActual = new JSONObject(userDTOResponse.getBody().prettyPrint());

        String actualUserId = userDTOJSONActual.getString("id");
        String actualUserName = userDTOJSONActual.getString("name");
        String actualUserPassword = userDTOJSONActual.getString("password");
        String actualUserEmail = userDTOJSONActual.getString("email");

        UserDTO userDTO = UserBuilder.anUser()
                .withId(Long.valueOf(actualUserId))
                .withName(actualUserName)
                .withEmail(actualUserEmail)
                .withPassword(actualUserPassword).buildUserDTO();

        ObjectMapper mapper = new ObjectMapper();
        mapper.writeValue(new File("ActualUser.json"), userDTO);
        JSONObject userRegisterDTOJSONExpected = new JSONObject(FileUtils.getJsonFromFile("UserRegisterDTODefaultBody.json"));

        Assertions.assertNotNull(userDTOJSONActual.getString("id"));
        Assertions.assertEquals(userRegisterDTOJSONExpected.getString("name"), userDTOJSONActual.getString("name"));
        Assertions.assertEquals(userRegisterDTOJSONExpected.getString("email"), userDTOJSONActual.getString("email"));
        Assertions.assertEquals(userRegisterDTOJSONExpected.getString("password"), userDTOJSONActual.getString("password"));

    }

//    https://www.youtube.com/watch?v=Y4_LmPhx1Jc
//    https://www.youtube.com/watch?v=l5WfHfHvqo8
//    https://www.youtube.com/watch?v=3duamjhP7NM

}