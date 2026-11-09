package br.ufpb.dcx.apps4society.educapi.utils;

import org.json.JSONObject;
import org.junit.jupiter.api.BeforeEach;

import static io.restassured.RestAssured.basePath;
import static io.restassured.RestAssured.baseURI;
import static io.restassured.RestAssured.given;
import static io.restassured.RestAssured.port;
import io.restassured.http.ContentType;
import io.restassured.response.Response;

public class CONTEXT_RequestsUtil {

    private static String CONTEXT_POST_ENDPOINT = baseURI + ":" + port + basePath + "auth/contexts";
    private static String CONTEXT_DELETE_ENDPOINT = baseURI + ":" + port + basePath + "auth/contexts/";

    @BeforeEach
    public void setUp() {

        baseURI = "http://localhost";
        port = 8080;
        basePath = "/v1/api/";

    }

    public static Response post(String token, String body) throws Exception {

        JSONObject json = new JSONObject(FileUtils.getJsonFromFile(body));

        Response contextDTOResponse = given()
                .multiPart("name", json.getString("name"))
                .multiPart("imageUrl", json.optString("imageUrl"))
                .multiPart("soundUrl", json.optString("soundUrl"))
                .multiPart("videoUrl", json.optString("videoUrl"))
                .headers(
                        "Authorization",
                        "Bearer " + token,
                        "Accept",
                        ContentType.JSON
                )
                .when()
                .post(CONTEXT_POST_ENDPOINT)
                .then()
                .extract().response();

        return contextDTOResponse;
    }

    public static void delete(String token, String ID) {

        given()
                .headers(
                        "Authorization",
                        "Bearer " + token,
                        "Content-Type",
                        ContentType.JSON,
                        "Accept",
                        ContentType.JSON)
                .when()
                .delete(CONTEXT_DELETE_ENDPOINT + ID);
    }

}
