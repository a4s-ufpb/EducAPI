package br.ufpb.dcx.apps4society.educapi.utils.builder;

import br.ufpb.dcx.apps4society.educapi.domain.User;
import br.ufpb.dcx.apps4society.educapi.dto.user.UserDTO;
import br.ufpb.dcx.apps4society.educapi.dto.user.UserLoginDTO;
import br.ufpb.dcx.apps4society.educapi.dto.user.UserRegisterDTO;

import java.util.Optional;

/**
 * Create an object that can be UserBuilder, UserRegisterDTO, UserLoginDTO or OptionalUser
 * Can create an object with custom or default data
 * This class used an adaptation of pattern Test Data Builder and pattern Builder
 *
 * Example 1:
 *      UserLoginDTO userLoginDTO = UserBuilder.anUser().buildUserLoginDTO();
 *      // UserLoginDTO with email = "user@educapi.com" and password = "testpassword";
 *
 * Example 2:
 *      UserLoginDTO userLoginDTO = UserBuilder.anUser().withEmail("foo@bar.com").buildUserLoginDTO();
 *      // UserLoginDTO with email = "foo@bar.com" and password = "testpassword";
 *
 * @author Enos Teteo
 */
public class UserBuilder {

    private Long id = null;
    private String name = "User";
    private String email = "user@educapi.com";
    private String password = "testpassword";
    public static UserBuilder anUser() {
        return new UserBuilder();
    }
    public UserBuilder withId(Long id) {
        this.id = id;
        return this;
    }
    public UserBuilder withEmail(String email) {
        this.email = email;
        return this;
    }
    public UserBuilder withName(String name) {
        this.name = name;
        return this;
    }
    public UserBuilder withPassword(String password) {
        this.password = password;
        return this;
    }
    public UserDTO buildUserDTO(){

        UserDTO userDTO = new UserDTO();
        userDTO.setId(this.id);
        userDTO.setName(this.name);
        userDTO.setEmail(this.email);
        userDTO.setPassword(this.password);

        return userDTO;
    }
    public Optional<User> buildOptionalUser() { return Optional.ofNullable(new User(this.id, this.name, this.email, this.password)); }

    /**
     * Generate an UserRegisterDTO object containing custom or default data
     * Example 1:
     *      UserBuilder.anUser().buildUserRegisterDTO();
     *      // return an UserRegisterDTO with default data
     *
     * Example 2:
     *      UserBuilder.anUser().withName("User Register DTO").buildUserRegisterDTO();
     *      // return an UserRegisterDTO with all data default, but with name "User Register DTO"
     *
     * @return UserRegisterDTO
     */



    public UserRegisterDTO buildUserRegisterDTO() { return new UserRegisterDTO(this.name, this.email, this.password);
    }

    /**
     * Generate an UserLoginDTO object containing custom or default data
     * Example 1:
     *      UserBuilder.anUser().buildUserLoginDTO();
     *      // return an UserLoginDTO with default data
     *
     * Example 2:
     *      UserBuilder.anUser().withName("User Login DTO").buildUserLoginDTO();
     *      // return an UserLoginDTO with all data default, but with name "User Login DTO"
     *
     * @return UserRegisterDTO
     */
    public UserLoginDTO buildUserLoginDTO() {
        return new UserLoginDTO(this.email, this.password);
    }
}
