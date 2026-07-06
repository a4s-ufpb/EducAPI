package br.ufpb.dcx.apps4society.educapi.resources;

import br.ufpb.dcx.apps4society.educapi.domain.User;
import br.ufpb.dcx.apps4society.educapi.dto.user.UserChangePasswordDTO;
import br.ufpb.dcx.apps4society.educapi.dto.user.UserDTO;
import br.ufpb.dcx.apps4society.educapi.dto.user.UserRegisterDTO;
import br.ufpb.dcx.apps4society.educapi.services.UserService;
import io.swagger.v3.oas.annotations.Operation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
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

	@Operation(summary = "Changes the User password, if the token is valid. Not allowed for Google accounts.")
	@PutMapping("auth/users/password")
	public ResponseEntity<UserDTO> changePassword(@Valid @RequestBody UserChangePasswordDTO changePasswordDTO,
												   @RequestHeader("Authorization") String token) {
		return ResponseEntity.ok(userService.changePassword(token, changePasswordDTO));
	}

	@Operation(summary = "Deletes the user from the service, if the token is valid.")
	@DeleteMapping("auth/users")
	public ResponseEntity<UserDTO> delete(@RequestHeader("Authorization") String token) {
		return ResponseEntity.ok(userService.delete(token));
	}

	@Operation(summary = "Returns a paginated list of every User in the system, for administrative user-management. Restricted to SYSADMIN.")
	@PreAuthorize("hasRole('SYSADMIN')")
	@GetMapping("admin/users")
	public ResponseEntity<Page<UserDTO>> findAllForAdmin(
			@RequestParam(value = "size", defaultValue = "20") Integer size,
			@RequestParam(value = "page", defaultValue = "0") Integer page,
			Pageable pageable) {
		return ResponseEntity.ok(userService.findAllForAdmin(pageable));
	}

	@Operation(summary = "Promotes a CLIENTE User to ADMIN. Restricted to SYSADMIN.")
	@PreAuthorize("hasRole('SYSADMIN')")
	@PutMapping("admin/users/{id}/promote")
	public ResponseEntity<UserDTO> promote(@RequestHeader("Authorization") String token,
											@PathVariable Long id) {
		return ResponseEntity.ok(userService.promote(token, id));
	}

	@Operation(summary = "Demotes an ADMIN User back to CLIENTE. Restricted to SYSADMIN.")
	@PreAuthorize("hasRole('SYSADMIN')")
	@PutMapping("admin/users/{id}/demote")
	public ResponseEntity<UserDTO> demote(@RequestHeader("Authorization") String token,
										   @PathVariable Long id) {
		return ResponseEntity.ok(userService.demote(token, id));
	}

	@Operation(summary = "Deletes another User's account. Restricted to SYSADMIN. "
			+ "Content created by the deleted user is orphaned, not removed.")
	@PreAuthorize("hasRole('SYSADMIN')")
	@DeleteMapping("admin/users/{id}")
	public ResponseEntity<UserDTO> deleteByAdmin(@RequestHeader("Authorization") String token,
												  @PathVariable Long id) {
		return ResponseEntity.ok(userService.deleteByAdmin(token, id));
	}

}
