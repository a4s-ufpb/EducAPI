package br.ufpb.dcx.apps4society.educapi;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.ComponentScan;

@SpringBootTest
@ComponentScan(basePackages = "repositories")
public class EducApiApplicationTests {

	@Test
	void contextLoads() {
	}

}