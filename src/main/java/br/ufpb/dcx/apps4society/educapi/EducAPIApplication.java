package br.ufpb.dcx.apps4society.educapi;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
@SpringBootApplication
public class EducAPIApplication {

	@Value("${app.version}")
	private String version;
	
	public static void main(String[] args) {
		SpringApplication.run(EducAPIApplication.class, args);	
	}

	@GetMapping("/")
    @ResponseBody
	public String index() {
      return String.format("Welcome to EducAPI! | VERSION: v%s", this.version);
    }

}
