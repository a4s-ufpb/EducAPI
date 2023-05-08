package br.ufpb.dcx.apps4society.educapi;

import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.test.annotation.DirtiesContext;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT)
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.ANY)
public class EducApiApplicationTests {
	@LocalServerPort
	public int port;

	@Autowired
	public TestRestTemplate restTemplate;

	public static final String invalidToken = "eyJhbGciOiJIUzUxMiJ9.eyJzdWIiOiJqb3NlMTdAZWR1Y2FwaS5jb20iLCJleHAiOjE2ODA2OTc2MjN9." +
			"qfwlZuirBvosD82v-7lHxb8qhH54_KXR20_0z3guG9rZOW68l5y3gZtvugBtpevmlgK76dsa4hOUPOooRiJ3ng";

	@BeforeEach
	public void setupServerParam() {
		io.restassured.RestAssured.baseURI = "http://localhost";
		io.restassured.RestAssured.basePath = "/v1/api/";
		io.restassured.RestAssured.port = this.port;
	}

}

//import br.ufpb.dcx.apps4society.educapi.repositories.UserRepository;
//import org.junit.jupiter.api.Test;
//import org.springframework.boot.test.context.SpringBootTest;
//import org.springframework.context.annotation.ComponentScan;
//
//@SpringBootTest
//@ComponentScan(basePackages = "repositories")
//public class EducApiApplicationTests {
//
//	@Test
//	void contextLoads() {
//	}
//
//}

//package br.ufpb.dcx.apps4society.educapi;
//
//import br.ufpb.dcx.apps4society.educapi.domain.User;
//import org.junit.jupiter.api.Assertions;
//import org.junit.jupiter.api.BeforeAll;
//import org.junit.jupiter.api.BeforeEach;
//import org.junit.jupiter.api.Test;
//import org.junit.runner.RunWith;
//import org.springframework.boot.SpringApplication;
//import org.springframework.boot.test.context.SpringBootTest;
//import org.springframework.boot.web.server.LocalServerPort;
//import org.springframework.test.context.ContextConfiguration;
//import org.springframework.test.context.TestPropertySource;
//import org.springframework.test.context.junit4.SpringRunner;
//import org.springframework.web.client.RestTemplate;
//import br.ufpb.dcx.apps4society.educapi.domain.User;
//import org.junit.jupiter.api.Assertions;
//import org.junit.jupiter.api.BeforeAll;
//import org.junit.jupiter.api.BeforeEach;
//import org.junit.jupiter.api.Test;
//import org.junit.runner.RunWith;
//import org.springframework.boot.SpringApplication;
//import org.springframework.boot.test.context.SpringBootTest;
//import org.springframework.boot.web.server.LocalServerPort;
//import org.springframework.test.context.ContextConfiguration;
//import org.springframework.test.context.TestPropertySource;
//import org.springframework.test.context.junit4.SpringRunner;
//import org.springframework.web.client.RestTemplate;
//
//@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
////@ContextConfiguration(classes=EducApiApplicationTests.class)
//public class EducApiApplicationTests {
//
//	@LocalServerPort
//	private int port;
//
//	private String baseUrl="http://localhost";
//
//	private static RestTemplate restTemplate;
//
//	@BeforeAll
//	public static void init(){
//		restTemplate = new RestTemplate();
//	}
//	@BeforeEach
//	public void setUp(){
//		baseUrl=baseUrl.concat(":").concat(port+"").concat("/v1/api/users");
//	}
//	@Test
//	public void insertUserTest(){
//		User user = new User("nome", "eu@educapi.com", "12345678");
//		User response = restTemplate.postForObject(baseUrl, user, User.class);
//		Assertions.assertEquals("nome", response.getName());
//	}
//
//}
