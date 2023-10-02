package br.ufpb.dcx.apps4society.educapi.resources;

import br.ufpb.dcx.apps4society.educapi.utils.CHALLENGE_RequestsUtil;
import br.ufpb.dcx.apps4society.educapi.utils.CONTEXT_RequestsUtil;
import br.ufpb.dcx.apps4society.educapi.utils.FileUtils;
import br.ufpb.dcx.apps4society.educapi.utils.USER_RequestsUtil;
import io.restassured.http.ContentType;
import io.restassured.response.Response;
import org.json.JSONObject;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static io.restassured.RestAssured.*;

public class ChallengeResourceIntegrationTest {

    private static String invalidToken = "eyJhbGciOiJIUzUxMiJ9.eyJzdWIiOiJqb3NlMTdAZWR1Y2FwaS5jb20iLCJleHAiOjE2ODA2OTc2MjN9." +
            "qfwlZuirBvosD82v-7lHxb8qhH54_KXR20_0z3guG9rZOW68l5y3gZtvugBtpevmlgK76dsa4hOUPOooRiJ3ng";

    @BeforeEach
    public void setUp(){

        baseURI = "http://localhost";
        port = 8080;
        basePath = "/v1/api/";
    }

    @Test
    public void insertChallengeByCreatorTokenBodyContextID_ShouldReturn201Test() throws Exception {

        USER_RequestsUtil.post("USER_POST_ExpectedRegisterDTOBody.json");
        String token = USER_RequestsUtil.authenticate("USER_POST_ExpectedRegisterDTOBody.json");
        Response contextDTOResponse = CONTEXT_RequestsUtil.post(token, "CONTEXT_POST_ExpectedRegisterDTOBody.json");

        JSONObject contextDTOJSON = new JSONObject(contextDTOResponse.getBody().prettyPrint());
        String contextDTOId = contextDTOJSON.getString("id");

        Response challengeDTOResponseAnterior = given()
                .body(FileUtils.getJsonFromFile("CHALLENGE_POST_ExpectedRegisterDTOBody.json"))
                .contentType(ContentType.JSON)
                .headers("Authorization", "Bearer " + token,
                        "content-type", ContentType.JSON,
                        "Accept", ContentType.JSON)
                .when()
                .post(baseURI+":"+port+basePath+"auth/challenges/" + contextDTOId)
                .then()
                .assertThat().statusCode(201)
                .extract().response();

        JSONObject challengeDTOAtual = new JSONObject(challengeDTOResponseAnterior.getBody().prettyPrint());
        JSONObject challengeRegisterDTOEsperado = new JSONObject(FileUtils.getJsonFromFile("CHALLENGE_POST_ExpectedRegisterDTOBody.json"));

        Assertions.assertNotNull(challengeDTOAtual.getString("id"));
        Assertions.assertEquals(challengeRegisterDTOEsperado.getString("word"), challengeDTOAtual.getString("word"));
        Assertions.assertEquals(challengeRegisterDTOEsperado.getString("imageUrl"), challengeDTOAtual.getString("imageUrl"));
        Assertions.assertEquals(challengeRegisterDTOEsperado.getString("soundUrl"), challengeDTOAtual.getString("soundUrl"));
        Assertions.assertEquals(challengeRegisterDTOEsperado.getString("videoUrl"), challengeDTOAtual.getString("videoUrl"));


        USER_RequestsUtil.delete(token);
        CONTEXT_RequestsUtil.delete(token, contextDTOId);
        CHALLENGE_RequestsUtil.delete(token, challengeDTOAtual.getString("id"));
    }

    @Test
    public void insertChallengeByInvalidWordLessThan2Characters_ShouldReturn400Test() throws Exception {

        USER_RequestsUtil.post("USER_POST_ExpectedRegisterDTOBody.json");
        String token = USER_RequestsUtil.authenticate("USER_AuthenticateBody.json");
        Response contextDTOResponse = CONTEXT_RequestsUtil.post(token, "CONTEXT_POST_ExpectedRegisterDTOBody.json");

        JSONObject contextDTOAtual = new JSONObject(contextDTOResponse.getBody().prettyPrint());
        String contextDTOId = contextDTOAtual.getString("id");

        given().body(FileUtils.getJsonFromFile("CHALLENGE_POST_InvalidWordLessThan2CharactersBody.json"))
                .contentType(ContentType.JSON)
                .headers("Authorization", "Bearer " + token,
                        "content-type", ContentType.JSON,
                        "Accept", ContentType.JSON)
                .when().post(baseURI+":"+port+basePath+"auth/challenges/" + contextDTOId)
                .then().assertThat().statusCode(400);


        USER_RequestsUtil.delete(token);
        CONTEXT_RequestsUtil.delete(token, contextDTOId);
    }

    @Test
    public void insertChallengeByInvalidWordMoreThan72Characters_ShouldReturn400Test() throws Exception {

        USER_RequestsUtil.post("USER_POST_ExpectedRegisterDTOBody.json");
        String token = USER_RequestsUtil.authenticate("USER_AuthenticateBody.json");
        Response contextDTOResponse = CONTEXT_RequestsUtil.post(token, "CONTEXT_POST_ExpectedRegisterDTOBody.json");

        JSONObject contextDTOAtual = new JSONObject(contextDTOResponse.getBody().prettyPrint());
        String contextDTOId = contextDTOAtual.getString("id");


        given().body(FileUtils.getJsonFromFile("CHALLENGE_POST_InvalidWordMoreThan72CharactersBody.json"))
                .contentType(ContentType.JSON)
                .headers("Authorization", "Bearer " + token,
                        "content-type", ContentType.JSON,
                        "Accept", ContentType.JSON)
                .when().post(baseURI+":"+port+basePath+"auth/challenges/" + contextDTOId)
                .then().assertThat().statusCode(400);


        USER_RequestsUtil.delete(token);
        CONTEXT_RequestsUtil.delete(token, contextDTOId);
    }

    @Test
    public void insertChallengeMissingID_ShouldReturn405Test() throws Exception {

        USER_RequestsUtil.post("USER_POST_ExpectedRegisterDTOBody.json");
        String token = USER_RequestsUtil.authenticate("USER_AuthenticateBody.json");
        Response contextDTOResponse = CONTEXT_RequestsUtil.post(token, "CONTEXT_POST_ExpectedRegisterDTOBody.json");

        JSONObject contextDTOAtual = new JSONObject(contextDTOResponse.getBody().prettyPrint());
        String contextDTOId = contextDTOAtual.getString("id");

        given().body(FileUtils.getJsonFromFile("CHALLENGE_POST_ExpectedRegisterDTOBody.json"))
                .contentType(ContentType.JSON)
                .headers("Authorization", "Bearer " + token,
                        "content-type", ContentType.JSON,
                        "Accept", ContentType.JSON)
                .when().post(baseURI+":"+port+basePath+"auth/challenges")
                .then().assertThat().statusCode(405);


        USER_RequestsUtil.delete(token);
        CONTEXT_RequestsUtil.delete(token, contextDTOId);
    }

    @Test
    public void insertChallengeByContextIDLinkedToMissingContext_ShouldReturn404Test() throws Exception {

        USER_RequestsUtil.post("USER_POST_ExpectedRegisterDTOBody.json");
        String token = USER_RequestsUtil.authenticate("USER_POST_ExpectedRegisterDTOBody.json");
        Response contextDTOResponse = CONTEXT_RequestsUtil.post(token, "CONTEXT_POST_ExpectedRegisterDTOBody.json");

        JSONObject contextDTOAtual = new JSONObject(contextDTOResponse.getBody().prettyPrint());
        String contextDTOId = contextDTOAtual.getString("id");

        CONTEXT_RequestsUtil.delete(token, contextDTOId);

        given()
                .body(FileUtils.getJsonFromFile("CHALLENGE_POST_ExpectedRegisterDTOBody.json"))
                .contentType(ContentType.JSON)
                .headers("Authorization", "Bearer " + token,
                        "content-type", ContentType.JSON,
                        "Accept", ContentType.JSON)
                .when()
                .post(baseURI+":"+port+basePath+"auth/challenges/" + contextDTOId)
                .then()
                .assertThat().statusCode(404);


        USER_RequestsUtil.delete(token);
    }

    @Test
    public void insertChallengeByNonNumericID_ShouldReturn400Test() throws Exception {

        USER_RequestsUtil.post("USER_POST_ExpectedRegisterDTOBody.json");
        String token = USER_RequestsUtil.authenticate("USER_POST_ExpectedRegisterDTOBody.json");
        Response contextDTOResponse = CONTEXT_RequestsUtil.post(token, "CONTEXT_POST_ExpectedRegisterDTOBody.json");

        JSONObject contextDTOAtual = new JSONObject(contextDTOResponse.getBody().prettyPrint());
        String contextDTOId = contextDTOAtual.getString("id");

        given()
                .body(FileUtils.getJsonFromFile("CHALLENGE_POST_ExpectedRegisterDTOBody.json"))
                .contentType(ContentType.JSON)
                .headers("Authorization", "Bearer " + token,
                        "content-type", ContentType.JSON,
                        "Accept", ContentType.JSON)
                .when()
                .post(baseURI+":"+port+basePath+"auth/challenges/" + "abc")
                .then()
                .assertThat().statusCode(400);


        USER_RequestsUtil.delete(token);
        CONTEXT_RequestsUtil.delete(token, contextDTOId);
    }

    @Test
    public void insertChallengeMissingToken_ShouldReturn400Test() throws Exception {

        USER_RequestsUtil.post("USER_POST_ExpectedRegisterDTOBody.json");
        String token = USER_RequestsUtil.authenticate("USER_POST_ExpectedRegisterDTOBody.json");
        Response contextDTOResponse = CONTEXT_RequestsUtil.post(token, "CONTEXT_POST_ExpectedRegisterDTOBody.json");

        JSONObject contextDTOAtual = new JSONObject(contextDTOResponse.getBody().prettyPrint());
        String contextDTOId = contextDTOAtual.getString("id");

        given()
                .body(FileUtils.getJsonFromFile("CHALLENGE_POST_ExpectedRegisterDTOBody.json"))
                .contentType(ContentType.JSON)
                .when()
                .post(baseURI+":"+port+basePath+"auth/challenges/" + contextDTOId)
                .then()
                .assertThat().statusCode(400);


        USER_RequestsUtil.delete(token);
        CONTEXT_RequestsUtil.delete(token, contextDTOId);
    }

    @Test
    public void insertChallengeByCreatorTokenLinkedToMissingCreator_ShouldReturn400Test() throws Exception {

        USER_RequestsUtil.post("USER_POST_ExpectedRegisterDTOBody.json");
        String token = USER_RequestsUtil.authenticate("USER_POST_ExpectedRegisterDTOBody.json");
        Response contextDTOResponse = CONTEXT_RequestsUtil.post(token, "CONTEXT_POST_ExpectedRegisterDTOBody.json");

        JSONObject contextDTOAtual = new JSONObject(contextDTOResponse.getBody().prettyPrint());
        String contextDTOId = contextDTOAtual.getString("id");

        USER_RequestsUtil.delete(token);

        given()
                .body(FileUtils.getJsonFromFile("CHALLENGE_POST_ExpectedRegisterDTOBody.json"))
                .contentType(ContentType.JSON)
                .headers("Authorization", "Bearer " + token,
                        "content-type", ContentType.JSON,
                        "Accept", ContentType.JSON)
                .when()
                .post(baseURI+":"+port+basePath+"auth/challenges/" + contextDTOId)
                .then()
                .assertThat().statusCode(404);


        CONTEXT_RequestsUtil.delete(token, contextDTOId);
    }

    @Test
    public void insertChallengeByMalformedOrExpiredToken_ShouldReturn500Test() throws Exception {

        USER_RequestsUtil.post("USER_POST_ExpectedRegisterDTOBody.json");
        String token = USER_RequestsUtil.authenticate("USER_POST_ExpectedRegisterDTOBody.json");
        Response contextDTOResponse = CONTEXT_RequestsUtil.post(token, "CONTEXT_POST_ExpectedRegisterDTOBody.json");

        JSONObject contextDTOAtual = new JSONObject(contextDTOResponse.getBody().prettyPrint());
        String contextDTOId = contextDTOAtual.getString("id");

        given()
                .body(FileUtils.getJsonFromFile("CHALLENGE_POST_ExpectedRegisterDTOBody.json"))
                .contentType(ContentType.JSON)
                .headers("Authorization", "Bearer " + invalidToken,
                        "content-type", ContentType.JSON,
                        "Accept", ContentType.JSON)
                .when()
                .post(baseURI+":"+port+basePath+"auth/challenges/" + contextDTOId)
                .then()
                .assertThat().statusCode(500);


        USER_RequestsUtil.delete(token);
        CONTEXT_RequestsUtil.delete(token, contextDTOId);
    }


    @Test
    public void findChallengesByCreatorToken_ShouldReturn200Test() throws Exception {

        USER_RequestsUtil.post("USER_POST_ExpectedRegisterDTOBody.json");
        String token = USER_RequestsUtil.authenticate("USER_POST_ExpectedRegisterDTOBody.json");
        Response contextDTOResponse = CONTEXT_RequestsUtil.post(token, "CONTEXT_POST_ExpectedRegisterDTOBody.json");

        JSONObject contextDTOAtual = new JSONObject(contextDTOResponse.getBody().prettyPrint());
        String contextDTOId = contextDTOAtual.getString("id");

        Response challengeDTOResponseAnterior = CHALLENGE_RequestsUtil.post(
                token, "CHALLENGE_POST_ExpectedRegisterDTOBody.json", contextDTOId);

        given()
                .body(FileUtils.getJsonFromFile("CHALLENGE_POST_ExpectedRegisterDTOBody.json"))
                .contentType(ContentType.JSON)
                .headers("Authorization", "Bearer " + token,
                        "content-type", ContentType.JSON,
                        "Accept", ContentType.JSON)
                .when()
                .get(baseURI+":"+port+basePath+"auth/challenges/")
                .then()
                .assertThat().statusCode(200);

        JSONObject challengeDTOAtual = new JSONObject(challengeDTOResponseAnterior.getBody().prettyPrint());
        String challengeIDAtual = challengeDTOAtual.getString("id");


        USER_RequestsUtil.delete(token);
        CONTEXT_RequestsUtil.delete(token, contextDTOId);
        CHALLENGE_RequestsUtil.delete(token, challengeIDAtual);
    }

    @Test
    public void findChallengesByMissingToken_ShouldReturn400Test() throws Exception {

        USER_RequestsUtil.post("USER_POST_ExpectedRegisterDTOBody.json");
        String token = USER_RequestsUtil.authenticate("USER_POST_ExpectedRegisterDTOBody.json");
        Response contextDTOResponse = CONTEXT_RequestsUtil.post(token, "CONTEXT_POST_ExpectedRegisterDTOBody.json");

        JSONObject contextDTOAtual = new JSONObject(contextDTOResponse.getBody().prettyPrint());
        String contextDTOId = contextDTOAtual.getString("id");

        Response challengeDTOResponseAnterior = CHALLENGE_RequestsUtil.post(
                token, "CHALLENGE_POST_ExpectedRegisterDTOBody.json", contextDTOId);

        given()
                .body(FileUtils.getJsonFromFile("CHALLENGE_POST_ExpectedRegisterDTOBody.json"))
                .contentType(ContentType.JSON)
                .when()
                .get(baseURI+":"+port+basePath+"auth/challenges/")
                .then()
                .assertThat().statusCode(400);

        JSONObject challengeDTOAtual = new JSONObject(challengeDTOResponseAnterior.getBody().prettyPrint());
        String challengeIDAtual = challengeDTOAtual.getString("id");


        USER_RequestsUtil.delete(token);
        CONTEXT_RequestsUtil.delete(token, contextDTOId);
        CHALLENGE_RequestsUtil.delete(token, challengeIDAtual);
    }

    @Test
    public void findChallengesByCreatorTokenLinkedToMissingCreator_ShouldReturn404Test() throws Exception {

        USER_RequestsUtil.post("USER_POST_ExpectedRegisterDTOBody.json");
        String token = USER_RequestsUtil.authenticate("USER_POST_ExpectedRegisterDTOBody.json");
        Response contextDTOResponse = CONTEXT_RequestsUtil.post(token, "CONTEXT_POST_ExpectedRegisterDTOBody.json");

        JSONObject contextDTOAtual = new JSONObject(contextDTOResponse.getBody().prettyPrint());
        String contextDTOId = contextDTOAtual.getString("id");

        Response challengeDTOResponseAnterior = CHALLENGE_RequestsUtil.post(
                token, "CHALLENGE_POST_ExpectedRegisterDTOBody.json", contextDTOId);

        USER_RequestsUtil.delete(token);

        given()
                .body(FileUtils.getJsonFromFile("CHALLENGE_POST_ExpectedRegisterDTOBody.json"))
                .contentType(ContentType.JSON)
                .headers("Authorization", "Bearer " + token,
                        "content-type", ContentType.JSON,
                        "Accept", ContentType.JSON)
                .when()
                .get(baseURI+":"+port+basePath+"auth/challenges/")
                .then()
                .assertThat().statusCode(404);

        JSONObject challengeDTOAtual = new JSONObject(challengeDTOResponseAnterior.getBody().prettyPrint());
        String challengeIDAtual = challengeDTOAtual.getString("id");


        CONTEXT_RequestsUtil.delete(token, contextDTOId);
        CHALLENGE_RequestsUtil.delete(token, challengeIDAtual);
    }

    @Test
    public void findChallengesByMalformedOrExpiredToken_ShouldReturn400Test() throws Exception {

        USER_RequestsUtil.post("USER_POST_ExpectedRegisterDTOBody.json");
        String token = USER_RequestsUtil.authenticate("USER_POST_ExpectedRegisterDTOBody.json");
        Response contextDTOResponse = CONTEXT_RequestsUtil.post(token, "CONTEXT_POST_ExpectedRegisterDTOBody.json");

        JSONObject contextDTOAtual = new JSONObject(contextDTOResponse.getBody().prettyPrint());
        String contextDTOId = contextDTOAtual.getString("id");

        Response challengeDTOResponseAnterior = CHALLENGE_RequestsUtil.post(
                token, "CHALLENGE_POST_ExpectedRegisterDTOBody.json", contextDTOId);

        given()
                .body(FileUtils.getJsonFromFile("CHALLENGE_POST_ExpectedRegisterDTOBody.json"))
                .contentType(ContentType.JSON)
                .headers("Authorization", "Bearer " + invalidToken,
                        "content-type", ContentType.JSON,
                        "Accept", ContentType.JSON)
                .when()
                .get(baseURI+":"+port+basePath+"auth/challenges/")
                .then()
                .assertThat().statusCode(500);

        JSONObject challengeDTOAtual = new JSONObject(challengeDTOResponseAnterior.getBody().prettyPrint());
        String challengeIDAtual = challengeDTOAtual.getString("id");


        USER_RequestsUtil.delete(token);
        CONTEXT_RequestsUtil.delete(token, contextDTOId);
        CHALLENGE_RequestsUtil.delete(token, challengeIDAtual);
    }


    @Test
    public void findChallengeByCreatorTokenID_ShouldReturn200Test() throws Exception {

        USER_RequestsUtil.post("USER_POST_ExpectedRegisterDTOBody.json");
        String token = USER_RequestsUtil.authenticate("USER_POST_ExpectedRegisterDTOBody.json");
        Response contextDTOResponse = CONTEXT_RequestsUtil.post(token, "CONTEXT_POST_ExpectedRegisterDTOBody.json");

        JSONObject contextDTOAtual = new JSONObject(contextDTOResponse.getBody().prettyPrint());
        String contextDTOId = contextDTOAtual.getString("id");

        Response challengeDTOResponseAnterior = CHALLENGE_RequestsUtil.post(
                token, "CHALLENGE_POST_ExpectedRegisterDTOBody.json", contextDTOId);

        JSONObject challengeDTOAtual = new JSONObject(challengeDTOResponseAnterior.getBody().prettyPrint());
        String challengeIDAtual = challengeDTOAtual.getString("id");

        given()
                .body(FileUtils.getJsonFromFile("CHALLENGE_POST_ExpectedRegisterDTOBody.json"))
                .contentType(ContentType.JSON)
                .headers("Authorization", "Bearer " + token,
                        "content-type", ContentType.JSON,
                        "Accept", ContentType.JSON)
                .when()
                .get(baseURI+":"+port+basePath+"auth/challenges/" + challengeIDAtual)
                .then()
                .assertThat().statusCode(200);


        USER_RequestsUtil.delete(token);
        CONTEXT_RequestsUtil.delete(token, contextDTOId);
        CHALLENGE_RequestsUtil.delete(token, challengeIDAtual);
    }

    @Test
    public void findChallengeMissingID_ShouldReturn200Test() throws Exception {

        USER_RequestsUtil.post("USER_POST_ExpectedRegisterDTOBody.json");
        String token = USER_RequestsUtil.authenticate("USER_POST_ExpectedRegisterDTOBody.json");
        Response contextDTOResponse = CONTEXT_RequestsUtil.post(token, "CONTEXT_POST_ExpectedRegisterDTOBody.json");

        JSONObject contextDTOAtual = new JSONObject(contextDTOResponse.getBody().prettyPrint());
        String contextDTOId = contextDTOAtual.getString("id");

        Response challengeDTOResponseAnterior = CHALLENGE_RequestsUtil.post(
                token, "CHALLENGE_POST_ExpectedRegisterDTOBody.json", contextDTOId);

        given()
                .body(FileUtils.getJsonFromFile("CHALLENGE_POST_ExpectedRegisterDTOBody.json"))
                .contentType(ContentType.JSON)
                .headers("Authorization", "Bearer " + token,
                        "content-type", ContentType.JSON,
                        "Accept", ContentType.JSON)
                .when()
                .get(baseURI+":"+port+basePath+"auth/challenges/")
                .then()
                .assertThat().statusCode(200);

        JSONObject challengeDTOAtual = new JSONObject(challengeDTOResponseAnterior.getBody().prettyPrint());
        String challengeIDAtual = challengeDTOAtual.getString("id");


        USER_RequestsUtil.delete(token);
        CONTEXT_RequestsUtil.delete(token, contextDTOId);
        CHALLENGE_RequestsUtil.delete(token, challengeIDAtual);
    }

    @Test
    public void findChallengeByNonNumericID_ShouldReturn400Test() throws Exception {

        USER_RequestsUtil.post("USER_POST_ExpectedRegisterDTOBody.json");
        String token = USER_RequestsUtil.authenticate("USER_POST_ExpectedRegisterDTOBody.json");
        Response contextDTOResponse = CONTEXT_RequestsUtil.post(token, "CONTEXT_POST_ExpectedRegisterDTOBody.json");

        JSONObject contextDTOAtual = new JSONObject(contextDTOResponse.getBody().prettyPrint());
        String contextDTOId = contextDTOAtual.getString("id");

        Response challengeDTOResponseAnterior = CHALLENGE_RequestsUtil.post(
                token, "CHALLENGE_POST_ExpectedRegisterDTOBody.json", contextDTOId);

        given()
                .body(FileUtils.getJsonFromFile("CHALLENGE_POST_ExpectedRegisterDTOBody.json"))
                .contentType(ContentType.JSON)
                .headers("Authorization", "Bearer " + token,
                        "content-type", ContentType.JSON,
                        "Accept", ContentType.JSON)
                .when()
                .get(baseURI+":"+port+basePath+"auth/challenges/" + "abc")
                .then()
                .assertThat().statusCode(400);

        JSONObject challengeDTOAtual = new JSONObject(challengeDTOResponseAnterior.getBody().prettyPrint());
        String challengeIDAtual = challengeDTOAtual.getString("id");


        USER_RequestsUtil.delete(token);
        CONTEXT_RequestsUtil.delete(token, contextDTOId);
        CHALLENGE_RequestsUtil.delete(token, challengeIDAtual);
    }

    @Test
    public void findChallengeByIDLinkedToMissingChallenge_ShouldReturn404Test() throws Exception {

        USER_RequestsUtil.post("USER_POST_ExpectedRegisterDTOBody.json");
        String token = USER_RequestsUtil.authenticate("USER_POST_ExpectedRegisterDTOBody.json");
        Response contextDTOResponse = CONTEXT_RequestsUtil.post(token, "CONTEXT_POST_ExpectedRegisterDTOBody.json");

        JSONObject contextDTOAtual = new JSONObject(contextDTOResponse.getBody().prettyPrint());
        String contextDTOId = contextDTOAtual.getString("id");

        Response challengeDTOResponseAnterior = CHALLENGE_RequestsUtil.post(
                token, "CHALLENGE_POST_ExpectedRegisterDTOBody.json", contextDTOId);

        given()
                .body(FileUtils.getJsonFromFile("CHALLENGE_POST_ExpectedRegisterDTOBody.json"))
                .contentType(ContentType.JSON)
                .headers("Authorization", "Bearer " + token,
                        "content-type", ContentType.JSON,
                        "Accept", ContentType.JSON)
                .when()
                .get(baseURI+":"+port+basePath+"auth/challenges/" + "-1")
                .then()
                .assertThat().statusCode(404);

        JSONObject challengeDTOAtual = new JSONObject(challengeDTOResponseAnterior.getBody().prettyPrint());
        String challengeIDAtual = challengeDTOAtual.getString("id");


        USER_RequestsUtil.delete(token);
        CONTEXT_RequestsUtil.delete(token, contextDTOId);
        CHALLENGE_RequestsUtil.delete(token, challengeIDAtual);
    }

    @Test
    public void findChallengeByIDMissingContext_ShouldReturn404Test() throws Exception {

        USER_RequestsUtil.post("USER_POST_ExpectedRegisterDTOBody.json");
        String token = USER_RequestsUtil.authenticate("USER_POST_ExpectedRegisterDTOBody.json");
        Response contextDTOResponse = CONTEXT_RequestsUtil.post(token, "CONTEXT_POST_ExpectedRegisterDTOBody.json");

        JSONObject contextDTOAtual = new JSONObject(contextDTOResponse.getBody().prettyPrint());
        String contextDTOId = contextDTOAtual.getString("id");

        Response challengeDTOResponseAnterior = CHALLENGE_RequestsUtil.post(
                token, "CHALLENGE_POST_ExpectedRegisterDTOBody.json", contextDTOId);

        JSONObject challengeDTOAtual = new JSONObject(challengeDTOResponseAnterior.getBody().prettyPrint());
        String challengeIDAtual = challengeDTOAtual.getString("id");

        CONTEXT_RequestsUtil.delete(token, contextDTOId);

        given()
                .body(FileUtils.getJsonFromFile("CHALLENGE_POST_ExpectedRegisterDTOBody.json"))
                .contentType(ContentType.JSON)
                .headers("Authorization", "Bearer " + token,
                        "content-type", ContentType.JSON,
                        "Accept", ContentType.JSON)
                .when()
                .get(baseURI+":"+port+basePath+"auth/challenges/" + challengeIDAtual)
                .then()
                .assertThat().statusCode(404);


        USER_RequestsUtil.delete(token);
        CHALLENGE_RequestsUtil.delete(token, challengeIDAtual);
    }

    @Test
    public void findChallengeByIDMissingToken_ShouldReturn400Test() throws Exception {

        USER_RequestsUtil.post("USER_POST_ExpectedRegisterDTOBody.json");
        String token = USER_RequestsUtil.authenticate("USER_POST_ExpectedRegisterDTOBody.json");
        Response contextDTOResponse = CONTEXT_RequestsUtil.post(token, "CONTEXT_POST_ExpectedRegisterDTOBody.json");

        JSONObject contextDTOAtual = new JSONObject(contextDTOResponse.getBody().prettyPrint());
        String contextDTOId = contextDTOAtual.getString("id");

        Response challengeDTOResponseAnterior = CHALLENGE_RequestsUtil.post(
                token, "CHALLENGE_POST_ExpectedRegisterDTOBody.json", contextDTOId);

        JSONObject challengeDTOAtual = new JSONObject(challengeDTOResponseAnterior.getBody().prettyPrint());
        String challengeIDAtual = challengeDTOAtual.getString("id");

        given()
                .body(FileUtils.getJsonFromFile("CHALLENGE_POST_ExpectedRegisterDTOBody.json"))
                .contentType(ContentType.JSON)
                .when()
                .get(baseURI+":"+port+basePath+"auth/challenges/" + challengeIDAtual)
                .then()
                .assertThat().statusCode(400);


        USER_RequestsUtil.delete(token);
        CONTEXT_RequestsUtil.delete(token, contextDTOId);
        CHALLENGE_RequestsUtil.delete(token, challengeIDAtual);
    }

    @Test
    public void findChallengeByMalformedOrExpiredToken_ShouldReturn500Test() throws Exception {

        USER_RequestsUtil.post("USER_POST_ExpectedRegisterDTOBody.json");
        String token = USER_RequestsUtil.authenticate("USER_POST_ExpectedRegisterDTOBody.json");
        Response contextDTOResponse = CONTEXT_RequestsUtil.post(token, "CONTEXT_POST_ExpectedRegisterDTOBody.json");

        JSONObject contextDTOAtual = new JSONObject(contextDTOResponse.getBody().prettyPrint());
        String contextDTOId = contextDTOAtual.getString("id");

        Response challengeDTOResponseAnterior = CHALLENGE_RequestsUtil.post(
                token, "CHALLENGE_POST_ExpectedRegisterDTOBody.json", contextDTOId);

        JSONObject challengeDTOAtual = new JSONObject(challengeDTOResponseAnterior.getBody().prettyPrint());
        String challengeIDAtual = challengeDTOAtual.getString("id");

        given()
                .body(FileUtils.getJsonFromFile("CHALLENGE_POST_ExpectedRegisterDTOBody.json"))
                .contentType(ContentType.JSON)
                .headers("Authorization", "Bearer " + invalidToken,
                        "content-type", ContentType.JSON,
                        "Accept", ContentType.JSON)
                .when()
                .get(baseURI+":"+port+basePath+"auth/challenges/" + challengeIDAtual)
                .then()
                .assertThat().statusCode(500);


        USER_RequestsUtil.delete(token);
        CONTEXT_RequestsUtil.delete(token, contextDTOId);
        CHALLENGE_RequestsUtil.delete(token, challengeIDAtual);
    }


    @Test
    public void findChallengeByQuery_ShouldReturn200Test() throws Exception {

        USER_RequestsUtil.post("USER_POST_ExpectedRegisterDTOBody.json");
        String token = USER_RequestsUtil.authenticate("USER_POST_ExpectedRegisterDTOBody.json");
        Response contextDTOResponse = CONTEXT_RequestsUtil.post(token, "CONTEXT_POST_ExpectedRegisterDTOBody.json");

        JSONObject contextDTOAtual = new JSONObject(contextDTOResponse.getBody().prettyPrint());
        String contextDTOId = contextDTOAtual.getString("id");

        Response challengeDTOResponseAnterior = CHALLENGE_RequestsUtil.post(
                token, "CHALLENGE_POST_ExpectedRegisterDTOBody.json", contextDTOId);

        given()
                .body(FileUtils.getJsonFromFile("CHALLENGE_POST_ExpectedRegisterDTOBody.json"))
                .contentType(ContentType.JSON)
                .headers("Authorization", "Bearer " + token,
                        "content-type", ContentType.JSON,
                        "Accept", ContentType.JSON)
                .when()
                .get(baseURI+":"+port+basePath+"challenges/" + "?word=&page=0&size=20")
                .then()
                .assertThat().statusCode(200);

        JSONObject challengeDTOAtual = new JSONObject(challengeDTOResponseAnterior.getBody().prettyPrint());
        String challengeIDAtual = challengeDTOAtual.getString("id");


        USER_RequestsUtil.delete(token);
        CONTEXT_RequestsUtil.delete(token, contextDTOId);
        CHALLENGE_RequestsUtil.delete(token, challengeIDAtual);
    }


    @Test
    public void updateChallengeByCreatorTokenBodyChallengeID_ShouldReturn200Test() throws Exception {

        USER_RequestsUtil.post("USER_POST_ExpectedRegisterDTOBody.json");
        String token = USER_RequestsUtil.authenticate("USER_POST_ExpectedRegisterDTOBody.json");
        Response contextDTOResponse = CONTEXT_RequestsUtil.post(token, "CONTEXT_POST_ExpectedRegisterDTOBody.json");

        JSONObject contextDTOAtual = new JSONObject(contextDTOResponse.getBody().prettyPrint());
        String contextDTOId = contextDTOAtual.getString("id");

        Response challengeDTOResponseAnterior = CHALLENGE_RequestsUtil.post(token, "CHALLENGE_POST_ExpectedRegisterDTOBody.json", contextDTOId);

        JSONObject challengeDTOEsperado = new JSONObject(challengeDTOResponseAnterior.getBody().prettyPrint());

        String challengeIDExpected = challengeDTOEsperado.getString("id");

        Response challengeDTOResponseAtual = given()
                .body(FileUtils.getJsonFromFile("CHALLENGE_PUT_ExpectedRegisterDTOBody.json"))
                .contentType(ContentType.JSON)
                .headers("Authorization", "Bearer " + token,
                        "content-type", ContentType.JSON,
                        "Accept", ContentType.JSON)
                .when()
                .put(baseURI+":"+port+basePath+"auth/challenges/" + challengeIDExpected)
                .then()
                .assertThat().statusCode(200)
                .extract().response();

        JSONObject challengeDTOAtual = new JSONObject(challengeDTOResponseAtual.getBody().prettyPrint());
        JSONObject challengeRegisterDTOEsperado = new JSONObject(FileUtils.getJsonFromFile("CHALLENGE_POST_ExpectedRegisterDTOBody.json"));

        Assertions.assertNotNull(challengeDTOAtual.getString("id"));
        Assertions.assertNotEquals(challengeRegisterDTOEsperado.getString("word"), challengeDTOAtual.getString("word"));
        Assertions.assertEquals(challengeRegisterDTOEsperado.getString("imageUrl"), challengeDTOAtual.getString("imageUrl"));
        Assertions.assertEquals(challengeRegisterDTOEsperado.getString("soundUrl"), challengeDTOAtual.getString("soundUrl"));
        Assertions.assertEquals(challengeRegisterDTOEsperado.getString("videoUrl"), challengeDTOAtual.getString("videoUrl"));


        USER_RequestsUtil.delete(token);
        CONTEXT_RequestsUtil.delete(token, contextDTOId);
        CHALLENGE_RequestsUtil.delete(token, challengeDTOAtual.getString("id"));
    }

    @Test
    public void updateChallengeByWordAlreadyExists_ShouldReturn304Test() throws Exception {

        USER_RequestsUtil.post("USER_POST_ExpectedRegisterDTOBody.json");
        String token = USER_RequestsUtil.authenticate("USER_POST_ExpectedRegisterDTOBody.json");
        Response contextDTOResponse = CONTEXT_RequestsUtil.post(token, "CONTEXT_POST_ExpectedRegisterDTOBody.json");

        JSONObject contextDTOAtual = new JSONObject(contextDTOResponse.getBody().prettyPrint());
        String contextDTOId = contextDTOAtual.getString("id");

        Response challengeDTOResponseAnterior = CHALLENGE_RequestsUtil.post(token, "CHALLENGE_POST_ExpectedRegisterDTOBody.json", contextDTOId);

        JSONObject challengeDTOEsperado = new JSONObject(challengeDTOResponseAnterior.getBody().prettyPrint());

        String challengeIDExpected = challengeDTOEsperado.getString("id");

        given()
                .body(FileUtils.getJsonFromFile("CHALLENGE_POST_ExpectedRegisterDTOBody.json"))
                .contentType(ContentType.JSON)
                .headers("Authorization", "Bearer " + token,
                        "content-type", ContentType.JSON,
                        "Accept", ContentType.JSON)
                .when()
                .put(baseURI+":"+port+basePath+"auth/challenges/" + challengeIDExpected)
                .then()
                .assertThat().statusCode(304)
                .extract().response();
        

        USER_RequestsUtil.delete(token);
        CONTEXT_RequestsUtil.delete(token, contextDTOId);
        CHALLENGE_RequestsUtil.delete(token, challengeIDExpected);
    }

    @Test
    public void updateChallengeMissingBody_ShouldReturn400Test() throws Exception {

        USER_RequestsUtil.post("USER_POST_ExpectedRegisterDTOBody.json");
        String token = USER_RequestsUtil.authenticate("USER_POST_ExpectedRegisterDTOBody.json");
        Response contextDTOResponse = CONTEXT_RequestsUtil.post(token, "CONTEXT_POST_ExpectedRegisterDTOBody.json");

        JSONObject contextDTOAtual = new JSONObject(contextDTOResponse.getBody().prettyPrint());
        String contextDTOId = contextDTOAtual.getString("id");

        Response challengeDTOResponseAnterior = CHALLENGE_RequestsUtil.post(token, "CHALLENGE_POST_ExpectedRegisterDTOBody.json", contextDTOId);

        JSONObject challengeDTOEsperado = new JSONObject(challengeDTOResponseAnterior.getBody().prettyPrint());

        String challengeIDExpected = challengeDTOEsperado.getString("id");

        given().headers("Authorization", "Bearer " + token,
                        "content-type", ContentType.JSON,
                        "Accept", ContentType.JSON)
                .when()
                .put(baseURI+":"+port+basePath+"auth/challenges/" + challengeIDExpected)
                .then()
                .assertThat().statusCode(400);


        USER_RequestsUtil.delete(token);
        CONTEXT_RequestsUtil.delete(token, contextDTOId);
    }

    @Test
    public void updateChallengeMissingID_ShouldReturn405Test() throws Exception {

        USER_RequestsUtil.post("USER_POST_ExpectedRegisterDTOBody.json");
        String token = USER_RequestsUtil.authenticate("USER_POST_ExpectedRegisterDTOBody.json");
        Response contextDTOResponse = CONTEXT_RequestsUtil.post(token, "CONTEXT_POST_ExpectedRegisterDTOBody.json");

        JSONObject contextDTOAtual = new JSONObject(contextDTOResponse.getBody().prettyPrint());
        String contextDTOId = contextDTOAtual.getString("id");

        Response challengeDTOResponseAnterior = CHALLENGE_RequestsUtil.post(token, "CHALLENGE_POST_ExpectedRegisterDTOBody.json", contextDTOId);

        JSONObject challengeDTOEsperado = new JSONObject(challengeDTOResponseAnterior.getBody().prettyPrint());
        String challengeIDExpected = challengeDTOEsperado.getString("id");

        given()
                .body(FileUtils.getJsonFromFile("CHALLENGE_POST_ExpectedRegisterDTOBody.json"))
                .contentType(ContentType.JSON)
                .headers("Authorization", "Bearer " + token,
                        "content-type", ContentType.JSON,
                        "Accept", ContentType.JSON)
                .when()
                .put(baseURI+":"+port+basePath+"auth/challenges/")
                .then()
                .assertThat().statusCode(405);


        USER_RequestsUtil.delete(token);
        CONTEXT_RequestsUtil.delete(token, contextDTOId);
        CHALLENGE_RequestsUtil.delete(token, challengeIDExpected);
    }

    @Test
    public void updateChallengeByNonNumericID_ShouldReturn405Test() throws Exception {

        USER_RequestsUtil.post("USER_POST_ExpectedRegisterDTOBody.json");
        String token = USER_RequestsUtil.authenticate("USER_POST_ExpectedRegisterDTOBody.json");
        Response contextDTOResponse = CONTEXT_RequestsUtil.post(token, "CONTEXT_POST_ExpectedRegisterDTOBody.json");

        JSONObject contextDTOAtual = new JSONObject(contextDTOResponse.getBody().prettyPrint());
        String contextDTOId = contextDTOAtual.getString("id");

        Response challengeDTOResponseAnterior = CHALLENGE_RequestsUtil.post(token, "CHALLENGE_POST_ExpectedRegisterDTOBody.json", contextDTOId);

        JSONObject challengeDTOEsperado = new JSONObject(challengeDTOResponseAnterior.getBody().prettyPrint());
        String challengeIDExpected = challengeDTOEsperado.getString("id");

        given()
                .body(FileUtils.getJsonFromFile("CHALLENGE_POST_ExpectedRegisterDTOBody.json"))
                .contentType(ContentType.JSON)
                .headers("Authorization", "Bearer " + token,
                        "content-type", ContentType.JSON,
                        "Accept", ContentType.JSON)
                .when()
                .put(baseURI+":"+port+basePath+"auth/challenges/" +"abc")
                .then()
                .assertThat().statusCode(400);


        USER_RequestsUtil.delete(token);
        CONTEXT_RequestsUtil.delete(token, contextDTOId);
        CHALLENGE_RequestsUtil.delete(token, challengeIDExpected);
    }

    @Test
    public void updateChallengeMissingContext_ShouldReturn400Test() throws Exception {

        USER_RequestsUtil.post("USER_POST_ExpectedRegisterDTOBody.json");
        String token = USER_RequestsUtil.authenticate("USER_POST_ExpectedRegisterDTOBody.json");
        Response contextDTOResponse = CONTEXT_RequestsUtil.post(token, "CONTEXT_POST_ExpectedRegisterDTOBody.json");

        JSONObject contextDTOAtual = new JSONObject(contextDTOResponse.getBody().prettyPrint());
        String contextDTOId = contextDTOAtual.getString("id");

        Response challengeDTOResponseAnterior = CHALLENGE_RequestsUtil.post(token, "CHALLENGE_POST_ExpectedRegisterDTOBody.json", contextDTOId);

        JSONObject challengeDTOEsperado = new JSONObject(challengeDTOResponseAnterior.getBody().prettyPrint());
        String challengeIDExpected = challengeDTOEsperado.getString("id");

        CONTEXT_RequestsUtil.delete(token, contextDTOId);

        given()
                .body(FileUtils.getJsonFromFile("CHALLENGE_POST_ExpectedRegisterDTOBody.json"))
                .contentType(ContentType.JSON)
                .headers("Authorization", "Bearer " + token,
                        "content-type", ContentType.JSON,
                        "Accept", ContentType.JSON)
                .when()
                .put(baseURI+":"+port+basePath+"auth/challenges/" + challengeIDExpected)
                .then()
                .assertThat().statusCode(404);


        USER_RequestsUtil.delete(token);
    }

    @Test
    public void updateChallengeByIDLinkedToMissingChallenge_ShouldReturn404() throws Exception {

        USER_RequestsUtil.post("USER_POST_ExpectedRegisterDTOBody.json");
        String token = USER_RequestsUtil.authenticate("USER_POST_ExpectedRegisterDTOBody.json");
        Response contextDTOResponse = CONTEXT_RequestsUtil.post(token, "CONTEXT_POST_ExpectedRegisterDTOBody.json");

        JSONObject contextDTOAtual = new JSONObject(contextDTOResponse.getBody().prettyPrint());
        String contextDTOId = contextDTOAtual.getString("id");

        Response challengeDTOResponseAnterior = CHALLENGE_RequestsUtil.post(token, "CHALLENGE_POST_ExpectedRegisterDTOBody.json", contextDTOId);

        JSONObject challengeDTOEsperado = new JSONObject(challengeDTOResponseAnterior.getBody().prettyPrint());
        String challengeIDExpected = challengeDTOEsperado.getString("id");

        CHALLENGE_RequestsUtil.delete(token, challengeIDExpected);

        given()
                .body(FileUtils.getJsonFromFile("CHALLENGE_POST_ExpectedRegisterDTOBody.json"))
                .contentType(ContentType.JSON)
                .headers("Authorization", "Bearer " + token,
                        "content-type", ContentType.JSON,
                        "Accept", ContentType.JSON)
                .when()
                .put(baseURI+":"+port+basePath+"auth/challenges/" + challengeIDExpected)
                .then()
                .assertThat().statusCode(404);


        USER_RequestsUtil.delete(token);
        CONTEXT_RequestsUtil.delete(token, contextDTOId);
    }

    @Test
    public void updateChallengeByTokenLinkedToMissingCreator_ShouldReturn404Test() throws Exception {

        USER_RequestsUtil.post("USER_POST_ExpectedRegisterDTOBody.json");
        String token = USER_RequestsUtil.authenticate("USER_POST_ExpectedRegisterDTOBody.json");
        Response contextDTOResponse = CONTEXT_RequestsUtil.post(token, "CONTEXT_POST_ExpectedRegisterDTOBody.json");

        JSONObject contextDTOAtual = new JSONObject(contextDTOResponse.getBody().prettyPrint());
        String contextDTOId = contextDTOAtual.getString("id");

        Response challengeDTOResponseAnterior = CHALLENGE_RequestsUtil.post(token, "CHALLENGE_POST_ExpectedRegisterDTOBody.json", contextDTOId);

        JSONObject challengeDTOEsperado = new JSONObject(challengeDTOResponseAnterior.getBody().prettyPrint());

        String challengeIDExpected = challengeDTOEsperado.getString("id");

        CONTEXT_RequestsUtil.delete(token, contextDTOId);

        given()
                .body(FileUtils.getJsonFromFile("CHALLENGE_POST_ExpectedRegisterDTOBody.json"))
                .contentType(ContentType.JSON)
                .headers("Authorization", "Bearer " + token,
                        "content-type", ContentType.JSON,
                        "Accept", ContentType.JSON)
                .when()
                .put(baseURI+":"+port+basePath+"auth/challenges/" + challengeIDExpected)
                .then()
                .assertThat().statusCode(404);


        USER_RequestsUtil.delete(token);
        CHALLENGE_RequestsUtil.delete(token, challengeIDExpected);
    }

    @Test
    public void updateChallengeMissingToken_ShouldReturn400Test() throws Exception {

        USER_RequestsUtil.post("USER_POST_ExpectedRegisterDTOBody.json");
        String token = USER_RequestsUtil.authenticate("USER_POST_ExpectedRegisterDTOBody.json");
        Response contextDTOResponse = CONTEXT_RequestsUtil.post(token, "CONTEXT_POST_ExpectedRegisterDTOBody.json");

        JSONObject contextDTOAtual = new JSONObject(contextDTOResponse.getBody().prettyPrint());
        String contextDTOId = contextDTOAtual.getString("id");

        Response challengeDTOResponseAnterior = CHALLENGE_RequestsUtil.post(token, "CHALLENGE_POST_ExpectedRegisterDTOBody.json", contextDTOId);

        JSONObject challengeDTOEsperado = new JSONObject(challengeDTOResponseAnterior.getBody().prettyPrint());

        String challengeIDExpected = challengeDTOEsperado.getString("id");

        given()
                .body(FileUtils.getJsonFromFile("CHALLENGE_POST_ExpectedRegisterDTOBody.json"))
                .contentType(ContentType.JSON)
                .when()
                .put(baseURI+":"+port+basePath+"auth/challenges/" + challengeIDExpected)
                .then()
                .assertThat().statusCode(400);


        USER_RequestsUtil.delete(token);
        CONTEXT_RequestsUtil.delete(token, contextDTOId);
        CHALLENGE_RequestsUtil.delete(token, challengeIDExpected);
    }

    @Test
    public void updateChallengeByMalformedOrExpiredToken_ShouldReturn500Test() throws Exception {

        USER_RequestsUtil.post("USER_POST_ExpectedRegisterDTOBody.json");
        String token = USER_RequestsUtil.authenticate("USER_POST_ExpectedRegisterDTOBody.json");
        Response contextDTOResponse = CONTEXT_RequestsUtil.post(token, "CONTEXT_POST_ExpectedRegisterDTOBody.json");

        JSONObject contextDTOAtual = new JSONObject(contextDTOResponse.getBody().prettyPrint());
        String contextDTOId = contextDTOAtual.getString("id");

        Response challengeDTOResponseAnterior = CHALLENGE_RequestsUtil.post(token, "CHALLENGE_POST_ExpectedRegisterDTOBody.json", contextDTOId);

        JSONObject challengeDTOEsperado = new JSONObject(challengeDTOResponseAnterior.getBody().prettyPrint());

        String challengeIDExpected = challengeDTOEsperado.getString("id");

        given()
                .body(FileUtils.getJsonFromFile("CHALLENGE_POST_ExpectedRegisterDTOBody.json"))
                .contentType(ContentType.JSON)
                .headers("Authorization", "Bearer " + invalidToken,
                        "content-type", ContentType.JSON,
                        "Accept", ContentType.JSON)
                .when()
                .put(baseURI+":"+port+basePath+"auth/challenges/" + challengeIDExpected)
                .then()
                .assertThat().statusCode(500);


        USER_RequestsUtil.delete(token);
        CONTEXT_RequestsUtil.delete(token, contextDTOId);
        CHALLENGE_RequestsUtil.delete(token, challengeIDExpected);
    }


    @Test
    public void deleteChallengeByCreatorTokenID_ShouldReturn200Test() throws Exception {

        USER_RequestsUtil.post("USER_POST_ExpectedRegisterDTOBody.json");
        String token = USER_RequestsUtil.authenticate("USER_POST_ExpectedRegisterDTOBody.json");
        Response contextDTOResponse = CONTEXT_RequestsUtil.post(token, "CONTEXT_POST_ExpectedRegisterDTOBody.json");

        JSONObject contextDTOJSON = new JSONObject(contextDTOResponse.getBody().prettyPrint());
        String contextDTOId = contextDTOJSON.getString("id");

        Response challengeDTOResponseAnterior = CHALLENGE_RequestsUtil.post(token, "CHALLENGE_POST_ExpectedRegisterDTOBody.json",contextDTOId);

        JSONObject challengeDTOJSON = new JSONObject(challengeDTOResponseAnterior.getBody().prettyPrint());
        String challengeID =challengeDTOJSON.getString("id");

        given()
                .contentType(ContentType.JSON)
                .headers("Authorization", "Bearer " + token,
                        "content-type", ContentType.JSON,
                        "Accept", ContentType.JSON)
                .when()
                .delete(baseURI+":"+port+basePath+"auth/challenges/" + challengeID)
                .then()
                .assertThat().statusCode(200);


        USER_RequestsUtil.delete(token);
        CONTEXT_RequestsUtil.delete(token, contextDTOId);
    }

    @Test
    public void deleteChallengeMissingID_ShouldReturn405Test() throws Exception {

        USER_RequestsUtil.post("USER_POST_ExpectedRegisterDTOBody.json");
        String token = USER_RequestsUtil.authenticate("USER_POST_ExpectedRegisterDTOBody.json");
        Response contextDTOResponse = CONTEXT_RequestsUtil.post(token, "CONTEXT_POST_ExpectedRegisterDTOBody.json");

        JSONObject contextDTOJSON = new JSONObject(contextDTOResponse.getBody().prettyPrint());
        String contextDTOId = contextDTOJSON.getString("id");

        Response challengeDTOResponseAnterior = CHALLENGE_RequestsUtil.post(token, "CHALLENGE_POST_ExpectedRegisterDTOBody.json",contextDTOId);

        JSONObject challengeDTOEsperado = new JSONObject(challengeDTOResponseAnterior.getBody().prettyPrint());
        String challengeIDExpected = challengeDTOEsperado.getString("id");

        given()
                .contentType(ContentType.JSON)
                .headers("Authorization", "Bearer " + token,
                        "content-type", ContentType.JSON,
                        "Accept", ContentType.JSON)
                .when()
                .delete(baseURI+":"+port+basePath+"auth/challenges/")
                .then()
                .assertThat().statusCode(405);


        USER_RequestsUtil.delete(token);
        CONTEXT_RequestsUtil.delete(token, contextDTOId);
        CHALLENGE_RequestsUtil.delete(token, challengeIDExpected);
    }

    @Test
    public void deleteChallengeByIDContextMissing_ShouldReturn404Test() throws Exception {

        USER_RequestsUtil.post("USER_POST_ExpectedRegisterDTOBody.json");
        String token = USER_RequestsUtil.authenticate("USER_POST_ExpectedRegisterDTOBody.json");
        Response contextDTOResponse = CONTEXT_RequestsUtil.post(token, "CONTEXT_POST_ExpectedRegisterDTOBody.json");

        JSONObject contextDTOJSON = new JSONObject(contextDTOResponse.getBody().prettyPrint());
        String contextDTOId = contextDTOJSON.getString("id");

        Response challengeDTOResponseAnterior = CHALLENGE_RequestsUtil.post(token, "CHALLENGE_POST_ExpectedRegisterDTOBody.json",contextDTOId);

        JSONObject challengeDTOEsperado = new JSONObject(challengeDTOResponseAnterior.getBody().prettyPrint());
        String challengeIDExpected = challengeDTOEsperado.getString("id");

        CONTEXT_RequestsUtil.delete(token, contextDTOId);

        given()
                .contentType(ContentType.JSON)
                .headers("Authorization", "Bearer " + token,
                        "content-type", ContentType.JSON,
                        "Accept", ContentType.JSON)
                .when()
                .delete(baseURI+":"+port+basePath+"auth/challenges/" + challengeIDExpected)
                .then()
                .assertThat().statusCode(404);


        USER_RequestsUtil.delete(token);
        CHALLENGE_RequestsUtil.delete(token, challengeIDExpected);
    }

    @Test
    public void deleteChallengeByNonNumericID_ShouldReturn400Test() throws Exception {

        USER_RequestsUtil.post("USER_POST_ExpectedRegisterDTOBody.json");
        String token = USER_RequestsUtil.authenticate("USER_POST_ExpectedRegisterDTOBody.json");
        Response contextDTOResponse = CONTEXT_RequestsUtil.post(token, "CONTEXT_POST_ExpectedRegisterDTOBody.json");

        JSONObject contextDTOJSON = new JSONObject(contextDTOResponse.getBody().prettyPrint());
        String contextDTOId = contextDTOJSON.getString("id");

        Response challengeDTOResponseAnterior = CHALLENGE_RequestsUtil.post(token, "CHALLENGE_POST_ExpectedRegisterDTOBody.json",contextDTOId);

        JSONObject challengeDTOEsperado = new JSONObject(challengeDTOResponseAnterior.getBody().prettyPrint());
        String challengeIDExpected = challengeDTOEsperado.getString("id");

        given()
                .contentType(ContentType.JSON)
                .headers("Authorization", "Bearer " + token,
                        "content-type", ContentType.JSON,
                        "Accept", ContentType.JSON)
                .when()
                .delete(baseURI+":"+port+basePath+"auth/challenges/" + "abc")
                .then()
                .assertThat().statusCode(400);


        USER_RequestsUtil.delete(token);
        CONTEXT_RequestsUtil.delete(token, contextDTOId);
        CHALLENGE_RequestsUtil.delete(token, challengeIDExpected);
    }

    @Test
    public void deleteChallengeMissingToken_ShouldReturn400Test() throws Exception {

        USER_RequestsUtil.post("USER_POST_ExpectedRegisterDTOBody.json");
        String token = USER_RequestsUtil.authenticate("USER_POST_ExpectedRegisterDTOBody.json");
        Response contextDTOResponse = CONTEXT_RequestsUtil.post(token, "CONTEXT_POST_ExpectedRegisterDTOBody.json");

        JSONObject contextDTOJSON = new JSONObject(contextDTOResponse.getBody().prettyPrint());
        String contextDTOId = contextDTOJSON.getString("id");

        Response challengeDTOResponseAnterior = CHALLENGE_RequestsUtil.post(token, "CHALLENGE_POST_ExpectedRegisterDTOBody.json",contextDTOId);

        JSONObject challengeDTOEsperado = new JSONObject(challengeDTOResponseAnterior.getBody().prettyPrint());
        String challengeIDExpected = challengeDTOEsperado.getString("id");

        given()
                .contentType(ContentType.JSON)
                .when()
                .delete(baseURI+":"+port+basePath+"auth/challenges/" + challengeIDExpected)
                .then()
                .assertThat().statusCode(400);


        USER_RequestsUtil.delete(token);
        CONTEXT_RequestsUtil.delete(token, contextDTOId);
        CHALLENGE_RequestsUtil.delete(token, challengeIDExpected);
    }

    @Test
    public void deleteChallengeByTokenLinkedToMissingCreator_ShouldReturn404Test() throws Exception {

        USER_RequestsUtil.post("USER_POST_ExpectedRegisterDTOBody.json");
        String token = USER_RequestsUtil.authenticate("USER_POST_ExpectedRegisterDTOBody.json");
        Response contextDTOResponse = CONTEXT_RequestsUtil.post(token, "CONTEXT_POST_ExpectedRegisterDTOBody.json");

        JSONObject contextDTOJSON = new JSONObject(contextDTOResponse.getBody().prettyPrint());
        String contextDTOId = contextDTOJSON.getString("id");

        Response challengeDTOResponseAnterior = CHALLENGE_RequestsUtil.post(token, "CHALLENGE_POST_ExpectedRegisterDTOBody.json",contextDTOId);

        JSONObject challengeDTOEsperado = new JSONObject(challengeDTOResponseAnterior.getBody().prettyPrint());
        String challengeIDExpected = challengeDTOEsperado.getString("id");

        USER_RequestsUtil.delete(token);

        given()
                .contentType(ContentType.JSON)
                .headers("Authorization", "Bearer " + token,
                        "content-type", ContentType.JSON,
                        "Accept", ContentType.JSON)
                .when()
                .delete(baseURI+":"+port+basePath+"auth/challenges/" + challengeIDExpected)
                .then()
                .assertThat().statusCode(404);


        CONTEXT_RequestsUtil.delete(token, contextDTOId);
        CHALLENGE_RequestsUtil.delete(token, challengeIDExpected);
    }

    @Test
    public void deleteChallengeByCreatorTokenMalformedOrExpired_ShouldReturn500Test() throws Exception {

        USER_RequestsUtil.post("USER_POST_ExpectedRegisterDTOBody.json");
        String token = USER_RequestsUtil.authenticate("USER_POST_ExpectedRegisterDTOBody.json");
        Response contextDTOResponse = CONTEXT_RequestsUtil.post(token, "CONTEXT_POST_ExpectedRegisterDTOBody.json");

        JSONObject contextDTOJSON = new JSONObject(contextDTOResponse.getBody().prettyPrint());
        String contextDTOId = contextDTOJSON.getString("id");

        Response challengeDTOResponseAnterior = CHALLENGE_RequestsUtil.post(token, "CHALLENGE_POST_ExpectedRegisterDTOBody.json",contextDTOId);

        JSONObject challengeDTOEsperado = new JSONObject(challengeDTOResponseAnterior.getBody().prettyPrint());
        String challengeIDExpected = challengeDTOEsperado.getString("id");

        given()
                .contentType(ContentType.JSON)
                .headers("Authorization", "Bearer " + invalidToken,
                        "content-type", ContentType.JSON,
                        "Accept", ContentType.JSON)
                .when()
                .delete(baseURI+":"+port+basePath+"auth/challenges/" + challengeIDExpected)
                .then()
                .assertThat().statusCode(500);


        USER_RequestsUtil.delete(token);
        CONTEXT_RequestsUtil.delete(token, contextDTOId);
        CHALLENGE_RequestsUtil.delete(token, challengeIDExpected);
    }
}