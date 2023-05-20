package br.ufpb.dcx.apps4society.educapi.utils;

import io.restassured.http.ContentType;
import io.restassured.response.Response;
import org.junit.jupiter.api.BeforeEach;

import static io.restassured.RestAssured.*;

public class USER_RequestsUtil {

    private static String USER_POST_ENDPOINT = baseURI+":"+port+basePath+"users";
    private static String USER_AUTENTICATION_ENDPOINT = baseURI+":"+port+basePath+"auth/login";

    @BeforeEach
    public void setUp(){

        baseURI = "http://localhost";
        port = 8080;
        basePath = "/v1/api/";

    }

    public static Response post(String body) throws Exception {
        Response userDTOResponse = given().body(FileUtils.getJsonFromFile(body))
                .contentType(ContentType.JSON)
                .when().post(USER_POST_ENDPOINT)
                .then()
                .extract().response();
        return userDTOResponse;
    }

    public static String authenticate(String body) throws Exception {
        String token = given().body(FileUtils.getJsonFromFile(body))
                .contentType(ContentType.JSON)
                .when()
                .post(USER_AUTENTICATION_ENDPOINT)
                .then()
                .log().all()
                .extract().path("token");

        return token;

    }

    public static void delete(String token){
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
