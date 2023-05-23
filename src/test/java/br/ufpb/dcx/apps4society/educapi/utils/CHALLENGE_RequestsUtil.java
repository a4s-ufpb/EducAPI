package br.ufpb.dcx.apps4society.educapi.utils;

import io.restassured.http.ContentType;
import io.restassured.response.Response;
import org.junit.jupiter.api.BeforeEach;

import static io.restassured.RestAssured.*;
import static io.restassured.RestAssured.basePath;

public class CHALLENGE_RequestsUtil {

    @BeforeEach
    public void setUp(){

        baseURI = "http://localhost";
        port = 8080;
        basePath = "/v1/api/";

    }

    public static Response post(String token, String body, String contextID) throws Exception {

        Response challengeDTOResponse = given()
                .body(FileUtils.getJsonFromFile(body))
                .contentType(ContentType.JSON)
                .headers("Authorization",
                        "Bearer " + token,
                        "Content-Type",
                        ContentType.JSON,
                        "Accept",
                        ContentType.JSON)
                .when()
                .post(baseURI+":"+port+basePath+"auth/challenges/" + contextID)
                .then()
                .extract().response();

        return challengeDTOResponse;
    }

    public static void delete(String token, String ID){

        given()
                .headers(
                        "Authorization",
                        "Bearer " + token,
                        "Content-Type",
                        ContentType.JSON,
                        "Accept",
                        ContentType.JSON)
                .when()
                .delete(baseURI+":"+port+basePath+"auth/challenges/" + ID);

    }
}
