package br.ufpb.dcx.apps4society.educapi.resources;

import br.ufpb.dcx.apps4society.educapi.EducApiApplicationTests;
import br.ufpb.dcx.apps4society.educapi.utils.CONTEXT_RequestsUtil;
import br.ufpb.dcx.apps4society.educapi.utils.FileUtils;
import br.ufpb.dcx.apps4society.educapi.utils.USER_RequestsUtil;
import io.restassured.http.ContentType;
import io.restassured.response.Response;
import org.json.JSONObject;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import static io.restassured.RestAssured.*;

public class ContextResourceIntegrationTest extends EducApiApplicationTests {

    @Test
    public void insertContextWithTokenNameImageURLSoundURLVideoURL_ShouldReturn201Test() throws Exception {

        USER_RequestsUtil.post("USER_POST_ExpectedRegisterDTOBody.json");

        String token = USER_RequestsUtil.authenticate("USER_POST_ExpectedRegisterDTOBody.json");

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

        JSONObject contextDTOAtual = new JSONObject(contextDTOResponse.getBody().prettyPrint());
        JSONObject contextRegisterDTOEsperado = new JSONObject(FileUtils.getJsonFromFile("CONTEXT_POST_ExpectedRegisterDTOBody.json"));

        Assertions.assertNotNull(contextDTOAtual.getString("id"));
        Assertions.assertEquals(contextRegisterDTOEsperado.getString("imageUrl"), contextDTOAtual.getString("imageUrl"));
        Assertions.assertEquals(contextRegisterDTOEsperado.getString("name"), contextDTOAtual.getString("name"));
        Assertions.assertEquals(contextRegisterDTOEsperado.getString("soundUrl"), contextDTOAtual.getString("soundUrl"));
        Assertions.assertEquals(contextRegisterDTOEsperado.getString("videoUrl"), contextDTOAtual.getString("videoUrl"));


        CONTEXT_RequestsUtil.delete(token, contextDTOAtual.getString("id"));
        USER_RequestsUtil.delete(token);
    }

    @Test
    public void insertContextWithTokenImageURLSoundURLVideoURLButNameAlreadyExists_ShouldReturn201Test() throws Exception {

        USER_RequestsUtil.post("USER_POST_ExpectedRegisterDTOBody.json");

        String token = USER_RequestsUtil.authenticate("USER_POST_ExpectedRegisterDTOBody.json");

        Response contextDTOResponse = CONTEXT_RequestsUtil.post(token, "CONTEXT_POST_ExpectedRegisterDTOBody.json");

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
                .assertThat().statusCode(201);

        JSONObject contextDTOAtual = new JSONObject(contextDTOResponse.getBody().prettyPrint());


        CONTEXT_RequestsUtil.delete(token, contextDTOAtual.getString("id"));
        USER_RequestsUtil.delete(token);
    }

    @Test
    public void insertContextWithTokenImageURLSoundURLVideoURLWithoutName_ShouldReturn400Test() throws Exception {

        USER_RequestsUtil.post("USER_POST_ExpectedRegisterDTOBody.json");

        String token = USER_RequestsUtil.authenticate("USER_POST_ExpectedRegisterDTOBody.json");

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

        USER_RequestsUtil.delete(token);
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

        USER_RequestsUtil.post("USER_POST_ExpectedRegisterDTOBody.json");

        String token = USER_RequestsUtil.authenticate("USER_POST_ExpectedRegisterDTOBody.json");

        USER_RequestsUtil.delete(token);

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
    public void findContextsByToken_ShouldReturn200Test() throws Exception {

        USER_RequestsUtil.post("USER_POST_ExpectedRegisterDTOBody.json");
        String token = USER_RequestsUtil.authenticate("USER_POST_ExpectedRegisterDTOBody.json");
        CONTEXT_RequestsUtil.post(token, "CONTEXT_POST_ExpectedRegisterDTOBody.json");

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

        USER_RequestsUtil.delete(token);
    }

    @Test
    public void findContextsByTokenWithInexistentUser_ShouldReturn404Test() throws Exception {

        USER_RequestsUtil.post("USER_POST_ExpectedRegisterDTOBody.json");
        String token = USER_RequestsUtil.authenticate("USER_POST_ExpectedRegisterDTOBody.json");
        USER_RequestsUtil.delete(token);

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

        USER_RequestsUtil.post("USER_POST_ExpectedRegisterDTOBody.json");
        String token = USER_RequestsUtil.authenticate("USER_POST_ExpectedRegisterDTOBody.json");
        Response contextDTOResponse = CONTEXT_RequestsUtil.post(token, "CONTEXT_POST_ExpectedRegisterDTOBody.json");

        JSONObject contextDTOAtual = new JSONObject(contextDTOResponse.getBody().prettyPrint());
        String actualContextID = contextDTOAtual.getString("id");

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


        USER_RequestsUtil.delete(token);
    }

    @Test
    public void findContextByNonID_ShouldReturn200Test() throws Exception {

        USER_RequestsUtil.post("USER_POST_ExpectedRegisterDTOBody.json");
        String token = USER_RequestsUtil.authenticate("USER_POST_ExpectedRegisterDTOBody.json");

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
                .get(baseURI+":"+port+basePath+"contexts")
                .then()
                .assertThat().statusCode(200);


        USER_RequestsUtil.delete(token);
    }

    @Test
    public void findContextByInexistentID_ShouldReturn404Test() throws Exception {

        USER_RequestsUtil.post("USER_POST_ExpectedRegisterDTOBody.json");
        String token = USER_RequestsUtil.authenticate("USER_POST_ExpectedRegisterDTOBody.json");
        Response contextDTOResponse = CONTEXT_RequestsUtil.post(token, "CONTEXT_POST_ExpectedRegisterDTOBody.json");

        JSONObject contextDTOAtual = new JSONObject(contextDTOResponse.getBody().prettyPrint());
        String actualContextID = contextDTOAtual.getString("id");

        CONTEXT_RequestsUtil.delete(token, actualContextID);

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


        USER_RequestsUtil.delete(token);
    }

    @Test
    public void findContextByOwnerEmailQuery_ShouldReturn200Test() throws Exception {

        Response userDTOResponse = USER_RequestsUtil.post("USER_POST_ExpectedRegisterDTOBody.json");
        String token = USER_RequestsUtil.authenticate("USER_POST_ExpectedRegisterDTOBody.json");
        CONTEXT_RequestsUtil.post(token, "CONTEXT_POST_ExpectedRegisterDTOBody.json");

        JSONObject userDTOJSONActual = new JSONObject(userDTOResponse.getBody().prettyPrint());
        String actualUserEmail = userDTOJSONActual.getString("email");

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
                .get(baseURI+":"+port+basePath+"contexts" + "?email=" + actualUserEmail + "&page=0&size=20")
                .then()
                .assertThat().statusCode(200);


        USER_RequestsUtil.delete(token);
    }

    @Test
    public void findContextByContextNameQuery_ShouldReturn200Test() throws Exception {

        USER_RequestsUtil.post("USER_POST_ExpectedRegisterDTOBody.json");
        String token = USER_RequestsUtil.authenticate("USER_POST_ExpectedRegisterDTOBody.json");
        Response contextDTOResponse = CONTEXT_RequestsUtil.post(token, "CONTEXT_POST_ExpectedRegisterDTOBody.json");

        JSONObject contextDTOAtual = new JSONObject(contextDTOResponse.getBody().prettyPrint());
        String actualContextName = contextDTOAtual.getString("name");

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
                .get(baseURI+":"+port+basePath+"contexts" + "?name=" + actualContextName + "&page=0&size=20")
                .then()
                .assertThat().statusCode(200);

        USER_RequestsUtil.delete(token);
    }

    @Test
    public void findContextsByNonNamedQuery_ShouldReturn200Test() throws Exception {

        USER_RequestsUtil.post("USER_POST_ExpectedRegisterDTOBody.json");
        String token = USER_RequestsUtil.authenticate("USER_POST_ExpectedRegisterDTOBody.json");

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
                .get(baseURI+":"+port+basePath+"contexts" + "?name=" + "&page=0&size=20")
                .then()
                .assertThat().statusCode(200);


        USER_RequestsUtil.delete(token);
    }

    @Test
    public void findContextsByNonEmailQuery_ShouldReturn200Test() throws Exception {

        USER_RequestsUtil.post("USER_POST_ExpectedRegisterDTOBody.json");
        String token = USER_RequestsUtil.authenticate("USER_POST_ExpectedRegisterDTOBody.json");

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
                .get(baseURI+":"+port+basePath+"contexts" + "?email=" + null + "&page=0&size=20")
                .then()
                .assertThat().statusCode(200);


        USER_RequestsUtil.delete(token);
    }


    @Test
    public void updateContextByTokenIDNameImageURLSoundURLVideoURL_ShouldReturn200Test() throws Exception {

        USER_RequestsUtil.post("USER_POST_ExpectedRegisterDTOBody.json");
        String token = USER_RequestsUtil.authenticate("USER_POST_ExpectedRegisterDTOBody.json");
        Response contextDTOResponse = CONTEXT_RequestsUtil.post(token, "CONTEXT_POST_ExpectedRegisterDTOBody.json");

        JSONObject contextDTOJSON = new JSONObject(contextDTOResponse.getBody().prettyPrint());
        String contextID = contextDTOJSON.getString("id");

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

        Assertions.assertNotNull(contextDTOJSONUpdated.getString("id"));
        Assertions.assertEquals(contextDTOJSON.getString("imageUrl"), contextDTOJSONUpdated.getString("imageUrl"));
        Assertions.assertNotEquals(contextDTOJSON.getString("name"), contextDTOJSONUpdated.getString("name"));
        Assertions.assertEquals(contextDTOJSON.getString("soundUrl"), contextDTOJSONUpdated.getString("soundUrl"));
        Assertions.assertEquals(contextDTOJSON.getString("videoUrl"), contextDTOJSONUpdated.getString("videoUrl"));


        CONTEXT_RequestsUtil.delete(token, contextDTOJSONUpdated.getString("id"));
        USER_RequestsUtil.delete(token);
    }

    @Test
    public void updateContextByNonName_ShouldReturn400Test() throws Exception {

        USER_RequestsUtil.post("USER_POST_ExpectedRegisterDTOBody.json");
        String token = USER_RequestsUtil.authenticate("USER_POST_ExpectedRegisterDTOBody.json");
        Response contextDTOResponse = CONTEXT_RequestsUtil.post(token, "CONTEXT_POST_ExpectedRegisterDTOBody.json");

        JSONObject contextDTOJSON = new JSONObject(contextDTOResponse.getBody().prettyPrint());
        String contextID = contextDTOJSON.getString("id");

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
                .put(baseURI+":"+port+basePath+"auth/contexts/" + contextID)
                .then()
                .assertThat().statusCode(400)
                .extract().response();


        CONTEXT_RequestsUtil.delete(token, contextID);
        USER_RequestsUtil.delete(token);
    }

    @Test
    public void updateContextByNonID_ShouldReturn405Test() throws Exception {

        USER_RequestsUtil.post("USER_POST_ExpectedRegisterDTOBody.json");
        String token = USER_RequestsUtil.authenticate("USER_POST_ExpectedRegisterDTOBody.json");

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


        USER_RequestsUtil.delete(token);
    }

    @Test
    public void updateInexistentContextByID_ShouldReturn404Test() throws Exception {

        USER_RequestsUtil.post("USER_POST_ExpectedRegisterDTOBody.json");
        String token = USER_RequestsUtil.authenticate("USER_POST_ExpectedRegisterDTOBody.json");
        Response contextDTOResponse = CONTEXT_RequestsUtil.post(token, "CONTEXT_POST_ExpectedRegisterDTOBody.json");

        JSONObject contextDTOJSON = new JSONObject(contextDTOResponse.getBody().prettyPrint());
        String contextID = contextDTOJSON.getString("id");

        CONTEXT_RequestsUtil.delete(token, contextID);

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


        USER_RequestsUtil.delete(token);
    }

    @Test
    public void updateContextByNonNumericID_ShouldReturn400Test() throws Exception {

        USER_RequestsUtil.post("USER_POST_ExpectedRegisterDTOBody.json");
        String token = USER_RequestsUtil.authenticate("USER_POST_ExpectedRegisterDTOBody.json");

        String contextID = "ID Non Numeric";

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
                .assertThat().statusCode(400)
                .extract().response();


        USER_RequestsUtil.delete(token);
    }

    @Test
    public void updateContextButAnyToken_ShouldReturn400Test() throws Exception {

        USER_RequestsUtil.post("USER_POST_ExpectedRegisterDTOBody.json");
        String token = USER_RequestsUtil.authenticate("USER_POST_ExpectedRegisterDTOBody.json");
        Response contextDTOResponse = CONTEXT_RequestsUtil.post(token, "CONTEXT_POST_ExpectedRegisterDTOBody.json");

        JSONObject contextDTOJSON = new JSONObject(contextDTOResponse.getBody().prettyPrint());
        String contextID = contextDTOJSON.getString("id");

        given()
                .body(FileUtils.getJsonFromFile("CONTEXT_POST_ExpectedRegisterDTOBody.json"))
                .contentType(ContentType.JSON)
                .when()
                .put(baseURI+":"+port+basePath+"auth/contexts/" + contextID)
                .then()
                .assertThat().statusCode(400);


        CONTEXT_RequestsUtil.delete(token, contextID);
        USER_RequestsUtil.delete(token);
    }

    @Test
    public void updateContextButExpiredOrMalformedToken_ShouldReturn500Test() throws Exception {

        USER_RequestsUtil.post("USER_POST_ExpectedRegisterDTOBody.json");
        String token = USER_RequestsUtil.authenticate("USER_POST_ExpectedRegisterDTOBody.json");
        Response contextDTOResponse = CONTEXT_RequestsUtil.post(token, "CONTEXT_POST_ExpectedRegisterDTOBody.json");

        JSONObject contextDTOJSON = new JSONObject(contextDTOResponse.getBody().prettyPrint());
        String contextID = contextDTOJSON.getString("id");

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


        CONTEXT_RequestsUtil.delete(token, contextID);
        USER_RequestsUtil.delete(token);
    }
    

    @Test
    public void deleteContextByTokenID_ShouldReturn200Test() throws Exception {

        USER_RequestsUtil.post("USER_POST_ExpectedRegisterDTOBody.json");
        String token = USER_RequestsUtil.authenticate("USER_POST_ExpectedRegisterDTOBody.json");
        Response contextDTOResponse = CONTEXT_RequestsUtil.post(token, "CONTEXT_POST_ExpectedRegisterDTOBody.json");

        JSONObject contextDTOJSON = new JSONObject(contextDTOResponse.getBody().prettyPrint());
        String contextID = contextDTOJSON.getString("id");

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


        USER_RequestsUtil.delete(token);
    }

    @Test
    public void deleteContextButAnyID_ShouldReturn405Test() throws Exception {

        USER_RequestsUtil.post("USER_POST_ExpectedRegisterDTOBody.json");
        String token = USER_RequestsUtil.authenticate("USER_POST_ExpectedRegisterDTOBody.json");

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


        USER_RequestsUtil.delete(token);
    }

    @Test
    public void deleteInexistentContextByID_ShouldReturn404Test() throws Exception {

        USER_RequestsUtil.post("USER_POST_ExpectedRegisterDTOBody.json");
        String token = USER_RequestsUtil.authenticate("USER_POST_ExpectedRegisterDTOBody.json");
        Response contextDTOResponse = CONTEXT_RequestsUtil.post(token, "CONTEXT_POST_ExpectedRegisterDTOBody.json");

        JSONObject contextDTOJSON = new JSONObject(contextDTOResponse.getBody().prettyPrint());
        String contextID = contextDTOJSON.getString("id");

        CONTEXT_RequestsUtil.delete(token, contextID);

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


        USER_RequestsUtil.delete(token);
    }

    @Test
    public void deleteContextButPassAnyToken_ShouldReturn400Test() throws Exception {

        USER_RequestsUtil.post("USER_POST_ExpectedRegisterDTOBody.json");
        String token = USER_RequestsUtil.authenticate("USER_POST_ExpectedRegisterDTOBody.json");
        Response contextDTOResponse = CONTEXT_RequestsUtil.post(token, "CONTEXT_POST_ExpectedRegisterDTOBody.json");

        JSONObject contextDTOJSON = new JSONObject(contextDTOResponse.getBody().prettyPrint());
        String contextID = contextDTOJSON.getString("id");

        given()
                .when()
                .delete(baseURI+":"+port+basePath+"auth/contexts/" + contextID)
                .then().assertThat().statusCode(400);


        USER_RequestsUtil.delete(token);
    }

    @Test
    public void deleteContextByExpiredOrMalformedToken_ShouldReturn500Test() throws Exception {

        USER_RequestsUtil.post("USER_POST_ExpectedRegisterDTOBody.json");
        String token = USER_RequestsUtil.authenticate("USER_POST_ExpectedRegisterDTOBody.json");
        Response contextDTOResponse = CONTEXT_RequestsUtil.post(token, "CONTEXT_POST_ExpectedRegisterDTOBody.json");

        JSONObject contextDTOJSON = new JSONObject(contextDTOResponse.getBody().prettyPrint());
        String contextID = contextDTOJSON.getString("id");

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


        USER_RequestsUtil.delete(token);
    }
}