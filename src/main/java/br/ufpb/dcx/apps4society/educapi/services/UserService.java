package br.ufpb.dcx.apps4society.educapi.services;

import java.util.List;
import java.util.Optional;

import br.ufpb.dcx.apps4society.educapi.domain.AcaoAuditoria;
import br.ufpb.dcx.apps4society.educapi.domain.Challenge;
import br.ufpb.dcx.apps4society.educapi.domain.Context;
import br.ufpb.dcx.apps4society.educapi.domain.Role;
import br.ufpb.dcx.apps4society.educapi.dto.user.UserChangePasswordDTO;
import br.ufpb.dcx.apps4society.educapi.dto.user.UserRegisterDTO;
import br.ufpb.dcx.apps4society.educapi.repositories.ChallengeRepository;
import br.ufpb.dcx.apps4society.educapi.repositories.ContextRepository;
import br.ufpb.dcx.apps4society.educapi.services.exceptions.GoogleAccountException;
import br.ufpb.dcx.apps4society.educapi.services.exceptions.InsufficientPrivilegeException;
import br.ufpb.dcx.apps4society.educapi.services.exceptions.InvalidRoleTransitionException;
import br.ufpb.dcx.apps4society.educapi.services.exceptions.InvalidUserException;
import br.ufpb.dcx.apps4society.educapi.services.exceptions.ObjectNotFoundException;
import br.ufpb.dcx.apps4society.educapi.services.exceptions.SelfActionNotAllowedException;
import br.ufpb.dcx.apps4society.educapi.services.exceptions.UserAlreadyExistsException;
import br.ufpb.dcx.apps4society.educapi.services.exceptions.UserAlreadyPromotedException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import br.ufpb.dcx.apps4society.educapi.domain.User;
import br.ufpb.dcx.apps4society.educapi.dto.user.UserDTO;
import br.ufpb.dcx.apps4society.educapi.repositories.UserRepository;

@Service
public class UserService {
	@Autowired
	private JWTService jwtService;

	@Autowired
	private UserRepository userRepository;

	@Autowired
	private ContextRepository contextRepository;

	@Autowired
	private ChallengeRepository challengeRepository;

	@Autowired
	private LogAuditoriaService logAuditoriaService;

	// To support ServicesBuilder
	public UserService(JWTService jwtService, UserRepository userRepository, ContextRepository contextRepository,
			ChallengeRepository challengeRepository, LogAuditoriaService logAuditoriaService) {
		this.jwtService = jwtService;
		this.userRepository = userRepository;
		this.contextRepository = contextRepository;
		this.challengeRepository = challengeRepository;
		this.logAuditoriaService = logAuditoriaService;
	}

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

	public UserDTO changePassword(String token, UserChangePasswordDTO dto) throws InvalidUserException {
		User user = find(token);

		if (user.isGoogleAccount()) {
			throw new GoogleAccountException(
					"This account was created with Google Sign-In and has no local password. It's not possible to change the password.");
		}

		if (!user.getPassword().equals(dto.getCurrentPassword())) {
			throw new InvalidUserException("Current password is incorrect.");
		}

		user.setPassword(dto.getNewPassword());
		userRepository.save(user);
		return new UserDTO(user);
	}

	/**
	 * Deletes the caller's own account. By default the Contexts/Challenges
	 * created by this user are orphaned (creator = null) and stay in the
	 * system, mirroring admin-triggered deletions in
	 * {@link #deleteByAdmin(String, Long)}. If {@code deleteChallenges} is
	 * true, the user's own Challenges and Contexts (themes) are permanently
	 * deleted instead of being orphaned.
	 */
	@Transactional
	public UserDTO delete(String token, boolean deleteChallenges) throws InvalidUserException {
		User user = find(token);

		if (deleteChallenges) {
			deleteUserChallenges(user);
			deleteUserContexts(user);
		} else {
			orphanUserContent(user);
		}

		userRepository.deleteById(user.getId());

		logAuditoriaService.registrar(user, AcaoAuditoria.EXCLUSAO_USUARIO, "User", user.getId(),
				"email=" + user.getEmail() + ", role=" + user.getRole()
				+ ", autoexclusao (usuario excluiu a propria conta)"
				+ (deleteChallenges ? ", temas e desafios do usuario tambem foram excluidos" : ", temas e desafios do usuario foram mantidos (orfaos)"));

		return new UserDTO(user);
	}

	public List<User> findAll(){
		return userRepository.findAll();
	}

	public Page<User> findPage(Integer page, Integer linesPerPage, String orderBy, String direction){
		PageRequest pageRequest = PageRequest.of(page, linesPerPage, Direction.valueOf(direction), orderBy);
		return userRepository.findAll(pageRequest);
	}

	/**
	 * Returns a paginated list of every User in the system, for administrative
	 * user-management screens (promote to ADMIN, delete users). Restricted at
	 * the controller level to ADMIN/SYSADMIN via {@code @PreAuthorize}.
	 */
	public Page<UserDTO> findAllForAdmin(Pageable pageable) {
		return userRepository.findAll(pageable).map(UserDTO::new);
	}

	/**
	 * Promotes a CLIENTE to ADMIN. Restricted to callers with the SYSADMIN role.
	 *
	 * @param token the requester's auth token; must belong to a SYSADMIN.
	 * @param id    the id of the User to promote.
	 */
	public UserDTO promote(String token, Long id) throws InvalidUserException, ObjectNotFoundException {
		User requester = find(token);

		if (!requester.isSysAdmin()) {
			throw new InsufficientPrivilegeException("Apenas o SYSADMIN pode promover usuarios a ADMIN.");
		}

		User target = findUserOrThrow(id);

		if (target.getRole() != Role.CLIENTE) {
			throw new UserAlreadyPromotedException(
					"Usuario " + target.getEmail() + " ja possui privilegios administrativos (role=" + target.getRole() + ").");
		}

		target.setRole(Role.ADMIN);
		userRepository.save(target);

		logAuditoriaService.registrar(requester, AcaoAuditoria.PROMOCAO_ADMIN, "User", target.getId(),
				"email=" + target.getEmail() + " promovido de CLIENTE para ADMIN");

		return new UserDTO(target);
	}

	/**
	 * Demotes an ADMIN back to CLIENTE. Restricted to callers with the
	 * SYSADMIN role — the symmetric, inverse operation of {@link #promote}.
	 * SYSADMIN accounts cannot be demoted through this action, and a
	 * SYSADMIN cannot demote themselves.
	 *
	 * @param token the requester's auth token; must belong to a SYSADMIN.
	 * @param id    the id of the User to demote.
	 */
	public UserDTO demote(String token, Long id) throws InvalidUserException, ObjectNotFoundException {
		User requester = find(token);

		if (!requester.isSysAdmin()) {
			throw new InsufficientPrivilegeException("Apenas o SYSADMIN pode rebaixar usuarios.");
		}

		User target = findUserOrThrow(id);

		if (requester.getId().equals(target.getId())) {
			throw new SelfActionNotAllowedException("Nao e possivel alterar a propria role por este endpoint.");
		}

		if (target.getRole() != Role.ADMIN) {
			throw new InvalidRoleTransitionException(
					"Usuario " + target.getEmail() + " nao pode ser rebaixado (role atual=" + target.getRole() + "). "
					+ "Apenas usuarios com role=ADMIN podem ser rebaixados para CLIENTE.");
		}

		target.setRole(Role.CLIENTE);
		userRepository.save(target);

		logAuditoriaService.registrar(requester, AcaoAuditoria.DEMOCAO_ADMIN, "User", target.getId(),
				"email=" + target.getEmail() + " rebaixado de ADMIN para CLIENTE");

		return new UserDTO(target);
	}

	/**
	 * Deletes another User's account, as an administrative action.
	 * SYSADMIN may delete any user (including ADMIN); ADMIN may only delete
	 * CLIENTE accounts. Self-deletion through this endpoint is not allowed
	 * (use {@link #delete(String)} instead).
	 *
	 * Any Context/Challenge created by the deleted user is not removed:
	 * their {@code creator} is set to null so the content is orphaned but
	 * remains in the system.
	 *
	 * @param token the requester's auth token; must belong to an ADMIN or SYSADMIN.
	 * @param id    the id of the User to delete.
	 */
	@Transactional
	public UserDTO deleteByAdmin(String token, Long id) throws InvalidUserException, ObjectNotFoundException {
		User requester = find(token);
		User target = findUserOrThrow(id);

		if (requester.getId().equals(target.getId())) {
			throw new SelfActionNotAllowedException("Nao e possivel excluir a propria conta por este endpoint.");
		}

		if (!requester.isAdmin()) {
			throw new InsufficientPrivilegeException("Apenas ADMIN ou SYSADMIN podem excluir outros usuarios.");
		}

		if (!requester.isSysAdmin() && target.getRole() != Role.CLIENTE) {
			throw new InsufficientPrivilegeException(
					"ADMIN so pode excluir usuarios com role CLIENTE. Usuario alvo possui role=" + target.getRole() + ".");
		}

		orphanUserContent(target);

		UserDTO deletedUserDTO = new UserDTO(target);
		userRepository.deleteById(target.getId());

		logAuditoriaService.registrar(requester, AcaoAuditoria.EXCLUSAO_USUARIO, "User", id,
				"email=" + target.getEmail() + ", role=" + target.getRole()
				+ ", excluido por " + requester.getRole() + " (" + requester.getEmail() + ")");

		return deletedUserDTO;
	}

	private User findUserOrThrow(Long id) throws ObjectNotFoundException {
		Optional<User> userOptional = userRepository.findById(id);
		if (userOptional.isEmpty()) {
			throw new ObjectNotFoundException("Object not found! Id: " + id + ", Type: " + User.class.getName());
		}
		return userOptional.get();
	}

	/**
	 * Detaches all Context/Challenge entities created by the given user,
	 * leaving them orphaned (creator = null) instead of cascade-deleting them.
	 */
	private void orphanUserContent(User target) {
		orphanUserContexts(target);

		List<Challenge> challenges = challengeRepository.findChallengesByCreator(target);
		for (Challenge challenge : challenges) {
			challenge.setCreator(null);
			challengeRepository.save(challenge);
		}
	}

	/**
	 * Detaches all Context entities created by the given user, leaving them
	 * orphaned (creator = null) instead of removing them. Contexts are always
	 * kept, regardless of what happens to the user's Challenges, since other
	 * Challenges (not created by this user) may still belong to them.
	 */
	private void orphanUserContexts(User target) {
		List<Context> contexts = contextRepository.findContextsByCreator(target);
		for (Context context : contexts) {
			context.setCreator(null);
			contextRepository.save(context);
		}
	}

	/**
	 * Permanently deletes every Challenge created by the given user, used when
	 * self-deletion is requested with deleteChallenges=true. Own Challenges are
	 * deleted before the user's Contexts so that Challenges belonging to other
	 * creators aren't touched by the Context's cascade removal below.
	 */
	private void deleteUserChallenges(User target) {
		List<Challenge> challenges = challengeRepository.findChallengesByCreator(target);
		challengeRepository.deleteAll(challenges);
	}

	/**
	 * Permanently deletes every Context (theme) created by the given user,
	 * used when self-deletion is requested with deleteChallenges=true. Note
	 * that removing a Context cascades to remove any Challenges still linked
	 * to it (same behavior as deleting a single theme from the Themes page),
	 * so this must run after {@link #deleteUserChallenges(User)}.
	 */
	private void deleteUserContexts(User target) {
		List<Context> contexts = contextRepository.findContextsByCreator(target);
		contextRepository.deleteAll(contexts);
	}

	private void updateData(User newObj, UserRegisterDTO obj) {
		newObj.setName(obj.getName());
		newObj.setEmail(obj.getEmail());
		// Google accounts keep password = null; this endpoint never sets/overwrites
		// a password for them. Use changePassword() for local-account password changes.
		if (!newObj.isGoogleAccount()) {
			newObj.setPassword(obj.getPassword());
		}
	}

}