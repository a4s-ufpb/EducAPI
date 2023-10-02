package br.ufpb.dcx.apps4society.educapi.resources;

import br.ufpb.dcx.apps4society.educapi.utils.FileUtils;
import br.ufpb.dcx.apps4society.educapi.utils.USER_RequestsUtil;
import io.restassured.http.ContentType;
import io.restassured.response.*;
import org.json.JSONObject;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static io.restassured.RestAssured.*;

public class UserResourceIntegrationTest {

    private static String invalidToken = "eyJhbGciOiJIUzUxMiJ9.eyJzdWIiOiJqb3NlMTdAZWR1Y2FwaS5jb20iLCJleHAiOjE2ODA2OTc2MjN9." +
            "qfwlZuirBvosD82v-7lHxb8qhH54_KXR20_0z3guG9rZOW68l5y3gZtvugBtpevmlgK76dsa4hOUPOooRiJ3ng";

    @BeforeEach
    public void setUp(){

        baseURI = "http://localhost";
        port = 8080;
        basePath = "/v1/api/";
    }

    @Test
    public void insertUserByNameEmailPassword_shouldReturn201Test() throws Exception {

        Response userDTOResponse = 
                given()
                                .body(FileUtils.getJsonFromFile("USER_POST_ExpectedRegisterDTOBody.json"))
                        .contentType(ContentType.JSON)
                .when()
                        .post(baseURI+":"+port+basePath+"users")
                .then()
                        .assertThat().statusCode(201)
                        .extract().response();

        JSONObject userDTOAtual = new JSONObject(userDTOResponse.getBody().prettyPrint());
        JSONObject userRegisterDTOEsperado = new JSONObject(FileUtils.getJsonFromFile("USER_POST_ExpectedRegisterDTOBody.json"));

        Assertions.assertNotNull(userDTOAtual.getString("id"));
        Assertions.assertEquals(userRegisterDTOEsperado.getString("name"), userDTOAtual.getString("name"));
        Assertions.assertEquals(userRegisterDTOEsperado.getString("email"), userDTOAtual.getString("email"));
        Assertions.assertEquals(userRegisterDTOEsperado.getString("password"), userDTOAtual.getString("password"));


        String token = given().body(FileUtils.getJsonFromFile("USER_AuthenticateBody.json"))
                .contentType(ContentType.JSON)
                .when()
                .post(baseURI+":"+port+basePath+"auth/login")
                .then().extract().path("token");

        USER_RequestsUtil.delete(token);
    }

    @Test
    public void insertUserByNameEmailPasswordAlreadyExists_shouldReturn201Test() throws Exception {

        USER_RequestsUtil.post("USER_POST_ExpectedRegisterDTOBody.json");

        given()
                .body(FileUtils.getJsonFromFile("USER_POST_ExpectedRegisterDTOBody.json"))
                .contentType(ContentType.JSON)
        .when()
                .post(baseURI+":"+port+basePath+"users")
        .then()
                .assertThat().statusCode(204);


        String token = given().body(FileUtils.getJsonFromFile("USER_AuthenticateBody.json"))
                .contentType(ContentType.JSON)
                .when()
                .post(baseURI+":"+port+basePath+"auth/login")
                .then().extract().path("token");


        USER_RequestsUtil.delete(token);
    }

    @Test
    public void insertUserByInvalidEmail_ShouldReturn400Test() throws Exception {

        given()
                .body(FileUtils.getJsonFromFile("USER_POST_PUT_InvalidEmailBody.json"))
                .contentType(ContentType.JSON)
                .when()
                .post(baseURI+":"+port+basePath+"users")
                .then()
                .assertThat().statusCode(400);
    }

    @Test
    public void insertUserByInvalidPasswordLessThan8Characters_ShouldReturn400Test() throws Exception {

        given()
                .body(FileUtils.getJsonFromFile("USER_POST_PUT_InvalidPasswordLessThan8Body.json"))
                .contentType(ContentType.JSON)
            .when()
                .post(baseURI+":"+port+basePath+"users")
            .then()
                .assertThat().statusCode(400);
    }

    @Test
    public void insertUserByInvalidPasswordMoreThan8Characters_ShouldReturn400Test() throws Exception {

        given()
                .body(FileUtils.getJsonFromFile("USER_POST_PUT_InvalidPasswordMoreThan12Body.json"))
                .contentType(ContentType.JSON)
            .when()
                .post(baseURI+":"+port+basePath+"users")
            .then()
                .assertThat()
                .statusCode(400)
                .extract().response();
    }


    @Test
    public void authenticateUserByEmailPassword_shouldReturn200Test() throws Exception {

        USER_RequestsUtil.post("USER_POST_ExpectedRegisterDTOBody.json");

        String token = given().body(FileUtils.getJsonFromFile("USER_AuthenticateBody.json"))
                .contentType(ContentType.JSON)
        .when()
                .post(baseURI+":"+port+basePath+"auth/login")
        .then()
                .assertThat()
                .statusCode(200)
                .log().all().extract().path("token");

        Assertions.assertNotNull(token);


        USER_RequestsUtil.delete(token);
    }

    @Test
    public void authenticateUserInexistent_ShouldReturn401() throws Exception {

        given().body(FileUtils.getJsonFromFile("USER_AuthenticateBody.json"))
                .contentType(ContentType.JSON)
                .when()
                .post(baseURI+":"+port+basePath+"auth/login")
                .then()
                .assertThat().statusCode(401);
    }

    @Test
    public void authenticateUserByInvalidEmail_shouldReturn401Test() throws Exception {

        USER_RequestsUtil.post("USER_POST_ExpectedRegisterDTOBody.json");

        given().body(FileUtils.getJsonFromFile("USER_AuthenticateInvalidEmailBody.json"))
                .contentType(ContentType.JSON)
                .when()
                .post(baseURI+":"+port+basePath+"auth/login")
                .then()
                .assertThat().statusCode(401);
    }

    @Test
    public void authenticateUserByInvalidPasswordLessThan8Characters_shouldReturn401Test() throws Exception {

        USER_RequestsUtil.post("USER_POST_ExpectedRegisterDTOBody.json");

        given().body(FileUtils.getJsonFromFile("USER_AuthenticatePasswordLessThan8Body.json"))
                .contentType(ContentType.JSON)
                .when()
                .post(baseURI+":"+port+basePath+"auth/login")
                .then()
                .assertThat().statusCode(401);
    }

    @Test
    public void authenticateUserByInvalidPasswordMoreThan12Characters_shouldReturn401Test() throws Exception {

        USER_RequestsUtil.post("USER_POST_ExpectedRegisterDTOBody.json");

        given().body(FileUtils.getJsonFromFile("USER_AuthenticatePasswordMoreThan8Body.json"))
                .contentType(ContentType.JSON)
                .when()
                .post(baseURI+":"+port+basePath+"auth/login")
                .then()
                .assertThat().statusCode(401);
    }


    @Test
    public void findUserByToken_ShouldReturn200Test() throws Exception {

        USER_RequestsUtil.post("USER_POST_ExpectedRegisterDTOBody.json");
        String token = USER_RequestsUtil.authenticate("USER_POST_ExpectedRegisterDTOBody.json");

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

        JSONObject userDTOJSON_Actual = new JSONObject(userDTOResponse.getBody().prettyPrint());
        JSONObject userRegisterDTOJSON_Expected = new JSONObject(FileUtils.getJsonFromFile("USER_POST_ExpectedRegisterDTOBody.json"));

        Assertions.assertNotNull(userDTOJSON_Actual.getString("id"));
        Assertions.assertEquals(userRegisterDTOJSON_Expected.getString("name"), userDTOJSON_Actual.getString("name"));
        Assertions.assertEquals(userRegisterDTOJSON_Expected.getString("email"), userDTOJSON_Actual.getString("email"));


        USER_RequestsUtil.delete(token);
    }

    @Test
    public void findInexistentUserByToken_ShouldReturn500Test() throws Exception {

        USER_RequestsUtil.post("USER_POST_ExpectedRegisterDTOBody.json");
        String token = USER_RequestsUtil.authenticate("USER_POST_ExpectedRegisterDTOBody.json");
        USER_RequestsUtil.delete(token);

        given()
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
                .assertThat().statusCode(500);
    }

    @Test
    public void findUserByMalformedOrInvalidToken_ShouldReturn500Test() throws Exception {

        given()
                .headers(
                        "Authorization",
                        "Bearer " + invalidToken,
                        "Content-Type",
                        ContentType.JSON,
                        "Accept",
                        ContentType.JSON)
                .when()
                .get(baseURI+":"+port+basePath+"auth/users")
                .then()
                .assertThat().statusCode(500);
    }


    @Test
    public void updateUserByEmailAndNameAndPasswordAndToken_ShouldReturn200Test() throws Exception {

        USER_RequestsUtil.post("USER_POST_ExpectedRegisterDTOBody.json");
        String token = USER_RequestsUtil.authenticate("USER_POST_ExpectedRegisterDTOBody.json");

        Response userDTOResponse = given()
                    .headers(
                            "Authorization",
                            "Bearer " + token,
                            "Content-Type",
                            ContentType.JSON,
                            "Accept",
                            ContentType.JSON)
                    .body(FileUtils.getJsonFromFile("USER_PUT_UserBody.json"))
                    .contentType(ContentType.JSON)
                .when()
                    .put(baseURI+":"+port+basePath+"auth/users")
                .then()
                    .assertThat().statusCode(200)
                    .extract().response();

        JSONObject userDTOAtual = new JSONObject(userDTOResponse.getBody().prettyPrint());
        JSONObject userRegisterDTOEsperado = new JSONObject(FileUtils.getJsonFromFile("USER_POST_ExpectedRegisterDTOBody.json"));

        Assertions.assertNotNull(userDTOAtual.getString("id"));
        Assertions.assertNotEquals(userRegisterDTOEsperado.getString("name"), userDTOAtual.getString("name"));
        Assertions.assertNotEquals(userRegisterDTOEsperado.getString("email"), userDTOAtual.getString("email"));
        Assertions.assertNotEquals(userRegisterDTOEsperado.getString("password"), userDTOAtual.getString("password"));


        USER_RequestsUtil.delete(token);
    }

    @Test
    public void updateInexistentUserByEmailAndNameAndPasswordAndToken_ShouldReturn500Test() throws Exception {

        USER_RequestsUtil.post("USER_POST_ExpectedRegisterDTOBody.json");
        String token = USER_RequestsUtil.authenticate("USER_POST_ExpectedRegisterDTOBody.json");
        USER_RequestsUtil.delete(token);

        given()
                .headers(
                        "Authorization",
                        "Bearer " + token,
                        "Content-Type",
                        ContentType.JSON,
                        "Accept",
                        ContentType.JSON)
                .body(FileUtils.getJsonFromFile("USER_PUT_UserBody.json"))
                .contentType(ContentType.JSON)
                .when()
                .put(baseURI+":"+port+basePath+"auth/users")
                .then()
                .assertThat().statusCode(500);
    }

    @Test
    public void updateUserByInvalidEmail_ShouldReturn400Test() throws Exception {

        USER_RequestsUtil.post("USER_POST_ExpectedRegisterDTOBody.json");
        String token = USER_RequestsUtil.authenticate("USER_POST_ExpectedRegisterDTOBody.json");

        given()
                .headers(
                        "Authorization",
                        "Bearer " + token,
                        "Content-Type",
                        ContentType.JSON,
                        "Accept",
                        ContentType.JSON)
                .body(FileUtils.getJsonFromFile("USER_POST_PUT_InvalidEmailBody.json"))
                .contentType(ContentType.JSON)
                .when()
                .put(baseURI+":"+port+basePath+"auth/users")
                .then()
                .assertThat().statusCode(400);


        USER_RequestsUtil.delete(token);
    }

    @Test
    public void updateUserByPasswordLessThan8Characters_ShouldReturn403Test() throws Exception {

        USER_RequestsUtil.post("USER_POST_ExpectedRegisterDTOBody.json");
        String token = USER_RequestsUtil.authenticate("USER_POST_ExpectedRegisterDTOBody.json");

        given()
                .headers(
                        "Authorization",
                        "Bearer " + token,
                        "Content-Type",
                        ContentType.JSON,
                        "Accept",
                        ContentType.JSON)
                .body(FileUtils.getJsonFromFile("USER_POST_PUT_InvalidPasswordLessThan8Body.json"))
                .contentType(ContentType.JSON)
                .when()
                .put(baseURI+":"+port+basePath+"auth/users")
                .then()
                .assertThat().statusCode(400);


        USER_RequestsUtil.delete(token);
    }

    @Test
    public void updateUserByPasswordMoreThan8Characters_ShouldReturn403Test() throws Exception {

        USER_RequestsUtil.post("USER_POST_ExpectedRegisterDTOBody.json");
        String token = USER_RequestsUtil.authenticate("USER_POST_ExpectedRegisterDTOBody.json");

        given()
                .headers(
                        "Authorization",
                        "Bearer " + token,
                        "Content-Type",
                        ContentType.JSON,
                        "Accept",
                        ContentType.JSON)
                .body(FileUtils.getJsonFromFile("USER_POST_PUT_InvalidPasswordMoreThan12Body.json"))
                .contentType(ContentType.JSON)
                .when()
                .put(baseURI+":"+port+basePath+"auth/users")
                .then()
                .assertThat().statusCode(400);


        USER_RequestsUtil.delete(token);
    }

    @Test
    public void updateUserByMalformedOrInvalidToken_ShouldReturn500Test() throws Exception {

        given()
                .headers(
                        "Authorization",
                        "Bearer " + invalidToken,
                        "Content-Type",
                        ContentType.JSON,
                        "Accept",
                        ContentType.JSON)
                .body(FileUtils.getJsonFromFile("USER_POST_ExpectedRegisterDTOBody.json"))
                .contentType(ContentType.JSON)
                .when()
                .put(baseURI+":"+port+basePath+"auth/users")
                .then()
                .assertThat().statusCode(500);
    }


    @Test
    public void deleteUserByToken_ShouldReturn200Test() throws Exception {

        USER_RequestsUtil.post("USER_POST_ExpectedRegisterDTOBody.json");
        String token = USER_RequestsUtil.authenticate("USER_POST_ExpectedRegisterDTOBody.json");

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

        JSONObject userDTOAtual = new JSONObject(userDTOResponse.getBody().prettyPrint());
        JSONObject userRegisterDTOEsperado = new JSONObject(FileUtils.getJsonFromFile("USER_POST_ExpectedRegisterDTOBody.json"));

        Assertions.assertNotNull(userDTOAtual.getString("id"));
        Assertions.assertEquals(userRegisterDTOEsperado.getString("name"), userDTOAtual.getString("name"));
        Assertions.assertEquals(userRegisterDTOEsperado.getString("email"), userDTOAtual.getString("email"));
        Assertions.assertEquals(userRegisterDTOEsperado.getString("password"), userDTOAtual.getString("password"));
    }

    @Test
    public void deleteInexistentUserByToken_ShouldReturn500Test() throws Exception {

        USER_RequestsUtil.post("USER_POST_ExpectedRegisterDTOBody.json");
        String token = USER_RequestsUtil.authenticate("USER_POST_ExpectedRegisterDTOBody.json");
        USER_RequestsUtil.delete(token);

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
                .assertThat().statusCode(500);
    }

    @Test
    public void deleteUserByMalformedOrInvalidToken_ShouldReturn500Test() throws Exception {

        given()
                .headers(
                        "Authorization",
                        "Bearer " + invalidToken,
                        "Content-Type",
                        ContentType.JSON,
                        "Accept",
                        ContentType.JSON)
                .body(FileUtils.getJsonFromFile("USER_POST_ExpectedRegisterDTOBody.json"))
                .contentType(ContentType.JSON)
                .when()
                .put(baseURI+":"+port+basePath+"auth/users")
                .then()
                .assertThat().statusCode(500);
    }
}