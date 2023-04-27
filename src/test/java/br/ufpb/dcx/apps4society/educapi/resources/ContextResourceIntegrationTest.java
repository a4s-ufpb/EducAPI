package br.ufpb.dcx.apps4society.educapi.resources;

import br.ufpb.dcx.apps4society.educapi.dto.context.ContextDTO;
import br.ufpb.dcx.apps4society.educapi.unit.domain.builder.ContextBuilder;
import br.ufpb.dcx.apps4society.educapi.utils.FileUtils;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.restassured.http.ContentType;
import io.restassured.response.Response;
import org.json.JSONObject;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.File;

import static io.restassured.RestAssured.*;
import static io.restassured.RestAssured.basePath;

public class ContextResourceIntegrationTest {

    private static String NAME = "jose";
    private static String EMAIL = "jose@educapi.com";
    private static String PASSWORD = "12345678";
    private static String USER_POST_ENDPOINT = baseURI+":"+port+basePath+"users";
    private static String USER_AUTENTICATION_ENDPOINT = baseURI+":"+port+basePath+"auth/login";

    private static String CONTEXT_POST_ENDPOINT = baseURI+":"+port+basePath+"contexts";

    private static String invalidToken = "eyJhbGciOiJIUzUxMiJ9.eyJzdWIiOiJqb3NlMTdAZWR1Y2FwaS5jb20iLCJleHAiOjE2ODA2OTc2MjN9.qfwlZuirBvosD82v-7lHxb8qhH54_KXR20_0z3guG9rZOW68l5y3gZtvugBtpevmlgK76dsa4hOUPOooRiJ3ng";

    @BeforeEach
    public void setUp(){

        baseURI = "http://localhost";
        port = 8080;
        basePath = "/v1/api/";

    }

    @Test
    public void insertContextWithTokenNameImageURLSoundURLVideoURL_ShouldReturn201() throws Exception {

        //Create User
        given().body(FileUtils.getJsonFromFile("USER_ExpectedRegisterDTOBody.json"))
                .contentType(ContentType.JSON)
            .when().post(baseURI+":"+port+basePath+"users");

        //Authenticate User
        String token = given().body(FileUtils.getJsonFromFile("USER_ExpectedRegisterDTOBody.json"))
                .contentType(ContentType.JSON)
            .when()
                .post(baseURI+":"+port+basePath+"auth/login")
            .then()
                .log().all()
                .extract().path("token");

        //Create Context
        Response contextDTOResponse = given()
                .body(FileUtils.getJsonFromFile("CONTEXT_ExpectedRegisterDTOBody.json"))
                .contentType(ContentType.JSON)
                .headers("Authorization",
                        "Bearer " + token,
                        "Content-Type",
                        ContentType.JSON,
                        "Accept",
                        ContentType.JSON)
                .when()
                .post(baseURI+":"+port+basePath+"auth/contexts")
                .then()
                .assertThat().statusCode(201)
                .extract().response();

        JSONObject contextDTOJSONActual = new JSONObject(contextDTOResponse.getBody().prettyPrint());

        String actualContextID = contextDTOJSONActual.getString("id");
        String actualContextImageURL = contextDTOJSONActual.getString("imageUrl");
        String actualContextName = contextDTOJSONActual.getString("name");
        String actualContextSoundURL = contextDTOJSONActual.getString("soundUrl");
        String actualContextVideoURL = contextDTOJSONActual.getString("videoUrl");

        ContextDTO contextDTO = ContextBuilder.anContext()
                .withId(Long.valueOf(actualContextID))
                .withImage(actualContextImageURL)
                .withName(actualContextName)
                .withSound(actualContextSoundURL)
                .withVideo(actualContextVideoURL).buildContextDTO();

        ObjectMapper mapper = new ObjectMapper();
        mapper.writeValue(new File("src/test/resources/CONTEXT_ActualContextDTOBody.json"), contextDTO);
        JSONObject contextJSONExpected = new JSONObject(FileUtils.getJsonFromFile("CONTEXT_ExpectedRegisterDTOBody.json"));

        Assertions.assertNotNull(contextDTOJSONActual.getString("id"));
        Assertions.assertEquals(contextJSONExpected.getString("imageUrl"), contextDTOJSONActual.getString("imageUrl"));
        Assertions.assertEquals(contextJSONExpected.getString("name"), contextDTOJSONActual.getString("name"));
        Assertions.assertEquals(contextJSONExpected.getString("soundUrl"), contextDTOJSONActual.getString("soundUrl"));
        Assertions.assertEquals(contextJSONExpected.getString("videoUrl"), contextDTOJSONActual.getString("videoUrl"));

//        //Just to remove context from db
//        given()
//                .headers(
//                        "Authorization",
//                        "Bearer " + token,
//                        "Content-Type",
//                        ContentType.JSON,
//                        "Accept",
//                        ContentType.JSON)
//                .when()
//                .delete(baseURI+":"+port+basePath+"auth/contexts");

        //Just to remove user from DB
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
    public void insertContextWithTokenImageURLSoundURLVideoURLButNameAlreadyExists_ShouldReturn403() throws Exception {

        //Create User
        given().body(FileUtils.getJsonFromFile("USER_ExpectedRegisterDTOBody.json"))
                .contentType(ContentType.JSON)
                .when().post(baseURI+":"+port+basePath+"users");

        //Authenticate User
        String token = given().body(FileUtils.getJsonFromFile("USER_ExpectedRegisterDTOBody.json"))
                .contentType(ContentType.JSON)
                .when()
                .post(baseURI+":"+port+basePath+"auth/login")
                .then()
                .log().all()
                .extract().path("token");

        //Create Context
        given()
                .body(FileUtils.getJsonFromFile("CONTEXT_ExpectedRegisterDTOBody.json"))
                .contentType(ContentType.JSON)
                .headers("Authorization",
                        "Bearer " + token,
                        "Content-Type",
                        ContentType.JSON,
                        "Accept",
                        ContentType.JSON).when()
                .post(baseURI+":"+port+basePath+"auth/contexts");

        given()
                .body(FileUtils.getJsonFromFile("CONTEXT_ExpectedRegisterDTOBody.json"))
                .contentType(ContentType.JSON)
                .headers("Authorization",
                        "Bearer " + token,
                        "Content-Type",
                        ContentType.JSON,
                        "Accept",
                        ContentType.JSON)
                .when()
                .post(baseURI+":"+port+basePath+"auth/contexts")
                .then()
                .assertThat().statusCode(403);

//        //Just to remove context from db
//        given()
//                .headers(
//                        "Authorization",
//                        "Bearer " + token,
//                        "Content-Type",
//                        ContentType.JSON,
//                        "Accept",
//                        ContentType.JSON)
//                .when()
//                .delete(baseURI+":"+port+basePath+"auth/contexts");

        //Just to remove user from DB
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
    public void insertContextWithTokenImageURLSoundURLVideoURLWithoutName_ShouldReturn400() throws Exception {

        //Create User
        given().body(FileUtils.getJsonFromFile("USER_ExpectedRegisterDTOBody.json"))
                .contentType(ContentType.JSON)
                .when().post(baseURI+":"+port+basePath+"users");

        //Authenticate User
        String token = given().body(FileUtils.getJsonFromFile("USER_ExpectedRegisterDTOBody.json"))
                .contentType(ContentType.JSON)
                .when()
                .post(baseURI+":"+port+basePath+"auth/login")
                .then()
                .log().all()
                .extract().path("token");

        given()
                .body(FileUtils.getJsonFromFile("CONTEXT_ExpectedRegisterDTOBody(no name).json"))
                .contentType(ContentType.JSON)
                .headers("Authorization",
                        "Bearer " + token,
                        "Content-Type",
                        ContentType.JSON,
                        "Accept",
                        ContentType.JSON)
                .when()
                .post(baseURI+":"+port+basePath+"auth/contexts")
                .then()
                .assertThat().statusCode(400);

        //Just to remove user from DB
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
    public void insertContextWithNameImageURLSoundURLVideoURLWithOutToken_ShouldReturn400() throws Exception {

        given()
                .body(FileUtils.getJsonFromFile("CONTEXT_ExpectedRegisterDTOBody(no name).json"))
                .contentType(ContentType.JSON)

                .when()
                .post(baseURI+":"+port+basePath+"auth/contexts")
                .then()
                .assertThat().statusCode(400);

    }

    @Test
    public void insertContextWithTokenNameImageURLSoundURLVideoURLButInexistentUser_ShouldReturn404() throws Exception {

        //Create User
        given().body(FileUtils.getJsonFromFile("USER_ExpectedRegisterDTOBody.json"))
                .contentType(ContentType.JSON)
                .when().post(baseURI+":"+port+basePath+"users");

        //Authenticate User
        String token = given().body(FileUtils.getJsonFromFile("USER_ExpectedRegisterDTOBody.json"))
                .contentType(ContentType.JSON)
                .when()
                .post(baseURI+":"+port+basePath+"auth/login")
                .then()
                .log().all()
                .extract().path("token");

        //Just to remove user from DB
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

        given()
                .body(FileUtils.getJsonFromFile("CONTEXT_ExpectedRegisterDTOBody.json"))
                .contentType(ContentType.JSON)
                .headers("Authorization",
                        "Bearer " + token,
                        "Content-Type",
                        ContentType.JSON,
                        "Accept",
                        ContentType.JSON)
                .when()
                .post(baseURI+":"+port+basePath+"auth/contexts")
                .then()
                .assertThat().statusCode(404);
    }

    @Test
    public void insertContextWithMalformedOrExpiredToken_ShouldReturn500() throws Exception {

        //Create Context
        given()
                .body(FileUtils.getJsonFromFile("CONTEXT_ExpectedRegisterDTOBody.json"))
                .contentType(ContentType.JSON)
                .headers("Authorization",
                        "Bearer " + invalidToken,
                        "Content-Type",
                        ContentType.JSON,
                        "Accept",
                        ContentType.JSON)
                .when()
                .post(baseURI+":"+port+basePath+"auth/contexts")
                .then()
                .assertThat().statusCode(500);
    }

    @Test
    public void findContextsByToken_ShouldReturn201() throws Exception {

        //Create User
        given().body(FileUtils.getJsonFromFile("USER_ExpectedRegisterDTOBody.json"))
                .contentType(ContentType.JSON)
                .when().post(baseURI+":"+port+basePath+"users");

        //Authenticate User
        String token = given().body(FileUtils.getJsonFromFile("USER_ExpectedRegisterDTOBody.json"))
                .contentType(ContentType.JSON)
                .when()
                .post(baseURI+":"+port+basePath+"auth/login")
                .then()
                .log().all()
                .extract().path("token");

        //Create Context
        given()
                .body(FileUtils.getJsonFromFile("CONTEXT_ExpectedRegisterDTOBody.json"))
                .contentType(ContentType.JSON)
                .headers("Authorization",
                        "Bearer " + token,
                        "Content-Type",
                        ContentType.JSON,
                        "Accept",
                        ContentType.JSON)
                .when()
                .post(baseURI+":"+port+basePath+"auth/contexts");

        //Get Contexts
        given()
                .headers("Authorization",
                        "Bearer " + token,
                        "Content-Type",
                        ContentType.JSON,
                        "Accept",
                        ContentType.JSON)
                .when()
                .get(baseURI+":"+port+basePath+"auth/contexts")
                .then().assertThat().statusCode(200);

//        //Just to remove context from db
//        given()
//                .headers(
//                        "Authorization",
//                        "Bearer " + token,
//                        "Content-Type",
//                        ContentType.JSON,
//                        "Accept",
//                        ContentType.JSON)
//                .when()
//                .delete(baseURI+":"+port+basePath+"auth/contexts");

        //Just to remove user from DB
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
    public void findContextsByTokenWithInexistentUser_ShouldReturn404() throws Exception {

        //Create User
        given().body(FileUtils.getJsonFromFile("USER_ExpectedRegisterDTOBody.json"))
                .contentType(ContentType.JSON)
                .when().post(baseURI+":"+port+basePath+"users");

        //Authenticate User
        String token = given().body(FileUtils.getJsonFromFile("USER_ExpectedRegisterDTOBody.json"))
                .contentType(ContentType.JSON)
                .when()
                .post(baseURI+":"+port+basePath+"auth/login")
                .then()
                .log().all()
                .extract().path("token");

        //Just to remove user from DB
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

        //Get Contexts
        given()
                .body(FileUtils.getJsonFromFile("CONTEXT_ExpectedRegisterDTOBody.json"))
                .contentType(ContentType.JSON)
                .headers("Authorization",
                        "Bearer " + token,
                        "Content-Type",
                        ContentType.JSON,
                        "Accept",
                        ContentType.JSON)
                .when()
                .get(baseURI+":"+port+basePath+"auth/contexts")
                .then()
                .assertThat().statusCode(404);
    }

    @Test
    public void findContextsByMalformedOrExpiredToken_ShouldReturn500() throws Exception {

        //Get Contexts
        given()
                .body(FileUtils.getJsonFromFile("CONTEXT_ExpectedRegisterDTOBody.json"))
                .contentType(ContentType.JSON)
                .headers("Authorization",
                        "Bearer " + invalidToken,
                        "Content-Type",
                        ContentType.JSON,
                        "Accept",
                        ContentType.JSON)
                .when()
                .get(baseURI+":"+port+basePath+"auth/contexts")
                .then()
                .assertThat().statusCode(500);
    }

    @Test
    public void findContextByID_ShouldReturn200() throws Exception {

        //Create User
        given().body(FileUtils.getJsonFromFile("USER_ExpectedRegisterDTOBody.json"))
                .contentType(ContentType.JSON)
                .when().post(baseURI+":"+port+basePath+"users");

        //Authenticate User
        String token = given().body(FileUtils.getJsonFromFile("USER_ExpectedRegisterDTOBody.json"))
                .contentType(ContentType.JSON)
                .when()
                .post(baseURI+":"+port+basePath+"auth/login")
                .then()
                .log().all()
                .extract().path("token");

        //Create Context
        Response contextDTOResponse = given()
                .body(FileUtils.getJsonFromFile("CONTEXT_ExpectedRegisterDTOBody.json"))
                .contentType(ContentType.JSON)
                .headers("Authorization",
                        "Bearer " + token,
                        "Content-Type",
                        ContentType.JSON,
                        "Accept",
                        ContentType.JSON)
                .when()
                .post(baseURI+":"+port+basePath+"auth/contexts")
                .then()
                .extract().response();

        JSONObject contextDTOJSONActual = new JSONObject(contextDTOResponse.getBody().prettyPrint());

        String actualContextID = contextDTOJSONActual.getString("id");

        given()
                .body(FileUtils.getJsonFromFile("CONTEXT_ExpectedRegisterDTOBody.json"))
                .contentType(ContentType.JSON)
                .headers("Authorization",
                        "Bearer " + token,
                        "Content-Type",
                        ContentType.JSON,
                        "Accept",
                        ContentType.JSON)
                .when()
                .get(baseURI+":"+port+basePath+"contexts/" + actualContextID)
                .then()
                .assertThat().statusCode(200);

//        //Just to remove context from db
//        given()
//                .headers(
//                        "Authorization",
//                        "Bearer " + token,
//                        "Content-Type",
//                        ContentType.JSON,
//                        "Accept",
//                        ContentType.JSON)
//                .when()
//                .delete(baseURI+":"+port+basePath+"auth/contexts");

        //Just to remove user from DB
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
    public void findContextByNullID_ShouldReturn400() throws Exception {

        //Create User
        given().body(FileUtils.getJsonFromFile("USER_ExpectedRegisterDTOBody.json"))
                .contentType(ContentType.JSON)
                .when().post(baseURI+":"+port+basePath+"users");

        //Authenticate User
        String token = given().body(FileUtils.getJsonFromFile("USER_ExpectedRegisterDTOBody.json"))
                .contentType(ContentType.JSON)
                .when()
                .post(baseURI+":"+port+basePath+"auth/login")
                .then()
                .log().all()
                .extract().path("token");

        //Get Context
        given()
                .body(FileUtils.getJsonFromFile("CONTEXT_ExpectedRegisterDTOBody.json"))
                .contentType(ContentType.JSON)
                .headers("Authorization",
                        "Bearer " + token,
                        "Content-Type",
                        ContentType.JSON,
                        "Accept",
                        ContentType.JSON)
                .when()
                .get(baseURI+":"+port+basePath+"contexts/" + null)
                .then()
                .assertThat().statusCode(400);

    }

    @Test
    public void findContextByInexistentID_ShouldReturn404() throws Exception {

        //Create User
        given().body(FileUtils.getJsonFromFile("USER_ExpectedRegisterDTOBody.json"))
                .contentType(ContentType.JSON)
                .when().post(baseURI+":"+port+basePath+"users");

        //Authenticate User
        String token = given().body(FileUtils.getJsonFromFile("USER_ExpectedRegisterDTOBody.json"))
                .contentType(ContentType.JSON)
                .when()
                .post(baseURI+":"+port+basePath+"auth/login")
                .then()
                .log().all()
                .extract().path("token");

        //Create Context
        Response contextDTOResponse = given()
                .body(FileUtils.getJsonFromFile("CONTEXT_ExpectedRegisterDTOBody.json"))
                .contentType(ContentType.JSON)
                .headers("Authorization",
                        "Bearer " + token,
                        "Content-Type",
                        ContentType.JSON,
                        "Accept",
                        ContentType.JSON)
                .when()
                .post(baseURI+":"+port+basePath+"auth/contexts")
                .then()
                .extract().response();

        JSONObject contextDTOJSONActual = new JSONObject(contextDTOResponse.getBody().prettyPrint());

        String actualContextID = contextDTOJSONActual.getString("id");

        //Just to remove context by id from db
        given()
                .headers(
                        "Authorization",
                        "Bearer " + token,
                        "Content-Type",
                        ContentType.JSON,
                        "Accept",
                        ContentType.JSON)
                .when()
                .delete(baseURI+":"+port+basePath+"auth/contexts/" + actualContextID);

        //Get Context
        given()
                .body(FileUtils.getJsonFromFile("CONTEXT_ExpectedRegisterDTOBody.json"))
                .contentType(ContentType.JSON)
                .headers("Authorization",
                        "Bearer " + token,
                        "Content-Type",
                        ContentType.JSON,
                        "Accept",
                        ContentType.JSON)
                .when()
                .get(baseURI+":"+port+basePath+"contexts/" + actualContextID)
                .then()
                .assertThat().statusCode(404);

        //Just to remove user from DB
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
    public void findContextBtQuery_ShouldReturn200(){

    }
}

//ISSUES:
// Tentar criar um contexto com mesmo nome retorna status code 403Forbidden
// Contextos sendo criados mesmo sem urls
// Nomes estão passando com caracteres que não são letras(pode ser um problema principalmente para usuários)
// VALIDAÇÃO DE EMAIL NÃO ESTÁ IMPLEMENTADO EM PESQUISAS POR QUERY
// na documentação do educAPI no swagger | context-resource, put indica que é um possivel response "201 created" porém é um update não era pra ter created... aparentemente é so um erro de texto na propria documentação do swagger.
// Atualizar contextos inserindo o mesmo nome do contexto antigo ou com novo nome que não seja somente letras está passando, ou seja o processamento esta sendo realizado porem está trocando o mesmo objeto pelo menos objeto idêntico, talvez isso devesse ser tratado.