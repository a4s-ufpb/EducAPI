package br.ufpb.dcx.apps4society.educapi.dto.user;

import java.io.Serializable;

import jakarta.validation.constraints.Email;
import org.hibernate.validator.constraints.Length;
import br.ufpb.dcx.apps4society.educapi.domain.User;
import jakarta.validation.constraints.NotEmpty;

public class UserDTO implements Serializable{
	private static final long serialVersionUID = 1L;
	
	private Long id;
	@NotEmpty(message="Required")
	@Length(min=3, max=80, message="The size must be between 3 and 80 characters")
	private String name;
	
	@NotEmpty(message="Required")
	@Email(message="Invalid email")
	private String email;
	
	@NotEmpty(message="Required")
	@Length(min=8, max=12, message="The size must be between 8 and 12 characters")
	private String password;
	
	public UserDTO() {}

	public User userDTOToUser(){
		return new User(id, name, email, password);
	}

	public UserDTO(User obj) {
		this.id = obj.getId();
		this.name = obj.getName();
		this.email = obj.getEmail();
		this.password = obj.getPassword();
	}

	@Override
	public String toString() {
		return "UserDTO{" +
				"id=" + id +
				", name='" + name + '\'' +
				", email='" + email + '\'' +
				", password='" + password + '\'' +
				'}';
	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
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
