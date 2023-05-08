package br.ufpb.dcx.apps4society.educapi.resources;

import br.ufpb.dcx.apps4society.educapi.EducApiApplicationTests;
import br.ufpb.dcx.apps4society.educapi.dto.context.ContextDTO;
import br.ufpb.dcx.apps4society.educapi.unit.domain.builder.ContextBuilder;
import br.ufpb.dcx.apps4society.educapi.utils.CONTEXT_RequestsUtil;
import br.ufpb.dcx.apps4society.educapi.utils.FileUtils;
import br.ufpb.dcx.apps4society.educapi.utils.USER_RequestsUtil;
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

public class ContextResourceIntegrationTest extends EducApiApplicationTests {

    @Test
    public void insertContextWithTokenNameImageURLSoundURLVideoURL_ShouldReturn201Test() throws Exception {

        USER_RequestsUtil.postUser("USER_POST_ExpectedRegisterDTOBody.json");

        String token = USER_RequestsUtil.authenticateUser("USER_POST_ExpectedRegisterDTOBody.json");

        //Create Context
        Response contextDTOResponse = given()
                .body(FileUtils.getJsonFromFile("CONTEXT_POST_ExpectedRegisterDTOBody.json"))
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
        JSONObject contextJSONExpected = new JSONObject(FileUtils.getJsonFromFile("CONTEXT_POST_ExpectedRegisterDTOBody.json"));

        Assertions.assertNotNull(contextDTOJSONActual.getString("id"));
        Assertions.assertEquals(contextJSONExpected.getString("imageUrl"), contextDTOJSONActual.getString("imageUrl"));
        Assertions.assertEquals(contextJSONExpected.getString("name"), contextDTOJSONActual.getString("name"));
        Assertions.assertEquals(contextJSONExpected.getString("soundUrl"), contextDTOJSONActual.getString("soundUrl"));
        Assertions.assertEquals(contextJSONExpected.getString("videoUrl"), contextDTOJSONActual.getString("videoUrl"));

        CONTEXT_RequestsUtil.deleteContext(token, actualContextID);
        USER_RequestsUtil.deleteUser(token);

    }

    @Test
    public void insertContextWithTokenImageURLSoundURLVideoURLButNameAlreadyExists_ShouldReturn403Test() throws Exception {

        USER_RequestsUtil.postUser("USER_POST_ExpectedRegisterDTOBody.json");

        String token = USER_RequestsUtil.authenticateUser("USER_POST_ExpectedRegisterDTOBody.json");

        Response contextDTOResponse = CONTEXT_RequestsUtil.postContext(token, "CONTEXT_POST_ExpectedRegisterDTOBody.json");

        given()
                .body(FileUtils.getJsonFromFile("CONTEXT_POST_ExpectedRegisterDTOBody.json"))
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

//        JSONObject contextDTOJSONActual = new JSONObject(contextDTOResponse.getBody().prettyPrint());
//        String actualContextID = contextDTOJSONActual.getString("id");
//
//        CONTEXT_RequestUtil.deleteContext(token, actualContextID);
        USER_RequestsUtil.deleteUser(token);
    }

    @Test
    public void insertContextWithTokenImageURLSoundURLVideoURLWithoutName_ShouldReturn400Test() throws Exception {

        USER_RequestsUtil.postUser("USER_POST_ExpectedRegisterDTOBody.json");

        String token = USER_RequestsUtil.authenticateUser("USER_POST_ExpectedRegisterDTOBody.json");

        given()
                .body(FileUtils.getJsonFromFile("CONTEXT_POST_MissingNameExpectedRegisterDTOBody.json"))
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

        USER_RequestsUtil.deleteUser(token);
    }

    @Test
    public void insertContextWithNameImageURLSoundURLVideoURLWithOutToken_ShouldReturn400Test() throws Exception {

        given()
                .body(FileUtils.getJsonFromFile("CONTEXT_POST_ExpectedRegisterDTOBody.json"))
                .contentType(ContentType.JSON)

                .when()
                .post(baseURI+":"+port+basePath+"auth/contexts")
                .then()
                .assertThat().statusCode(400);

    }

    @Test
    public void insertContextWithTokenNameImageURLSoundURLVideoURLButInexistentUser_ShouldReturn404Test() throws Exception {

        USER_RequestsUtil.postUser("USER_POST_ExpectedRegisterDTOBody.json");

        String token = USER_RequestsUtil.authenticateUser("USER_POST_ExpectedRegisterDTOBody.json");

        USER_RequestsUtil.deleteUser(token);

        given()
                .body(FileUtils.getJsonFromFile("CONTEXT_POST_ExpectedRegisterDTOBody.json"))
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
    public void insertContextWithMalformedOrExpiredToken_ShouldReturn500Test() throws Exception {

        //Create Context
        given()
                .body(FileUtils.getJsonFromFile("CONTEXT_POST_ExpectedRegisterDTOBody.json"))
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
    public void findContextsByToken_ShouldReturn201Test() throws Exception {

        USER_RequestsUtil.postUser("USER_POST_ExpectedRegisterDTOBody.json");
        String token = USER_RequestsUtil.authenticateUser("USER_POST_ExpectedRegisterDTOBody.json");
        CONTEXT_RequestsUtil.postContext(token, "CONTEXT_POST_ExpectedRegisterDTOBody.json");

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

        USER_RequestsUtil.deleteUser(token);

    }

    @Test
    public void findContextsByTokenWithInexistentUser_ShouldReturn404Test() throws Exception {

        USER_RequestsUtil.postUser("USER_POST_ExpectedRegisterDTOBody.json");
        String token = USER_RequestsUtil.authenticateUser("USER_POST_ExpectedRegisterDTOBody.json");
        USER_RequestsUtil.deleteUser(token);

        //Get Contexts
        given()
                .body(FileUtils.getJsonFromFile("CONTEXT_POST_ExpectedRegisterDTOBody.json"))
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
    public void findContextsByMalformedOrExpiredToken_ShouldReturn500Test() throws Exception {

        //Get Contexts
        given()
                .body(FileUtils.getJsonFromFile("CONTEXT_POST_ExpectedRegisterDTOBody.json"))
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
    public void findContextByID_ShouldReturn200Test() throws Exception {

        USER_RequestsUtil.postUser("USER_POST_ExpectedRegisterDTOBody.json");
        String token = USER_RequestsUtil.authenticateUser("USER_POST_ExpectedRegisterDTOBody.json");
        Response contextDTOResponse = CONTEXT_RequestsUtil.postContext(token, "CONTEXT_POST_ExpectedRegisterDTOBody.json");

        JSONObject contextDTOJSONActual = new JSONObject(contextDTOResponse.getBody().prettyPrint());
        String actualContextID = contextDTOJSONActual.getString("id");

        given()
                .body(FileUtils.getJsonFromFile("CONTEXT_POST_ExpectedRegisterDTOBody.json"))
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

        USER_RequestsUtil.deleteUser(token);

    }

    @Test
    public void findContextByNonID_ShouldReturn200Test() throws Exception {

        USER_RequestsUtil.postUser("USER_POST_ExpectedRegisterDTOBody.json");
        String token = USER_RequestsUtil.authenticateUser("USER_POST_ExpectedRegisterDTOBody.json");

        //Get Context
        given()
                .body(FileUtils.getJsonFromFile("CONTEXT_POST_ExpectedRegisterDTOBody.json"))
                .contentType(ContentType.JSON)
                .headers("Authorization",
                        "Bearer " + token,
                        "Content-Type",
                        ContentType.JSON,
                        "Accept",
                        ContentType.JSON)
                .when()
                .get(baseURI+":"+port+basePath+"contexts/")
                .then()
                .assertThat().statusCode(200);

        USER_RequestsUtil.deleteUser(token);

    }

    @Test
    public void findContextByInexistentID_ShouldReturn404Test() throws Exception {

        USER_RequestsUtil.postUser("USER_POST_ExpectedRegisterDTOBody.json");
        String token = USER_RequestsUtil.authenticateUser("USER_POST_ExpectedRegisterDTOBody.json");
        Response contextDTOResponse = CONTEXT_RequestsUtil.postContext(token, "CONTEXT_POST_ExpectedRegisterDTOBody.json");

        JSONObject contextDTOJSONActual = new JSONObject(contextDTOResponse.getBody().prettyPrint());
        String actualContextID = contextDTOJSONActual.getString("id");

        CONTEXT_RequestsUtil.deleteContext(token, actualContextID);

        //Get Context
        given()
                .body(FileUtils.getJsonFromFile("CONTEXT_POST_ExpectedRegisterDTOBody.json"))
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

        USER_RequestsUtil.deleteUser(token);

    }

    @Test
    public void findContextByOwnerEmailQuery_ShouldReturn200Test() throws Exception {

        Response userDTOResponse = USER_RequestsUtil.postUser("USER_POST_ExpectedRegisterDTOBody.json");
        String token = USER_RequestsUtil.authenticateUser("USER_POST_ExpectedRegisterDTOBody.json");
        CONTEXT_RequestsUtil.postContext(token, "CONTEXT_POST_ExpectedRegisterDTOBody.json");

        JSONObject userDTOJSONActual = new JSONObject(userDTOResponse.getBody().prettyPrint());
        String actualUserEmail = userDTOJSONActual.getString("email");

        //Get Contexts
        given()
                .body(FileUtils.getJsonFromFile("CONTEXT_POST_ExpectedRegisterDTOBody.json"))
                .contentType(ContentType.JSON)
                .headers("Authorization",
                        "Bearer " + token,
                        "Content-Type",
                        ContentType.JSON,
                        "Accept",
                        ContentType.JSON)
                .when()
                .get(baseURI+":"+port+basePath+"contexts/" + "?email=" + actualUserEmail + "&page=0&size=20")
                .then()
                .assertThat().statusCode(200);

        USER_RequestsUtil.deleteUser(token);

    }

    @Test
    public void findContextByContextNameQuery_ShouldReturn200Test() throws Exception {

        USER_RequestsUtil.postUser("USER_POST_ExpectedRegisterDTOBody.json");
        String token = USER_RequestsUtil.authenticateUser("USER_POST_ExpectedRegisterDTOBody.json");
        Response contextDTOResponse = CONTEXT_RequestsUtil.postContext(token, "CONTEXT_POST_ExpectedRegisterDTOBody.json");

        JSONObject contextDTOJSONActual = new JSONObject(contextDTOResponse.getBody().prettyPrint());
        String actualContextName = contextDTOJSONActual.getString("name");

        //Get Contexts
        given()
                .body(FileUtils.getJsonFromFile("CONTEXT_POST_ExpectedRegisterDTOBody.json"))
                .contentType(ContentType.JSON)
                .headers("Authorization",
                        "Bearer " + token,
                        "Content-Type",
                        ContentType.JSON,
                        "Accept",
                        ContentType.JSON)
                .when()
                .get(baseURI+":"+port+basePath+"contexts/" + "?name=" + actualContextName + "&page=0&size=20")
                .then()
                .assertThat().statusCode(200);

        USER_RequestsUtil.deleteUser(token);

    }

    @Test
    public void findContextsByNonNamedQuery_ShouldReturn200Test() throws Exception {

        USER_RequestsUtil.postUser("USER_POST_ExpectedRegisterDTOBody.json");
        String token = USER_RequestsUtil.authenticateUser("USER_POST_ExpectedRegisterDTOBody.json");

        //Get All Contexts
        given()
                .body(FileUtils.getJsonFromFile("CONTEXT_POST_ExpectedRegisterDTOBody.json"))
                .contentType(ContentType.JSON)
                .headers("Authorization",
                        "Bearer " + token,
                        "Content-Type",
                        ContentType.JSON,
                        "Accept",
                        ContentType.JSON)
                .when()
                .get(baseURI+":"+port+basePath+"contexts/" + "?name=" + null + "&page=0&size=20")
                .then()
                .assertThat().statusCode(200);

        USER_RequestsUtil.deleteUser(token);

    }

    @Test
    public void findContextsByNonEmailQuery_ShouldReturn200Test() throws Exception {

        USER_RequestsUtil.postUser("USER_POST_ExpectedRegisterDTOBody.json");
        String token = USER_RequestsUtil.authenticateUser("USER_POST_ExpectedRegisterDTOBody.json");

        //Get Contexts
        given()
                .body(FileUtils.getJsonFromFile("CONTEXT_POST_ExpectedRegisterDTOBody.json"))
                .contentType(ContentType.JSON)
                .headers("Authorization",
                        "Bearer " + token,
                        "Content-Type",
                        ContentType.JSON,
                        "Accept",
                        ContentType.JSON)
                .when()
                .get(baseURI+":"+port+basePath+"contexts/" + "?email=" + null + "&page=0&size=20")
                .then()
                .assertThat().statusCode(200);

        USER_RequestsUtil.deleteUser(token);

    }

    @Test
    public void updateContextByTokenIDNameImageURLSoundURLVideoURL_ShouldReturn200Test() throws Exception {

        USER_RequestsUtil.postUser("USER_POST_ExpectedRegisterDTOBody.json");
        String token = USER_RequestsUtil.authenticateUser("USER_POST_ExpectedRegisterDTOBody.json");
        Response contextDTOResponse = CONTEXT_RequestsUtil.postContext(token, "CONTEXT_POST_ExpectedRegisterDTOBody.json");

        JSONObject contextDTOJSON = new JSONObject(contextDTOResponse.getBody().prettyPrint());
        String contextID = contextDTOJSON.getString("id");

        //Update Context
        Response contextDTOResponseUpdated = given()
                .body(FileUtils.getJsonFromFile("CONTEXT_PUT_ExpectedRegisterDTOBody.json"))
                .contentType(ContentType.JSON)
                .headers("Authorization",
                        "Bearer " + token,
                        "Content-Type",
                        ContentType.JSON,
                        "Accept",
                        ContentType.JSON)
                .when()
                .put(baseURI+":"+port+basePath+"auth/contexts/" + contextID)
                .then()
                .assertThat().statusCode(200)
                .extract().response();

        JSONObject contextDTOJSONUpdated = new JSONObject(contextDTOResponseUpdated.getBody().prettyPrint());

        String contextIDUpdated = contextDTOJSONUpdated.getString("id");
        String contextImageURLUpdated = contextDTOJSONUpdated.getString("imageUrl");
        String contextNameUpdated = contextDTOJSONUpdated.getString("name");
        String contextSoundURLUpdated = contextDTOJSONUpdated.getString("soundUrl");
        String contextVideoURLUpdated = contextDTOJSONUpdated.getString("videoUrl");

        ContextDTO contextDTOUpdated = ContextBuilder.anContext()
                .withId(Long.valueOf(contextIDUpdated))
                .withImage(contextImageURLUpdated)
                .withName(contextNameUpdated)
                .withSound(contextSoundURLUpdated)
                .withVideo(contextVideoURLUpdated).buildContextDTO();

        ObjectMapper mapper = new ObjectMapper();
        mapper.writeValue(new File("src/test/resources/CONTEXT_PUT_ContextDTOBody[spawned].json"), contextDTOUpdated);

        Assertions.assertNotNull(contextDTOJSONUpdated.getString("id"));
        Assertions.assertEquals(contextDTOJSON.getString("imageUrl"), contextDTOJSONUpdated.getString("imageUrl"));
        Assertions.assertNotEquals(contextDTOJSON.getString("name"), contextDTOJSONUpdated.getString("name"));
        Assertions.assertEquals(contextDTOJSON.getString("soundUrl"), contextDTOJSONUpdated.getString("soundUrl"));
        Assertions.assertEquals(contextDTOJSON.getString("videoUrl"), contextDTOJSONUpdated.getString("videoUrl"));

        CONTEXT_RequestsUtil.deleteContext(token, contextIDUpdated);
        USER_RequestsUtil.deleteUser(token);

    }

    @Test
    public void updateContextByTokenImageURLSoundURLVideoURLButSameName_ShouldReturn200Test() throws Exception {

        USER_RequestsUtil.postUser("USER_POST_ExpectedRegisterDTOBody.json");
        String token = USER_RequestsUtil.authenticateUser("USER_POST_ExpectedRegisterDTOBody.json");
        Response contextDTOResponse = CONTEXT_RequestsUtil.postContext(token, "CONTEXT_POST_ExpectedRegisterDTOBody.json");

        JSONObject contextDTOJSON = new JSONObject(contextDTOResponse.getBody().prettyPrint());
        String contextID = contextDTOJSON.getString("id");

        //Update Context
        Response contextDTOResponseUpdated = given()
                .body(FileUtils.getJsonFromFile("CONTEXT_POST_ExpectedRegisterDTOBody.json"))
                .contentType(ContentType.JSON)
                .headers("Authorization",
                        "Bearer " + token,
                        "Content-Type",
                        ContentType.JSON,
                        "Accept",
                        ContentType.JSON)
                .when()
                .put(baseURI+":"+port+basePath+"auth/contexts/" + contextID)
                .then()
                .assertThat().statusCode(200)
                .extract().response();

        JSONObject contextDTOJSONUpdated = new JSONObject(contextDTOResponseUpdated.getBody().prettyPrint());

        String contextIDUpdated = contextDTOJSONUpdated.getString("id");
        String contextImageURLUpdated = contextDTOJSONUpdated.getString("imageUrl");
        String contextNameUpdated = contextDTOJSONUpdated.getString("name");
        String contextSoundURLUpdated = contextDTOJSONUpdated.getString("soundUrl");
        String contextVideoURLUpdated = contextDTOJSONUpdated.getString("videoUrl");

        ContextDTO contextDTOUpdated = ContextBuilder.anContext()
                .withId(Long.valueOf(contextIDUpdated))
                .withImage(contextImageURLUpdated)
                .withName(contextNameUpdated)
                .withSound(contextSoundURLUpdated)
                .withVideo(contextVideoURLUpdated).buildContextDTO();

        ObjectMapper mapper = new ObjectMapper();
        mapper.writeValue(new File("src/test/resources/CONTEXT_PUT_ContextDTOBody[spawned].json"), contextDTOUpdated);

        Assertions.assertNotNull(contextDTOJSONUpdated.getString("id"));
        Assertions.assertEquals(contextDTOJSON.getString("imageUrl"), contextDTOJSONUpdated.getString("imageUrl"));
        Assertions.assertEquals(contextDTOJSON.getString("name"), contextDTOJSONUpdated.getString("name"));
        Assertions.assertEquals(contextDTOJSON.getString("soundUrl"), contextDTOJSONUpdated.getString("soundUrl"));
        Assertions.assertEquals(contextDTOJSON.getString("videoUrl"), contextDTOJSONUpdated.getString("videoUrl"));

        CONTEXT_RequestsUtil.deleteContext(token, contextIDUpdated);
        USER_RequestsUtil.deleteUser(token);

    }

    @Test
    public void updateContextByNonName_ShouldReturn400Test() throws Exception {

        USER_RequestsUtil.postUser("USER_POST_ExpectedRegisterDTOBody.json");
        String token = USER_RequestsUtil.authenticateUser("USER_POST_ExpectedRegisterDTOBody.json");
        Response contextDTOResponse = CONTEXT_RequestsUtil.postContext(token, "CONTEXT_POST_ExpectedRegisterDTOBody.json");

        JSONObject contextDTOJSON = new JSONObject(contextDTOResponse.getBody().prettyPrint());
        String contextID = contextDTOJSON.getString("id");

        //Update Context
        Response contextDTOResponseUpdated = given()
                .body(FileUtils.getJsonFromFile("CONTEXT_POST_MissingNameExpectedRegisterDTOBody.json"))
                .contentType(ContentType.JSON)
                .headers("Authorization",
                        "Bearer " + token,
                        "Content-Type",
                        ContentType.JSON,
                        "Accept",
                        ContentType.JSON)
                .when()
                .put(baseURI+":"+port+basePath+"auth/contexts/" + contextID)
                .then()
                .assertThat().statusCode(400)
                .extract().response();

        CONTEXT_RequestsUtil.deleteContext(token, contextID);
        USER_RequestsUtil.deleteUser(token);

    }

    @Test
    public void updateContextByNonID_ShouldReturn405Test() throws Exception {

        USER_RequestsUtil.postUser("USER_POST_ExpectedRegisterDTOBody.json");
        String token = USER_RequestsUtil.authenticateUser("USER_POST_ExpectedRegisterDTOBody.json");

        //Update Context
        given()
                .body(FileUtils.getJsonFromFile("CONTEXT_POST_ExpectedRegisterDTOBody.json"))
                .contentType(ContentType.JSON)
                .headers("Authorization",
                        "Bearer " + token,
                        "Content-Type",
                        ContentType.JSON,
                        "Accept",
                        ContentType.JSON)
                .when()
                .put(baseURI+":"+port+basePath+"auth/contexts")
                .then()
                .assertThat().statusCode(405);

        USER_RequestsUtil.deleteUser(token);

    }

    @Test
    public void updateInexistentContextByID_ShouldReturn404Test() throws Exception {

        USER_RequestsUtil.postUser("USER_POST_ExpectedRegisterDTOBody.json");
        String token = USER_RequestsUtil.authenticateUser("USER_POST_ExpectedRegisterDTOBody.json");
        Response contextDTOResponse = CONTEXT_RequestsUtil.postContext(token, "CONTEXT_POST_ExpectedRegisterDTOBody.json");

        JSONObject contextDTOJSON = new JSONObject(contextDTOResponse.getBody().prettyPrint());
        String contextID = contextDTOJSON.getString("id");

        CONTEXT_RequestsUtil.deleteContext(token, contextID);

        //Update Context
        given()
                .body(FileUtils.getJsonFromFile("CONTEXT_POST_ExpectedRegisterDTOBody.json"))
                .contentType(ContentType.JSON)
                .headers("Authorization",
                        "Bearer " + token,
                        "Content-Type",
                        ContentType.JSON,
                        "Accept",
                        ContentType.JSON)
                .when()
                .put(baseURI+":"+port+basePath+"auth/contexts/" + contextID)
                .then()
                .assertThat().statusCode(404);

        USER_RequestsUtil.deleteUser(token);

    }

    @Test
    public void updateContextByNonNumericID_ShouldReturn400Test() throws Exception {

        USER_RequestsUtil.postUser("USER_POST_ExpectedRegisterDTOBody.json");
        String token = USER_RequestsUtil.authenticateUser("USER_POST_ExpectedRegisterDTOBody.json");

        String contextID = "ID Non Numeric";

        //Update Context
        Response contextDTOResponseUpdated = given()
                .body(FileUtils.getJsonFromFile("CONTEXT_POST_ExpectedRegisterDTOBody.json"))
                .contentType(ContentType.JSON)
                .headers("Authorization",
                        "Bearer " + token,
                        "Content-Type",
                        ContentType.JSON,
                        "Accept",
                        ContentType.JSON)
                .when()
                .put(baseURI+":"+port+basePath+"auth/contexts/" + contextID)
                .then()
                .assertThat().statusCode(400)
                .extract().response();

        USER_RequestsUtil.deleteUser(token);

    }

    @Test
    public void updateContextButAnyToken_ShouldReturn400Test() throws Exception {

        USER_RequestsUtil.postUser("USER_POST_ExpectedRegisterDTOBody.json");
        String token = USER_RequestsUtil.authenticateUser("USER_POST_ExpectedRegisterDTOBody.json");
        Response contextDTOResponse = CONTEXT_RequestsUtil.postContext(token, "CONTEXT_POST_ExpectedRegisterDTOBody.json");

        JSONObject contextDTOJSON = new JSONObject(contextDTOResponse.getBody().prettyPrint());
        String contextID = contextDTOJSON.getString("id");

        //Update Context
        given()
                .body(FileUtils.getJsonFromFile("CONTEXT_POST_ExpectedRegisterDTOBody.json"))
                .contentType(ContentType.JSON)
                .when()
                .put(baseURI+":"+port+basePath+"auth/contexts/" + contextID)
                .then()
                .assertThat().statusCode(400);

        CONTEXT_RequestsUtil.deleteContext(token, contextID);
        USER_RequestsUtil.deleteUser(token);

    }

    @Test
    public void updateContextButExpiredOrMalformedToken_ShouldReturn500Test() throws Exception {

        USER_RequestsUtil.postUser("USER_POST_ExpectedRegisterDTOBody.json");
        String token = USER_RequestsUtil.authenticateUser("USER_POST_ExpectedRegisterDTOBody.json");
        Response contextDTOResponse = CONTEXT_RequestsUtil.postContext(token, "CONTEXT_POST_ExpectedRegisterDTOBody.json");

        JSONObject contextDTOJSON = new JSONObject(contextDTOResponse.getBody().prettyPrint());
        String contextID = contextDTOJSON.getString("id");

        //Update Context
        given()
                .body(FileUtils.getJsonFromFile("CONTEXT_POST_ExpectedRegisterDTOBody.json"))
                .contentType(ContentType.JSON)
                .headers("Authorization",
                        "Bearer " + invalidToken,
                        "Content-Type",
                        ContentType.JSON,
                        "Accept",
                        ContentType.JSON)
                .when()
                .put(baseURI+":"+port+basePath+"auth/contexts/" + contextID)
                .then()
                .assertThat().statusCode(500);

        CONTEXT_RequestsUtil.deleteContext(token, contextID);
        USER_RequestsUtil.deleteUser(token);

    }

    @Test
    public void deleteContextByTokenID_ShouldReturn200Test() throws Exception {

        USER_RequestsUtil.postUser("USER_POST_ExpectedRegisterDTOBody.json");
        String token = USER_RequestsUtil.authenticateUser("USER_POST_ExpectedRegisterDTOBody.json");
        Response contextDTOResponse = CONTEXT_RequestsUtil.postContext(token, "CONTEXT_POST_ExpectedRegisterDTOBody.json");

        JSONObject contextDTOJSON = new JSONObject(contextDTOResponse.getBody().prettyPrint());
        String contextID = contextDTOJSON.getString("id");

        //Delete Context
        given()
                .headers("Authorization",
                        "Bearer " + token,
                        "Content-Type",
                        ContentType.JSON,
                        "Accept",
                        ContentType.JSON)
                .when()
                .delete(baseURI+":"+port+basePath+"auth/contexts/" + contextID)
                .then().assertThat().statusCode(200);

        USER_RequestsUtil.deleteUser(token);

    }

    @Test
    public void deleteContextButAnyID_ShouldReturn405Test() throws Exception {

        USER_RequestsUtil.postUser("USER_POST_ExpectedRegisterDTOBody.json");
        String token = USER_RequestsUtil.authenticateUser("USER_POST_ExpectedRegisterDTOBody.json");

        //Delete Context
        given()
                .headers("Authorization",
                        "Bearer " + token,
                        "Content-Type",
                        ContentType.JSON,
                        "Accept",
                        ContentType.JSON)
                .when()
                .delete(baseURI+":"+port+basePath+"auth/contexts")
                .then().assertThat().statusCode(405);

        USER_RequestsUtil.deleteUser(token);

    }

    @Test
    public void deleteInexistentContextByID_ShouldReturn404Test() throws Exception {

        USER_RequestsUtil.postUser("USER_POST_ExpectedRegisterDTOBody.json");
        String token = USER_RequestsUtil.authenticateUser("USER_POST_ExpectedRegisterDTOBody.json");
        Response contextDTOResponse = CONTEXT_RequestsUtil.postContext(token, "CONTEXT_POST_ExpectedRegisterDTOBody.json");

        JSONObject contextDTOJSON = new JSONObject(contextDTOResponse.getBody().prettyPrint());
        String contextID = contextDTOJSON.getString("id");

        CONTEXT_RequestsUtil.deleteContext(token, contextID);

        //Delete Context
        given()
                .headers("Authorization",
                        "Bearer " + token,
                        "Content-Type",
                        ContentType.JSON,
                        "Accept",
                        ContentType.JSON)
                .when()
                .delete(baseURI+":"+port+basePath+"auth/contexts/" + contextID)
                .then().assertThat().statusCode(404);

        USER_RequestsUtil.deleteUser(token);

    }

    @Test
    public void deleteContextButPassAnyToken_ShouldReturn400Test() throws Exception {

        USER_RequestsUtil.postUser("USER_POST_ExpectedRegisterDTOBody.json");
        String token = USER_RequestsUtil.authenticateUser("USER_POST_ExpectedRegisterDTOBody.json");
        Response contextDTOResponse = CONTEXT_RequestsUtil.postContext(token, "CONTEXT_POST_ExpectedRegisterDTOBody.json");

        JSONObject contextDTOJSON = new JSONObject(contextDTOResponse.getBody().prettyPrint());
        String contextID = contextDTOJSON.getString("id");

        //Delete Context
        given()
                .when()
                .delete(baseURI+":"+port+basePath+"auth/contexts/" + contextID)
                .then().assertThat().statusCode(400);

        USER_RequestsUtil.deleteUser(token);

    }

    @Test
    public void deleteContextByExpiredOrMalformedToken_ShouldReturn500Test() throws Exception {

        USER_RequestsUtil.postUser("USER_POST_ExpectedRegisterDTOBody.json");
        String token = USER_RequestsUtil.authenticateUser("USER_POST_ExpectedRegisterDTOBody.json");
        Response contextDTOResponse = CONTEXT_RequestsUtil.postContext(token, "CONTEXT_POST_ExpectedRegisterDTOBody.json");

        JSONObject contextDTOJSON = new JSONObject(contextDTOResponse.getBody().prettyPrint());
        String contextID = contextDTOJSON.getString("id");

        //Delete Context
        given()
                .headers("Authorization",
                        "Bearer " + invalidToken,
                        "Content-Type",
                        ContentType.JSON,
                        "Accept",
                        ContentType.JSON)
                .when()
                .delete(baseURI+":"+port+basePath+"auth/contexts/" + contextID)
                .then().assertThat().statusCode(500);

        USER_RequestsUtil.deleteUser(token);

    }
}

//ISSUES:
// Tentar criar um contexto com mesmo nome retorna status code 403Forbidden
// Contextos sendo criados mesmo sem urls
// QUERY Busca de contextos por nome com nome vazio retorna todos os desafios e 200 OK
// QUERY Busca de contextos por email com email vazio retorna nada e 200 OK
// QUERY BUSCA SEM ID exibe todos os contextos
// Nomes estão passando com caracteres que não são letras(pode ser um problema principalmente para usuários)
// VALIDAÇÃO DE EMAIL NÃO ESTÁ IMPLEMENTADO EM PESQUISAS POR QUERY
// na documentação do educAPI no swagger | context-resource, put indica que é um possivel response "201 created" porém é um update não era pra ter created... aparentemente é so um erro de texto na propria documentação do swagger.
// Atualizar contextos inserindo o mesmo nome do contexto antigo ou com novo nome que não seja somente letras está passando, ou seja o processamento esta sendo realizado porem está trocando o mesmo objeto pelo menos objeto idêntico, talvez isso devesse ser tratado.

//    https://www.youtube.com/watch?v=Y4_LmPhx1Jc
//    https://www.youtube.com/watch?v=l5WfHfHvqo8
//    https://www.youtube.com/watch?v=3duamjhP7NM