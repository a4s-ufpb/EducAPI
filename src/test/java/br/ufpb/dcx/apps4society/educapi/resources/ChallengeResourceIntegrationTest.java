package br.ufpb.dcx.apps4society.educapi.resources;

import br.ufpb.dcx.apps4society.educapi.dto.challenge.ChallengeDTO;
import br.ufpb.dcx.apps4society.educapi.unit.domain.builder.ChallengeBuilder;
import br.ufpb.dcx.apps4society.educapi.utils.CHALLENGE_RequestsUtil;
import br.ufpb.dcx.apps4society.educapi.utils.CONTEXT_RequestsUtil;
import br.ufpb.dcx.apps4society.educapi.utils.FileUtils;
import br.ufpb.dcx.apps4society.educapi.utils.USER_RequestsUtil;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.restassured.http.ContentType;
import io.restassured.response.Response;
import org.json.JSONException;
import org.json.JSONObject;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.File;

import static io.restassured.RestAssured.*;

public class ChallengeResourceIntegrationTest {

    private static String USER_POST_ENDPOINT = baseURI+":"+port+basePath+"users";
    private static String USER_AUTENTICATION_ENDPOINT = baseURI+":"+port+basePath+"auth/login";
    private static String CONTEXT_POST_ENDPOINT = baseURI+":"+port+basePath+"contexts";
    private static String CHALLENGE_POST_ENDPOINT = baseURI+":"+port+basePath+"challenges";

    private static String invalidToken = "eyJhbGciOiJIUzUxMiJ9.eyJzdWIiOiJqb3NlMTdAZWR1Y2FwaS5jb20iLCJleHAiOjE2ODA2OTc2MjN9.qfwlZuirBvosD82v-7lHxb8qhH54_KXR20_0z3guG9rZOW68l5y3gZtvugBtpevmlgK76dsa4hOUPOooRiJ3ng";

    @BeforeEach
    public void setUp(){

        baseURI = "http://localhost";
        port = 8080;
        basePath = "/v1/api/";

    }

    @Test
    public void insertChallengeByCreatorTokenBodyContextID_ShouldReturn201Test() throws Exception {

        USER_RequestsUtil.postUser("USER_POST_ExpectedRegisterDTOBody.json");
        String token = USER_RequestsUtil.authenticateUser("USER_POST_ExpectedRegisterDTOBody.json");
        Response contextDTOResponse = CONTEXT_RequestsUtil.postContext(token, "CONTEXT_POST_ExpectedRegisterDTOBody.json");

        JSONObject contextDTOJSONActual = new JSONObject(contextDTOResponse.getBody().prettyPrint());
        String actualContextID = contextDTOJSONActual.getString("id");

        //Create challenge
        Response challengeDTOResponse = given()
                .body(FileUtils.getJsonFromFile("CHALLENGE_POST_ExpectedRegisterDTOBody.json"))
                .contentType(ContentType.JSON)
                .headers("Authorization", "Bearer " + token,
                        "content-type", ContentType.JSON,
                        "Accept", ContentType.JSON)
                .when()
                .post(baseURI+":"+port+basePath+"auth/challenges/" + actualContextID)
                .then()
                .assertThat().statusCode(201)
                .extract().response();

        JSONObject challengeDTOJSONActual = new JSONObject(challengeDTOResponse.getBody().prettyPrint());

        String actualChallengeID = challengeDTOJSONActual.getString("id");
        String actualChallengeImageURL = challengeDTOJSONActual.getString("imageUrl");
        String actualChallengeWord = challengeDTOJSONActual.getString("word");
        String actualChallengeSoundURL = challengeDTOJSONActual.getString("soundUrl");
        String actualChallengeVideoURL = challengeDTOJSONActual.getString("videoUrl");

        ChallengeDTO challengeDTO = ChallengeBuilder.anChallenge()
                .withId(Long.valueOf(actualChallengeID))
                .withWord(actualChallengeWord)
                .withImageUrl(actualChallengeImageURL)
                .withSoundUrl(actualChallengeSoundURL)
                .withVideoUrl(actualChallengeVideoURL).buildChallengeDTO();

        ObjectMapper mapper =  new ObjectMapper();
        mapper.writeValue(new File("src/test/resources/CHALLENGE_ActualContextDTOBody[spawned].json"), challengeDTO);
        JSONObject challengeJSONExpected = new JSONObject(FileUtils.getJsonFromFile("CHALLENGE_POST_ExpectedRegisterDTOBody.json"));

        Assertions.assertNotNull(challengeDTOJSONActual.getString("id"));
        Assertions.assertEquals(challengeJSONExpected.getString("word"), challengeDTOJSONActual.getString("word"));
        Assertions.assertEquals(challengeJSONExpected.getString("imageUrl"), challengeDTOJSONActual.getString("imageUrl"));
        Assertions.assertEquals(challengeJSONExpected.getString("soundUrl"), challengeDTOJSONActual.getString("soundUrl"));
        Assertions.assertEquals(challengeJSONExpected.getString("videoUrl"), challengeDTOJSONActual.getString("videoUrl"));

        USER_RequestsUtil.deleteUser(token);
        CONTEXT_RequestsUtil.deleteContext(token, actualContextID);
        CHALLENGE_RequestsUtil.deleteChallenge(token, actualChallengeID);

    }

    @Test
    public void insertChallengeByAlreadyExistingWord_ShouldReturn201Test() throws Exception {

        USER_RequestsUtil.postUser("USER_POST_ExpectedRegisterDTOBody.json");
        String token = USER_RequestsUtil.authenticateUser("USER_POST_ExpectedRegisterDTOBody.json");
        Response contextDTOResponse = CONTEXT_RequestsUtil.postContext(token, "CONTEXT_POST_ExpectedRegisterDTOBody.json");

        JSONObject contextDTOJSONActual = new JSONObject(contextDTOResponse.getBody().prettyPrint());
        String actualContextID = contextDTOJSONActual.getString("id");

        CHALLENGE_RequestsUtil.postChallenge(token, "CHALLENGE_POST_ExpectedRegisterDTOBody.json", actualContextID);

        //Create challenge
        Response challengeDTOResponse = given()
                .body(FileUtils.getJsonFromFile("CHALLENGE_POST_ExpectedRegisterDTOBody.json"))
                .contentType(ContentType.JSON)
                .headers("Authorization", "Bearer " + token,
                        "content-type", ContentType.JSON,
                        "Accept", ContentType.JSON)
                .when()
                .post(baseURI+":"+port+basePath+"auth/challenges/" + actualContextID)
                .then()
                .assertThat().statusCode(201)
                .extract().response();

        JSONObject challengeDTOJSONActual = new JSONObject(challengeDTOResponse.getBody().prettyPrint());
        String actualChallengeID = challengeDTOJSONActual.getString("id");

        USER_RequestsUtil.deleteUser(token);
        CONTEXT_RequestsUtil.deleteContext(token, actualContextID);
        CHALLENGE_RequestsUtil.deleteChallenge(token, actualChallengeID);

    }

    @Test
    public void insertChallengeByInvalidWordLessThan2Characters_ShouldReturn400Test() throws Exception {

        USER_RequestsUtil.postUser("USER_POST_ExpectedRegisterDTOBody.json");
        String token = USER_RequestsUtil.authenticateUser("USER_AuthenticateBody.json");
        Response contextDTOResponse = CONTEXT_RequestsUtil.postContext(token, "CONTEXT_POST_ExpectedRegisterDTOBody.json");

        JSONObject contextDTOJSONActual = new JSONObject(contextDTOResponse.getBody().prettyPrint());
        String actualContextID = contextDTOJSONActual.getString("id");

        given().body(FileUtils.getJsonFromFile("CHALLENGE_POST_InvalidWordLessThan2CharactersBody.json"))
                .contentType(ContentType.JSON)
                .headers("Authorization", "Bearer " + token,
                        "content-type", ContentType.JSON,
                        "Accept", ContentType.JSON)
                .when().post(baseURI+":"+port+basePath+"auth/challenges/" + actualContextID)
                .then().assertThat().statusCode(400);

        USER_RequestsUtil.deleteUser(token);
        CONTEXT_RequestsUtil.deleteContext(token, actualContextID);

    }

    @Test
    public void insertChallengeByInvalidWordMoreThan72Characters_ShouldReturn400Test() throws Exception {

        USER_RequestsUtil.postUser("USER_POST_ExpectedRegisterDTOBody.json");
        String token = USER_RequestsUtil.authenticateUser("USER_AuthenticateBody.json");
        Response contextDTOResponse = CONTEXT_RequestsUtil.postContext(token, "CONTEXT_POST_ExpectedRegisterDTOBody.json");

        JSONObject contextDTOJSONActual = new JSONObject(contextDTOResponse.getBody().prettyPrint());
        String actualContextID = contextDTOJSONActual.getString("id");

        given().body(FileUtils.getJsonFromFile("CHALLENGE_POST_InvalidWordMoreThan72CharactersBody.json"))
                .contentType(ContentType.JSON)
                .headers("Authorization", "Bearer " + token,
                        "content-type", ContentType.JSON,
                        "Accept", ContentType.JSON)
                .when().post(baseURI+":"+port+basePath+"auth/challenges/" + actualContextID)
                .then().assertThat().statusCode(400);

        USER_RequestsUtil.deleteUser(token);
        CONTEXT_RequestsUtil.deleteContext(token, actualContextID);

    }

    @Test
    public void insertChallengeMissingID_ShouldReturn405Test() throws Exception {

        USER_RequestsUtil.postUser("USER_POST_ExpectedRegisterDTOBody.json");
        String token = USER_RequestsUtil.authenticateUser("USER_AuthenticateBody.json");
        Response contextDTOResponse = CONTEXT_RequestsUtil.postContext(token, "CONTEXT_POST_ExpectedRegisterDTOBody.json");

        JSONObject contextDTOJSONActual = new JSONObject(contextDTOResponse.getBody().prettyPrint());
        String actualContextID = contextDTOJSONActual.getString("id");

        given().body(FileUtils.getJsonFromFile("CHALLENGE_POST_ExpectedRegisterDTOBody.json"))
                .contentType(ContentType.JSON)
                .headers("Authorization", "Bearer " + token,
                        "content-type", ContentType.JSON,
                        "Accept", ContentType.JSON)
                .when().post(baseURI+":"+port+basePath+"auth/challenges")
                .then().assertThat().statusCode(405);

        USER_RequestsUtil.deleteUser(token);
        CONTEXT_RequestsUtil.deleteContext(token, actualContextID);

    }

    @Test
    public void insertChallengeByContextIDLinkedToMissingContext_ShouldReturn404Test() throws Exception {

        USER_RequestsUtil.postUser("USER_POST_ExpectedRegisterDTOBody.json");
        String token = USER_RequestsUtil.authenticateUser("USER_POST_ExpectedRegisterDTOBody.json");
        Response contextDTOResponse = CONTEXT_RequestsUtil.postContext(token, "CONTEXT_POST_ExpectedRegisterDTOBody.json");

        JSONObject contextDTOJSONActual = new JSONObject(contextDTOResponse.getBody().prettyPrint());
        String actualContextID = contextDTOJSONActual.getString("id");

        CONTEXT_RequestsUtil.deleteContext(token, actualContextID);

        //Create challenge
        given()
                .body(FileUtils.getJsonFromFile("CHALLENGE_POST_ExpectedRegisterDTOBody.json"))
                .contentType(ContentType.JSON)
                .headers("Authorization", "Bearer " + token,
                        "content-type", ContentType.JSON,
                        "Accept", ContentType.JSON)
                .when()
                .post(baseURI+":"+port+basePath+"auth/challenges/" + actualContextID)
                .then()
                .assertThat().statusCode(404);

        USER_RequestsUtil.deleteUser(token);

    }

    @Test
    public void insertChallengeByNonNumericID_ShouldReturn400Test() throws Exception {

        USER_RequestsUtil.postUser("USER_POST_ExpectedRegisterDTOBody.json");
        String token = USER_RequestsUtil.authenticateUser("USER_POST_ExpectedRegisterDTOBody.json");
        Response contextDTOResponse = CONTEXT_RequestsUtil.postContext(token, "CONTEXT_POST_ExpectedRegisterDTOBody.json");

        JSONObject contextDTOJSONActual = new JSONObject(contextDTOResponse.getBody().prettyPrint());
        String actualContextID = contextDTOJSONActual.getString("id");

        //Create challenge
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

        USER_RequestsUtil.deleteUser(token);
        CONTEXT_RequestsUtil.deleteContext(token, actualContextID);

    }

    @Test
    public void insertChallengeMissingToken_ShouldReturn400Test() throws Exception {

        USER_RequestsUtil.postUser("USER_POST_ExpectedRegisterDTOBody.json");
        String token = USER_RequestsUtil.authenticateUser("USER_POST_ExpectedRegisterDTOBody.json");
        Response contextDTOResponse = CONTEXT_RequestsUtil.postContext(token, "CONTEXT_POST_ExpectedRegisterDTOBody.json");

        JSONObject contextDTOJSONActual = new JSONObject(contextDTOResponse.getBody().prettyPrint());
        String actualContextID = contextDTOJSONActual.getString("id");

        //Create challenge
        given()
                .body(FileUtils.getJsonFromFile("CHALLENGE_POST_ExpectedRegisterDTOBody.json"))
                .contentType(ContentType.JSON)
                .when()
                .post(baseURI+":"+port+basePath+"auth/challenges/" + actualContextID)
                .then()
                .assertThat().statusCode(400);

        USER_RequestsUtil.deleteUser(token);
        CONTEXT_RequestsUtil.deleteContext(token, actualContextID);

    }

    @Test
    public void insertChallengeByCreatorTokenLinkedToMissingCreator_ShouldReturn400Test() throws Exception {

        USER_RequestsUtil.postUser("USER_POST_ExpectedRegisterDTOBody.json");
        String token = USER_RequestsUtil.authenticateUser("USER_POST_ExpectedRegisterDTOBody.json");
        Response contextDTOResponse = CONTEXT_RequestsUtil.postContext(token, "CONTEXT_POST_ExpectedRegisterDTOBody.json");

        JSONObject contextDTOJSONActual = new JSONObject(contextDTOResponse.getBody().prettyPrint());
        String actualContextID = contextDTOJSONActual.getString("id");

        USER_RequestsUtil.deleteUser(token);

        //Create challenge
        given()
                .body(FileUtils.getJsonFromFile("CHALLENGE_POST_ExpectedRegisterDTOBody.json"))
                .contentType(ContentType.JSON)
                .headers("Authorization", "Bearer " + token,
                        "content-type", ContentType.JSON,
                        "Accept", ContentType.JSON)
                .when()
                .post(baseURI+":"+port+basePath+"auth/challenges/" + actualContextID)
                .then()
                .assertThat().statusCode(404);

        CONTEXT_RequestsUtil.deleteContext(token, actualContextID);

    }

    @Test
    public void insertChallengeByMalformedOrExpiredToken_ShouldReturn500Test() throws Exception {

        USER_RequestsUtil.postUser("USER_POST_ExpectedRegisterDTOBody.json");
        String token = USER_RequestsUtil.authenticateUser("USER_POST_ExpectedRegisterDTOBody.json");
        Response contextDTOResponse = CONTEXT_RequestsUtil.postContext(token, "CONTEXT_POST_ExpectedRegisterDTOBody.json");

        JSONObject contextDTOJSONActual = new JSONObject(contextDTOResponse.getBody().prettyPrint());
        String actualContextID = contextDTOJSONActual.getString("id");

        //Create challenge
        given()
                .body(FileUtils.getJsonFromFile("CHALLENGE_POST_ExpectedRegisterDTOBody.json"))
                .contentType(ContentType.JSON)
                .headers("Authorization", "Bearer " + invalidToken,
                        "content-type", ContentType.JSON,
                        "Accept", ContentType.JSON)
                .when()
                .post(baseURI+":"+port+basePath+"auth/challenges/" + actualContextID)
                .then()
                .assertThat().statusCode(500);

        USER_RequestsUtil.deleteUser(token);
        CONTEXT_RequestsUtil.deleteContext(token, actualContextID);

    }


    @Test
    public void findChallengesByCreatorToken_ShouldReturn200Test() throws Exception {

        USER_RequestsUtil.postUser("USER_POST_ExpectedRegisterDTOBody.json");
        String token = USER_RequestsUtil.authenticateUser("USER_POST_ExpectedRegisterDTOBody.json");
        Response contextDTOResponse = CONTEXT_RequestsUtil.postContext(token, "CONTEXT_POST_ExpectedRegisterDTOBody.json");

        JSONObject contextDTOJSONActual = new JSONObject(contextDTOResponse.getBody().prettyPrint());
        String actualContextID = contextDTOJSONActual.getString("id");

        Response challengeDTOResponse = CHALLENGE_RequestsUtil.postChallenge(
                token, "CHALLENGE_POST_ExpectedRegisterDTOBody.json", actualContextID);

        //Find challenge
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

        JSONObject challengeDTOJSON = new JSONObject(challengeDTOResponse.getBody().prettyPrint());
        String actualChallengeID = challengeDTOJSON.getString("id");

        USER_RequestsUtil.deleteUser(token);
        CONTEXT_RequestsUtil.deleteContext(token, actualContextID);
        CHALLENGE_RequestsUtil.deleteChallenge(token, actualChallengeID);

    }

    @Test
    public void findChallengesByMissingToken_ShouldReturn400(){



    }

    @Test
    public void findChallengesByCreatorTokenLinkedToMissingCreator_ShouldReturn400(){



    }

    @Test
    public void findChallengesByMalformedOrExpiredToken_ShouldReturn400(){



    }
}

//ISSUES
// i1: POST Desafios com palavras iguais são adicionados normalmente (word UNIQUE)
// i2: GET por QUERY não está buscando nada, sugestão: buscar por word
// i3: SWAGGER challenge-resource/get por query informação "Authorization" está faltando
// i4: PUT desafio com word já existente(200 OK) não altera nada e retorna o mesmo desafio(body) do input. Sugestão: Deveria ser tratado

//TODO's
// ToDo: FAZER A VERIFICAÇÃO DE QUANTIDADE DE CARACTERES DE CONTEXTS
// ToDo: FAZER UMA TABELA DAS FUNÇÕES E O QUE FORAM TESTADAS NELAS em x e y(BATALHA NAVAL)
// ToDo: FORMALIZAR "ByNoID", "ByNoName" pra "MissingID", "MissingName"
// ToDo: FORMALIZAR NOMES DE TESTES "ByToken" por "ByCreatorToken"
// ToDo: Drop tables between tests
