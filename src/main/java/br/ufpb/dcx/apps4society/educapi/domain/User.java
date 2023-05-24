package br.ufpb.dcx.apps4society.educapi.domain;

import com.fasterxml.jackson.annotation.JsonIgnore;

import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;

import javax.persistence.*;

/**
 * Represents a User that created a Challenge or Context.
 * 
 * @author Ayla Dantas
 * @author Emerson Dantas
 */
@Entity(name="educapi_user")
public class User implements Serializable {
	private static final long serialVersionUID = 1L;
	@Id
	@GeneratedValue(strategy=GenerationType.IDENTITY)
	private Long id;
	private String name;
	private String email;
	@JsonIgnore
	private String password;
	@JsonIgnore
	@OneToMany(mappedBy = "creator", cascade = CascadeType.ALL)
	private Set<Challenge> challenges = new HashSet<>();
	@JsonIgnore
	@OneToMany(mappedBy = "creator", cascade = CascadeType.ALL)
	private Set<Context> contexts = new HashSet<>();
	
	/**
	 * Empty Constructor
	 * 
	 */
	public User() {}

	public User(String name, String email, String password) {
		this.name = name;
		this.setEmail(email);
		this.setPassword(password);
	}
	
	/**
	 * Constructor
	 * @param id
	 *            The user id.
	 *            
	 * @param name
	 *            The user name.
	 * 
	 * @param email
	 *            The user email.
	 *            
	 * @param password
	 *            The user password.
	 */
	public User(Long id, String name, String email, String password) {
		this.id = id;
		this.name = name;
		this.setEmail(email);
		this.setPassword(password);
	}

	/**
	 * The name of the User.
	 * 
	 * @return the name of the user.
	 */
	public String getName() {
		return name;
	}

	/**
	 * Changes the name of the User.
	 * 
	 * @param name
	 *            The new name for the User.
	 */
	public void setName(String name) {
		this.name = name;
	}

	/**
	 * Returns the id of this User.
	 * 
	 * @return the id of this User.
	 */
	public Long getId() {
		return this.id;
	}

	/**
	 * Changes the if of this User.
	 * 
	 * @param userId
	 *            The new id for this User.
	 */
	public void setId(Long id) {
		this.id = id;
	}

	/**
	 * Gets the user email.
	 * 
	 * @return the user email.
	 */
	public String getEmail() {
		return email;
	}

	/**
	 * Changes the user email
	 * 
	 * @param email
	 *            The new email value.
	 * 
	 */
	public void setEmail(String email) {
		this.email = email;
	}
	
	/**
	 * Gets the user password.
	 * 
	 * @return the user password.
	 */
	@JsonIgnore
	public String getPassword() {
		return this.password;
	}
	
	/**
	 * Changes the user password.
	 * 
	 * @param password
	 *            The new password value.
	 * 
	 */
	public void setPassword(String password) {
		this.password = password;
	}
	
	/**
	 * Gets the user challenges.
	 * 
	 * @return the user challenges.
	 */
	public Set<Challenge> getChallenges() {
		return challenges;
	}

	/**
	 * Changes the user challenges.
	 * 
	 * @param Set<Challenge> challenges
	 *         				The new challenges.
	 * 
	 */
	public void setChallenges(Set<Challenge> challenges) {
		this.challenges = challenges;
	}

	/**
	 * Gets the user contexts.
	 * 
	 * @return the user contexts.
	 */
	public Set<Context> getContexts() {
		return contexts;
	}

	/**
	 * Changes the user contexts.
	 * 
	 * @param Set<Context> contexts.
	 *         				The new contexts.
	 * 
	 */
	public void setContexts(Set<Context> contexts) {
		this.contexts = contexts;
	}

	@Override
	public String toString() {
		return "User [id=" + id + ", name=" + name + ", email=" + email + "]";
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((id == null) ? 0 : id.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		User other = (User) obj;
		if (id == null) {
			if (other.id != null)
				return false;
		} else if (!id.equals(other.id))
			return false;
		return true;
	}

}
