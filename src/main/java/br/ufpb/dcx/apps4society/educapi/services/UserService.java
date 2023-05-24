package br.ufpb.dcx.apps4society.educapi.services;

import java.util.List;
import java.util.Optional;

import br.ufpb.dcx.apps4society.educapi.dto.user.UserRegisterDTO;
import br.ufpb.dcx.apps4society.educapi.services.exceptions.InvalidUserException;
import br.ufpb.dcx.apps4society.educapi.services.exceptions.UserAlreadyExistsException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.stereotype.Service;

import br.ufpb.dcx.apps4society.educapi.domain.User;
import br.ufpb.dcx.apps4society.educapi.dto.user.UserDTO;
import br.ufpb.dcx.apps4society.educapi.repositories.UserRepository;

@Service
public class UserService {

	@Autowired
	private JWTService jwtService;

	@Autowired
	private UserRepository userRepository;

	public User find(String token) throws InvalidUserException {
		Optional<String> userEmail = jwtService.recoverUser(token);

		if (userEmail.isEmpty()){
			throw new InvalidUserException("Invalid user! Please check the token.");
		}

		Optional<User> obgOptional = userRepository.findByEmail(userEmail.get());
		return obgOptional.get();
	}

	public UserDTO insert(UserRegisterDTO userDTO) throws UserAlreadyExistsException {
		Optional<User> userOptional = userRepository.findByEmail(userDTO.getEmail());

		if (userOptional.isPresent()){
			throw new UserAlreadyExistsException("There is already a user with this e-mail registered in the system!");
		}

		User user = userDTO.userRegisterDtoToUser();

		userRepository.save(user);
		return new UserDTO(user);
	}

	public UserDTO update(String token, UserRegisterDTO user) throws InvalidUserException {
		User newObj = find(token);
		updateData(newObj, user);
		userRepository.save(newObj);
		return new UserDTO(newObj);
	}

	public UserDTO delete(String token) throws InvalidUserException {
		User user = find(token);
		userRepository.deleteById(user.getId());
		return new UserDTO(user);
	}

	public List<User> findAll(){
		return userRepository.findAll();
	}

	public Page<User> findPage(Integer page, Integer linesPerPage, String orderBy, String direction){
		PageRequest pageRequest = PageRequest.of(page, linesPerPage, Direction.valueOf(direction), orderBy);
		return userRepository.findAll(pageRequest);
	}

	private void updateData(User newObj, UserRegisterDTO obj) {
		newObj.setName(obj.getName());
		newObj.setEmail(obj.getEmail());
		newObj.setPassword(obj.getPassword());
	}

}