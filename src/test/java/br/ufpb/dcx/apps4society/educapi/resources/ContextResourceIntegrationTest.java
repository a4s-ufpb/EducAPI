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

        given().body(FileUtils.getJsonFromFile("USER_ExpectedRegisterDTOBody.json"))
                .contentType(ContentType.JSON)
            .when().post(baseURI+":"+port+basePath+"users");

        String token = given().body(FileUtils.getJsonFromFile("USER_ExpectedRegisterDTOBody.json"))
                .contentType(ContentType.JSON)
            .when()
                .post(baseURI+":"+port+basePath+"auth/login")
            .then()
                .log().all()
                .extract().path("token");

        Response contextDTOResponse = given()
                .body(FileUtils.getJsonFromFile("CONTEXT_ËxpectedRegisterDTOBody.json"))
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
        JSONObject contextJSONExpected = new JSONObject(FileUtils.getJsonFromFile("CONTEXT_ËxpectedRegisterDTOBody.json"));

        Assertions.assertNotNull(contextDTOJSONActual.getString("id"));
        Assertions.assertEquals(contextJSONExpected.getString("imageUrl"), contextDTOJSONActual.getString("imageUrl"));
        Assertions.assertEquals(contextJSONExpected.getString("name"), contextDTOJSONActual.getString("name"));
        Assertions.assertEquals(contextJSONExpected.getString("soundUrl"), contextDTOJSONActual.getString("soundUrl"));
        Assertions.assertEquals(contextJSONExpected.getString("videoUrl"), contextDTOJSONActual.getString("videoUrl"));

        //Just to remove context from db
        given()
                .headers(
                        "Authorization",
                        "Bearer " + token,
                        "Content-Type",
                        ContentType.JSON,
                        "Accept",
                        ContentType.JSON)
                .when()
                .delete(baseURI+":"+port+basePath+"auth/contexts");

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
}
