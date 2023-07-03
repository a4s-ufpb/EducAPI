package br.ufpb.dcx.apps4society.educapi.dto.user;

import br.ufpb.dcx.apps4society.educapi.domain.User;
import org.hibernate.validator.constraints.Length;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotEmpty;
import java.io.Serializable;

public class UserRegisterDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    @NotEmpty(message="Required")
    @Length(min=3, max=80, message="The size must be between 3 and 80 characters")
    private String name;

    @NotEmpty(message="Required")
    @Email(message="Invalid email")
    private String email;

    @NotEmpty(message="Required")
    @Length(min=8, max=12, message="The size must be between 8 and 12 characters")
    private String password;

    public UserRegisterDTO(String name, String email, String password) {
        this.name = name;
        this.email = email;
        this.password = password;
    }

    public User userRegisterDtoToUser(){
        return new User(name, email, password);
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }
}
