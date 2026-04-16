package br.ufpb.dcx.apps4society.educapi;

import org.junit.jupiter.api.BeforeAll;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import static io.restassured.RestAssured.basePath;
import static io.restassured.RestAssured.baseURI;
import static io.restassured.RestAssured.port;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT)
@ActiveProfiles("test")
public class EducApiApplicationTests {

	public static String invalidToken = "eyJhbGciOiJIUzUxMiJ9.eyJzdWIiOiJqb3NlMTdAZWR1Y2FwaS5jb20iLCJleHAiOjE2ODA2OTc2MjN9." +
			"qfwlZuirBvosD82v-7lHxb8qhH54_KXR20_0z3guG9rZOW68l5y3gZtvugBtpevmlgK76dsa4hOUPOooRiJ3ng";

	@BeforeAll
	public static void setUp(){
		baseURI = "http://localhost";
		port = 8080;
		basePath = "/v1/api/";
	}
}