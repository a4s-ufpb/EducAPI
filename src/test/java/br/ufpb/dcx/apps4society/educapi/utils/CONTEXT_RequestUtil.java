package br.ufpb.dcx.apps4society.educapi.utils;

import io.restassured.http.ContentType;
import io.restassured.response.Response;
import org.junit.jupiter.api.BeforeEach;

import static io.restassured.RestAssured.*;

public class CONTEXT_RequestUtil {

    @BeforeEach
    public void setUp(){

        baseURI = "http://localhost";
        port = 8080;
        basePath = "/v1/api/";

    }

    public static Response postContext(String token, String body) throws Exception {

        Response contextDTOResponse = given()
                .body(FileUtils.getJsonFromFile(body))
                .contentType(ContentType.JSON)
                .headers("Authorization",
                        "Bearer " + token,
                        "Content-Type",
                        ContentType.JSON,
                        "Accept",
                        ContentType.JSON).when()
                .post(baseURI+":"+port+basePath+"auth/contexts")
                .then()
                .extract().response();

        return contextDTOResponse;
    }

    public static void deleteContext(String token, String ID){

        given()
                .headers(
                        "Authorization",
                        "Bearer " + token,
                        "Content-Type",
                        ContentType.JSON,
                        "Accept",
                        ContentType.JSON)
                .when()
                .delete(baseURI+":"+port+basePath+"auth/contexts/" + ID);
    }

}
