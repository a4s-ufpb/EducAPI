package br.ufpb.dcx.apps4society.educapi.resources;

import br.ufpb.dcx.apps4society.educapi.domain.User;
import br.ufpb.dcx.apps4society.educapi.dto.user.UserDTO;
import br.ufpb.dcx.apps4society.educapi.dto.user.UserRegisterDTO;
import br.ufpb.dcx.apps4society.educapi.services.UserService;
import io.swagger.v3.oas.annotations.Operation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;

@RestController
@RequestMapping(value="/v1/api/")
@CrossOrigin("*")
public class UserResource {
	@Autowired
	private UserService userService;

	@Operation(summary = "Returns a User if the token is valid.")
	@GetMapping("auth/users")
	public ResponseEntity<User> find(@RequestHeader ("Authorization") String token) {
		return ResponseEntity.ok(userService.find(token));
	}

	@Operation(summary = "Register a new User to the service.")
	@PostMapping("users")
	public ResponseEntity<UserDTO> insert(@Valid @RequestBody UserRegisterDTO userRegister) {
		return ResponseEntity.status(HttpStatus.CREATED).body(userService.insert(userRegister));
	}

	@Operation(summary = "Updates User information, if the token is valid.")
	@PutMapping("auth/users")
	public ResponseEntity<UserDTO> update(@Valid @RequestBody UserRegisterDTO registerDTO,
										  @RequestHeader("Authorization") String token){
		return ResponseEntity.ok(userService.update(token,registerDTO));
	}

	@Operation(summary = "Deletes the user from the service, if the token is valid.")
	@DeleteMapping("auth/users")
	public ResponseEntity<UserDTO> delete(@RequestHeader("Authorization") String token) {
		return ResponseEntity.ok(userService.delete(token));
	}

}
