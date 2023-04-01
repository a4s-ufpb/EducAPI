package br.ufpb.dcx.apps4society.educapi;

import br.ufpb.dcx.apps4society.educapi.repositories.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
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

	@Autowired
	UserRepository userRepository;
	
	public static void main(String[] args) {
		SpringApplication.run(EducAPIApplication.class, args);	
	}

	@GetMapping("/")
    @ResponseBody
	public String index() {
      return String.format("Welcome to EducAPI! | VERSION: v%s", this.version);
    }


//	@Bean
//	public CommandLineRunner commandLineRunner() {
//		return args -> {
//
//
//
//			User user = new User();
//			user.setEmail("user@educapi.com");
//			user.setPassword("12345678");
//			user.setName("usuario");
//			user.setId(1L);
//			this.userRepository.save(user);
//
//			Context context = new Context();
//			context.setId(1L);
//			Set<Context> contextSet = new HashSet<>();
//			contextSet.add(context);
//			user.setContexts(contextSet);
//
//		};
//
//	}

}
