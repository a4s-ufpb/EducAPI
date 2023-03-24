

package br.ufpb.dcx.apps4society.educapi;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class EducApiApplicationTests {

	@Test
	void contextLoads() {
	}

}

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
//
//		import br.ufpb.dcx.apps4society.educapi.domain.User;
//		import org.junit.jupiter.api.Assertions;
//		import org.junit.jupiter.api.BeforeAll;
//		import org.junit.jupiter.api.BeforeEach;
//		import org.junit.jupiter.api.Test;
//		import org.junit.runner.RunWith;
//		import org.springframework.boot.SpringApplication;
//		import org.springframework.boot.test.context.SpringBootTest;
//		import org.springframework.boot.web.server.LocalServerPort;
//		import org.springframework.test.context.ContextConfiguration;
//		import org.springframework.test.context.TestPropertySource;
//		import org.springframework.test.context.junit4.SpringRunner;
//		import org.springframework.web.client.RestTemplate;
//
//@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
//@ContextConfiguration(classes=EducApiApplicationTests.class)
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
