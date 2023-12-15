package br.ufpb.dcx.apps4society.educapi.resources;

import br.ufpb.dcx.apps4society.educapi.dto.user.UserRegisterDTO;
import br.ufpb.dcx.apps4society.educapi.services.exceptions.InvalidUserException;
import br.ufpb.dcx.apps4society.educapi.services.exceptions.UserAlreadyExistsException;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import br.ufpb.dcx.apps4society.educapi.domain.User;
import br.ufpb.dcx.apps4society.educapi.dto.user.UserDTO;
import br.ufpb.dcx.apps4society.educapi.services.UserService;

@RestController
@RequestMapping(value="/v1/api/")
@CrossOrigin("*")
public class UserResource {
	@Autowired
	private UserService userService;

	@Operation(summary = "Returns a User if the token is valid.")
	@GetMapping("auth/users")
	public ResponseEntity<User> find(@RequestHeader ("Authorization") String token) {
		try{
			return new ResponseEntity<User>(userService.find(token), HttpStatus.OK);
		}catch (InvalidUserException | SecurityException exception){
			return new ResponseEntity<>(HttpStatus.FORBIDDEN);
		}
	}

	@Operation(summary = "Register a new User to the service.")
	@PostMapping("users")
	public ResponseEntity<UserDTO> insert(@Valid @RequestBody UserRegisterDTO userRegister) {
		try{
			return new ResponseEntity<UserDTO>(userService.insert(userRegister), HttpStatus.CREATED);
		}catch (UserAlreadyExistsException existsException){
			return new ResponseEntity<>(HttpStatus.NO_CONTENT);
		}
	}

	@Operation(summary = "Updates User information, if the token is valid.")
	@PutMapping("auth/users")
	public ResponseEntity<UserDTO> update(@Valid @RequestBody UserRegisterDTO registerDTO,
										  @RequestHeader("Authorization") String token){
		try{
			return new ResponseEntity<UserDTO>(userService.update(token, registerDTO), HttpStatus.OK);
		}catch (InvalidUserException | SecurityException exception){
			return new ResponseEntity<>(HttpStatus.FORBIDDEN);
		}
	}

	@Operation(summary = "Deletes the user from the service, if the token is valid.")
	@DeleteMapping("auth/users")
	public ResponseEntity<UserDTO> delete(@RequestHeader("Authorization") String token) {
		try {
			return new ResponseEntity<UserDTO>(userService.delete(token), HttpStatus.OK);
		}catch (InvalidUserException | SecurityException exception){
			return new ResponseEntity<>(HttpStatus.FORBIDDEN);
		}
	}

/*
	@RequestMapping(method=RequestMethod.GET, produces="application/json")
	public ResponseEntity<List<User>> findAll(){
		List<User> list = service.findAll();
		return ResponseEntity.ok().body(list);
	}


	//@RequestMapping(value="/page", method=RequestMethod.GET, produces="application/json")
	public ResponseEntity<Page<UserDTO>> findPage(
			@RequestParam(value="page", defaultValue="0") Integer page, 
			@RequestParam(value="linesPerPage", defaultValue="10") Integer linesPerPage, 
			@RequestParam(value="orderBy", defaultValue="name") String orderBy, 
			@RequestParam(value="direction", defaultValue="ASC") String direction){
		Page<User> list = userService.findPage(page, linesPerPage, orderBy, direction);
		//Page<UserDTO> listDto = list.map(obj -> new UserDTO(obj));
		//re turn ResponseEntity.ok().body(listDto);
	}
*/
}
