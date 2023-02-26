package br.ufpb.dcx.apps4society.educapi.resources;

import br.ufpb.dcx.apps4society.educapi.repositories.UserRepository;
import br.ufpb.dcx.apps4society.educapi.services.UserService;
import br.ufpb.dcx.apps4society.educapi.unit.domain.builder.ServicesBuilder;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;

@ExtendWith(SpringExtension.class)
@WebMvcTest(UserResource.class)
class UserResourceIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

//    @Autowired
//    private TestEntityManager testEntityManager;

    @MockBean
    public UserRepository userRepository;

    @Autowired
    public UserResource userResource;

    @MockBean
    public UserService userService;

    //@Autowired
    //public UserService userService = ServicesBuilder.anService()
    //    .withUserRepository(userRepository).buildUserService();

    @Test
    void find() {


    }

    @Test
    void insert() {
    }

    @Test
    void update() {
    }

    @Test
    void delete() {
    }
}