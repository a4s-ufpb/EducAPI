package br.ufpb.dcx.apps4society.educapi.resources;

import br.ufpb.dcx.apps4society.educapi.EducApiApplicationTests;
import br.ufpb.dcx.apps4society.educapi.domain.User;
import br.ufpb.dcx.apps4society.educapi.dto.user.UserLoginDTO;
import br.ufpb.dcx.apps4society.educapi.dto.user.UserRegisterDTO;
import br.ufpb.dcx.apps4society.educapi.repositories.UserRepository;
import br.ufpb.dcx.apps4society.educapi.services.UserService;
import br.ufpb.dcx.apps4society.educapi.unit.FileUtils;
import br.ufpb.dcx.apps4society.educapi.unit.domain.builder.UserBuilder;

import java.net.URI;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultHandlers;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = UserResource.class)
@AutoConfigureMockMvc
@ContextConfiguration(classes = EducApiApplicationTests.class)
@ActiveProfiles("test")
class UserResourceIntegrationTest {

    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private UserResource userResource;

    @MockBean
    private UserRepository userRepository;
    @MockBean
    private UserService userService;

    private static String NAME = "Jose";
    private static String EMAIL = "jose@educapi.com";
    private static String PASSWORD = "12345678";

    private static String USER_POST_ENDPOINT = "http://localhost:8080/v1/api/users";

    private final UserLoginDTO userLoginDTO = UserBuilder.anUser().buildUserLoginDTO();
    private final UserLoginDTO userLoginEmailEmptyDTO = UserBuilder.anUser().withName("User3").withEmail("").buildUserLoginDTO();

    private final UserRegisterDTO userRegisterDTO = UserBuilder.anUser()
            .withName(NAME)
            .withEmail(EMAIL)
            .withPassword(PASSWORD).buildUserRegisterDTO();

    private User user = userRegisterDTO.userRegisterDtoToUser();

    @BeforeEach
    public void setUp(){
        mockMvc = MockMvcBuilders.standaloneSetup(userResource).build();
    }

     @Test
     public void insertUserWithCorrectsInputs_ThenReturnStatus201() throws Exception {

         URI uri = new URI(USER_POST_ENDPOINT);

         String requestBody = FileUtils.getJsonFromFile("CreateUser.json");

         mockMvc.perform(MockMvcRequestBuilders
                         .post(uri)
                         .contentType(MediaType.APPLICATION_JSON)
                         .content(requestBody)
                 )
                 .andExpect(MockMvcResultMatchers.status().isCreated())
                 .andDo(MockMvcResultHandlers.print());

     }

    @Test
    public void insertUserWithEmptyBody_ThenReturnStatus400() throws Exception {

        URI uri = new URI(USER_POST_ENDPOINT);

        String requestBody = FileUtils.getJsonFromFile("CreateUser.json");

        mockMvc.perform(MockMvcRequestBuilders
                        .post(uri)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("")
                )
                .andExpect(MockMvcResultMatchers.status().isBadRequest())
                .andDo(MockMvcResultHandlers.print());

    }

    public void insertUserAlreadyExists_ThenReturnStatus() throws Exception {

        URI uri = new URI(USER_POST_ENDPOINT);

        String requestBody = FileUtils.getJsonFromFile("CreateUser.json");

        mockMvc.perform(MockMvcRequestBuilders
                        .post(uri)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody)
                )
                .andExpect(MockMvcResultMatchers.status().isCreated())
                .andDo(MockMvcResultHandlers.print());

    }

//     @Test
//     public void findByUserEmailTest() throws InvalidUserException {
//
//     }
//
//     @Test
//     public void findByInvalidUserEmailTest(){
//
//     }
//
//     @Test
//     void update() {
//     }
//
//     @Test
//     void delete() {
//     }

//    https://www.youtube.com/watch?v=Y4_LmPhx1Jc
//    https://www.youtube.com/watch?v=l5WfHfHvqo8
//    https://www.youtube.com/watch?v=3duamjhP7NM

}