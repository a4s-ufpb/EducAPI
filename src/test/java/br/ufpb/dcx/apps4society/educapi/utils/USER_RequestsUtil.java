package br.ufpb.dcx.apps4society.educapi.utils;

import io.restassured.http.ContentType;
import io.restassured.response.Response;
import org.junit.jupiter.api.BeforeEach;

import static io.restassured.RestAssured.*;

public class USER_RequestsUtil {

    @BeforeEach
    public void setUp(){

        baseURI = "http://localhost";
        port = 8080;
        basePath = "/v1/api/";

    }

    public static Response postUser(String body) throws Exception {
        Response userDTOResponse = given().body(FileUtils.getJsonFromFile(body))
                .contentType(ContentType.JSON)
                .when().post(baseURI + ":" + port + basePath + "users")
                .then()
                .extract().response();
        return userDTOResponse;
    }

    public static String authenticateUser(String body) throws Exception {
        String token = given().body(FileUtils.getJsonFromFile(body))
                .contentType(ContentType.JSON)
                .when()
                .post(baseURI+":"+port+basePath+"auth/login")
                .then()
                .log().all()
                .extract().path("token");

        return token;

    }

    public static void deleteUser(String token){
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
