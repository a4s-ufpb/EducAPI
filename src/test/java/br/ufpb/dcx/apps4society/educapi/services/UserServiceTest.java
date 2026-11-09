package br.ufpb.dcx.apps4society.educapi.services;

import br.ufpb.dcx.apps4society.educapi.domain.AcaoAuditoria;
import br.ufpb.dcx.apps4society.educapi.domain.Challenge;
import br.ufpb.dcx.apps4society.educapi.domain.Context;
import br.ufpb.dcx.apps4society.educapi.domain.Role;
import br.ufpb.dcx.apps4society.educapi.domain.User;
import br.ufpb.dcx.apps4society.educapi.dto.user.UserDTO;
import br.ufpb.dcx.apps4society.educapi.dto.user.UserLoginDTO;
import br.ufpb.dcx.apps4society.educapi.dto.user.UserRegisterDTO;
import br.ufpb.dcx.apps4society.educapi.repositories.ChallengeRepository;
import br.ufpb.dcx.apps4society.educapi.repositories.ContextRepository;
import br.ufpb.dcx.apps4society.educapi.repositories.UserRepository;
import br.ufpb.dcx.apps4society.educapi.response.LoginResponse;
import br.ufpb.dcx.apps4society.educapi.services.exceptions.InsufficientPrivilegeException;
import br.ufpb.dcx.apps4society.educapi.services.exceptions.InvalidUserException;
import br.ufpb.dcx.apps4society.educapi.services.exceptions.ObjectNotFoundException;
import br.ufpb.dcx.apps4society.educapi.services.exceptions.SelfActionNotAllowedException;
import br.ufpb.dcx.apps4society.educapi.services.exceptions.UserAlreadyExistsException;
import br.ufpb.dcx.apps4society.educapi.services.exceptions.UserAlreadyPromotedException;
import br.ufpb.dcx.apps4society.educapi.utils.builder.ChallengeBuilder;
import br.ufpb.dcx.apps4society.educapi.utils.builder.ContextBuilder;
import br.ufpb.dcx.apps4society.educapi.utils.builder.ServicesBuilder;
import br.ufpb.dcx.apps4society.educapi.utils.builder.UserBuilder;
import br.ufpb.dcx.apps4society.educapi.util.Messages;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.data.domain.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.AssertionsForClassTypes.catchThrowableOfType;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
public class UserServiceTest {

    @Mock
    UserRepository userRepository;
    @Mock
    ContextRepository contextRepository;
    @Mock
    ChallengeRepository challengeRepository;
    @Mock
    LogAuditoriaService logAuditoriaService;

    @InjectMocks
    JWTService jwtService = ServicesBuilder.anService().withUserRepository(userRepository).buildJwtService();
    @InjectMocks
    UserService userService = ServicesBuilder.anService()
            .withJwtService(jwtService)
            .withUserRepository(userRepository)
            .withContextRepository(contextRepository)
            .withChallengeRepository(challengeRepository)
            .withLogAuditoriaService(logAuditoriaService)
            .buildUserService();

    private final UserLoginDTO userLoginDTO = UserBuilder.anUser().buildUserLoginDTO();    
    private final UserLoginDTO userLoginEmailEmptyDTO = UserBuilder.anUser().withName("User3").withEmail("").buildUserLoginDTO();

    private final Optional<User> userOptional = UserBuilder.anUser().withId(1L).buildOptionalUser();
    private final Optional<User> userEmailEmptyOptional = UserBuilder.anUser().withId(3L).withName("User3").withEmail(userLoginEmailEmptyDTO.getEmail()).buildOptionalUser();    

    private final UserLoginDTO sysAdminLoginDTO = UserBuilder.anUser()
            .withName("SysAdmin").withEmail("sysadmin@educapi.com").buildUserLoginDTO();
    private final Optional<User> sysAdminOptional = UserBuilder.anUser()
            .withId(10L).withName("SysAdmin").withEmail("sysadmin@educapi.com").withRole(Role.SYSADMIN).buildOptionalUser();

    private final UserLoginDTO adminLoginDTO = UserBuilder.anUser()
            .withName("Admin").withEmail("admin@educapi.com").buildUserLoginDTO();
    private final Optional<User> adminOptional = UserBuilder.anUser()
            .withId(20L).withName("Admin").withEmail("admin@educapi.com").withRole(Role.ADMIN).buildOptionalUser();

    private final Optional<User> targetClienteOptional = UserBuilder.anUser()
            .withId(30L).withName("Cliente").withEmail("cliente@educapi.com").buildOptionalUser();
    private final Optional<User> targetAdminOptional = UserBuilder.anUser()
            .withId(40L).withName("Admin2").withEmail("admin2@educapi.com").withRole(Role.ADMIN).buildOptionalUser();

    private void authenticateAs(UserLoginDTO loginDTO, Optional<User> userOpt) {
        Mockito.lenient().when(userRepository.findByEmail(loginDTO.getEmail())).thenReturn(userOpt);
        Mockito.lenient().when(userRepository.findByEmailAndPassword(loginDTO.getEmail(), loginDTO.getPassword()))
                .thenReturn(userOpt);
    }

    private final UserRegisterDTO userRegisterDTO = UserBuilder.anUser().buildUserRegisterDTO();
    private final UserRegisterDTO userRegisterDTO2 = UserBuilder.anUser().withName("User2").buildUserRegisterDTO();

    private List<User> users = new ArrayList<>();

    public PageRequest pageable = PageRequest.of(0, 20, Sort.by("name").ascending());
    

    @BeforeEach
    public void setUp(){

        Mockito.lenient().when(userRepository.findByEmail("user@educapi.com")).thenReturn(userOptional);
        Mockito.lenient().when(userRepository.findByEmailAndPassword("user@educapi.com", "testpassword")).thenReturn(userOptional);
        
        ReflectionTestUtils.setField(jwtService, "TOKEN_KEY", "it's a token key");
        
    }

    @Test
    public void findUserTest() throws InvalidUserException, UserAlreadyExistsException{

        LoginResponse loginResponse = jwtService.authenticate(userLoginDTO);
        User user = userService.find(jwtService.tokenBearerFormat(loginResponse.getToken()));

        assertNotNull(user.getName());
        assertNotNull(user.getEmail());
        assertEquals("User", user.getName());
        assertEquals("user@educapi.com", user.getEmail());
        
    }

    @Test
    public void findInvalidUserTest() throws InvalidUserException{

    Mockito.lenient().when(userRepository.findByEmailAndPassword("", "testpassword")).thenReturn(userEmailEmptyOptional);
    
    LoginResponse loginResponse = jwtService.authenticate(userLoginEmailEmptyDTO);

    catchThrowableOfType(() ->
            userService.find(jwtService.tokenBearerFormat(loginResponse.getToken())), InvalidUserException.class);

    assertNotNull(loginResponse.getToken());

    }

    @Test
    public void insertAUserTest() throws UserAlreadyExistsException {

        Mockito.when(userRepository.findByEmail(this.userRegisterDTO.getEmail())).thenReturn(Optional.empty());

        UserDTO response = userService.insert(this.userRegisterDTO);

        assertEquals(response.getName(), this.userRegisterDTO.getName());
        assertEquals(response.getEmail(), this.userRegisterDTO.getEmail());
        assertEquals(response.getPassword(), this.userRegisterDTO.getPassword());
    }

    @Test
    public void insertAUserAlreadyExistTest() {
        
        Mockito.when(this.userRepository.findByEmail(this.userRegisterDTO.getEmail())).thenReturn(this.userOptional);

        Exception exception = assertThrows(UserAlreadyExistsException.class, () -> {
            userService.insert(this.userRegisterDTO);
        });

        assertEquals(Messages.USER_ALREADY_EXISTS, exception.getMessage());
    }

    @Test
    public void updateUserTest() throws InvalidUserException, UserAlreadyExistsException {

        Mockito.when(userRepository.findByEmail(this.userRegisterDTO.getEmail())).thenReturn(Optional.empty());

        LoginResponse loginResponse = jwtService.authenticate(userLoginDTO);
        UserDTO userDTO = userService.insert(this.userRegisterDTO);

        Mockito.when(userRepository.findByEmail(this.userRegisterDTO.getEmail())).thenReturn(userOptional);

        UserDTO userDTO2 = userService.update(jwtService.tokenBearerFormat(loginResponse.getToken()), userRegisterDTO2);

        assertNotNull(userDTO2);
        assertNotEquals(userDTO, userDTO2);

    }

    @Test
    public void deleteUserTest() throws InvalidUserException, UserAlreadyExistsException{

        Mockito.when(userRepository.findByEmail(this.userRegisterDTO.getEmail())).thenReturn(Optional.empty());

        LoginResponse loginResponse = jwtService.authenticate(userLoginDTO);
        UserDTO userDTOResponse = userService.insert(this.userRegisterDTO);

        Mockito.when(userRepository.findByEmail(this.userRegisterDTO.getEmail())).thenReturn(userOptional);

        userService.delete(jwtService.tokenBearerFormat(loginResponse.getToken()), false);

        catchThrowableOfType(() ->
                userService.find(jwtService.tokenBearerFormat(loginResponse.getToken())), InvalidUserException.class);

        assertNotNull(userDTOResponse);

    }

    @Test
    public void findAllUsersTest(){  

        Mockito.when(userRepository.findAll()).thenReturn(users);

        User user = userRegisterDTO.userRegisterDtoToUser();
        User user2 = userRegisterDTO2.userRegisterDtoToUser();

        ServicesBuilder.insertSimulator(user, users);
        ServicesBuilder.insertSimulator(user2, users);

        List<User> usersList = userService.findAll();

        assertEquals(users, usersList);    
        assertEquals(user, usersList.get(0));
        assertEquals(user2, usersList.get(1));    

    }

    @Test
    public void findPageOfUsersTest(){

        User user = userRegisterDTO.userRegisterDtoToUser();
        User user2 = userRegisterDTO2.userRegisterDtoToUser();

        ServicesBuilder.insertSimulator(user, users);
        ServicesBuilder.insertSimulator(user2, users);

        Mockito.lenient().when(userRepository.findAll(pageable)).thenReturn(new PageImpl<>(users, pageable, pageable.getPageSize()));

        Page<User> pageResponse = userService.findPage(pageable.getPageNumber(), pageable.getPageSize(), "name", "ASC");

        assertEquals(pageResponse, userRepository.findAll(pageable));
        assertEquals(user, pageResponse.getContent().get(0));
        assertEquals(user2, pageResponse.getContent().get(1));

    }
        

    // ---- Fase 5/6/7: promote() ----

    @Test
    public void promoteBySysAdmin_shouldPromoteClienteToAdminTest() throws Exception {

        authenticateAs(sysAdminLoginDTO, sysAdminOptional);
        Mockito.when(userRepository.findById(30L)).thenReturn(targetClienteOptional);

        LoginResponse loginResponse = jwtService.authenticate(sysAdminLoginDTO);

        UserDTO result = userService.promote(jwtService.tokenBearerFormat(loginResponse.getToken()), 30L);

        assertEquals(Role.ADMIN, result.getRole());
        assertEquals(Role.ADMIN, targetClienteOptional.get().getRole());
        verify(userRepository).save(targetClienteOptional.get());
        verify(logAuditoriaService).registrar(eq(sysAdminOptional.get()), eq(AcaoAuditoria.PROMOCAO_ADMIN),
                eq("User"), eq(30L), any());
    }

    @Test
    public void promoteByNonSysAdmin_shouldThrowInsufficientPrivilegeTest() throws Exception {

        authenticateAs(adminLoginDTO, adminOptional);

        LoginResponse loginResponse = jwtService.authenticate(adminLoginDTO);
        String token = jwtService.tokenBearerFormat(loginResponse.getToken());

        assertThrows(InsufficientPrivilegeException.class, () -> userService.promote(token, 30L));

        verify(userRepository, never()).save(any());
    }

    @Test
    public void promoteTargetAlreadyAdmin_shouldThrowUserAlreadyPromotedTest() throws Exception {

        authenticateAs(sysAdminLoginDTO, sysAdminOptional);
        Mockito.when(userRepository.findById(40L)).thenReturn(targetAdminOptional);

        LoginResponse loginResponse = jwtService.authenticate(sysAdminLoginDTO);
        String token = jwtService.tokenBearerFormat(loginResponse.getToken());

        assertThrows(UserAlreadyPromotedException.class, () -> userService.promote(token, 40L));

        verify(userRepository, never()).save(any());
    }

    @Test
    public void promoteTargetNotFound_shouldThrowObjectNotFoundTest() throws Exception {

        authenticateAs(sysAdminLoginDTO, sysAdminOptional);
        Mockito.when(userRepository.findById(99L)).thenReturn(Optional.empty());

        LoginResponse loginResponse = jwtService.authenticate(sysAdminLoginDTO);
        String token = jwtService.tokenBearerFormat(loginResponse.getToken());

        assertThrows(ObjectNotFoundException.class, () -> userService.promote(token, 99L));
    }

    // ---- Fase 5/6/7: deleteByAdmin() ----

    @Test
    public void deleteByAdminBySysAdmin_targetAdmin_shouldSucceedAndOrphanContentTest() throws Exception {

        authenticateAs(sysAdminLoginDTO, sysAdminOptional);
        Mockito.when(userRepository.findById(40L)).thenReturn(targetAdminOptional);

        User target = targetAdminOptional.get();
        Context orphanContext = ContextBuilder.anContext().withId(1L).withCreator(target).buildContext();
        Challenge orphanChallenge = ChallengeBuilder.anChallenge().withId(1L).withCreator(target).buildOptionalChallenge().get();

        Mockito.when(contextRepository.findContextsByCreator(target)).thenReturn(List.of(orphanContext));
        Mockito.when(challengeRepository.findChallengesByCreator(target)).thenReturn(List.of(orphanChallenge));

        LoginResponse loginResponse = jwtService.authenticate(sysAdminLoginDTO);
        String token = jwtService.tokenBearerFormat(loginResponse.getToken());

        UserDTO result = userService.deleteByAdmin(token, 40L);

        assertEquals(target.getEmail(), result.getEmail());
        assertNull(orphanContext.getCreator());
        assertNull(orphanChallenge.getCreator());
        verify(contextRepository).save(orphanContext);
        verify(challengeRepository).save(orphanChallenge);
        verify(userRepository).deleteById(40L);
        verify(logAuditoriaService).registrar(eq(sysAdminOptional.get()), eq(AcaoAuditoria.EXCLUSAO_USUARIO),
                eq("User"), eq(40L), any());
    }

    @Test
    public void deleteByAdminByAdmin_targetCliente_shouldSucceedTest() throws Exception {

        authenticateAs(adminLoginDTO, adminOptional);
        Mockito.when(userRepository.findById(30L)).thenReturn(targetClienteOptional);
        Mockito.when(contextRepository.findContextsByCreator(targetClienteOptional.get())).thenReturn(List.of());
        Mockito.when(challengeRepository.findChallengesByCreator(targetClienteOptional.get())).thenReturn(List.of());

        LoginResponse loginResponse = jwtService.authenticate(adminLoginDTO);
        String token = jwtService.tokenBearerFormat(loginResponse.getToken());

        UserDTO result = userService.deleteByAdmin(token, 30L);

        assertEquals(targetClienteOptional.get().getEmail(), result.getEmail());
        verify(userRepository).deleteById(30L);
    }

    @Test
    public void deleteByAdminByAdmin_targetAdmin_shouldThrowInsufficientPrivilegeTest() throws Exception {

        authenticateAs(adminLoginDTO, adminOptional);
        Mockito.when(userRepository.findById(40L)).thenReturn(targetAdminOptional);

        LoginResponse loginResponse = jwtService.authenticate(adminLoginDTO);
        String token = jwtService.tokenBearerFormat(loginResponse.getToken());

        assertThrows(InsufficientPrivilegeException.class, () -> userService.deleteByAdmin(token, 40L));

        verify(userRepository, never()).deleteById(any());
    }

    @Test
    public void deleteByAdminByNonAdmin_shouldThrowInsufficientPrivilegeTest() throws Exception {

        // userOptional (id=1) is a plain CLIENTE, targeting a different user (id=30)
        Mockito.when(userRepository.findById(30L)).thenReturn(targetClienteOptional);

        LoginResponse loginResponse = jwtService.authenticate(userLoginDTO);
        String token = jwtService.tokenBearerFormat(loginResponse.getToken());

        assertThrows(InsufficientPrivilegeException.class, () -> userService.deleteByAdmin(token, 30L));

        verify(userRepository, never()).deleteById(any());
    }

    @Test
    public void deleteByAdminSelfTarget_shouldThrowSelfActionNotAllowedTest() throws Exception {

        authenticateAs(sysAdminLoginDTO, sysAdminOptional);
        Mockito.when(userRepository.findById(10L)).thenReturn(sysAdminOptional);

        LoginResponse loginResponse = jwtService.authenticate(sysAdminLoginDTO);
        String token = jwtService.tokenBearerFormat(loginResponse.getToken());

        assertThrows(SelfActionNotAllowedException.class, () -> userService.deleteByAdmin(token, 10L));

        verify(userRepository, never()).deleteById(any());
    }

    // ---- Fase 5/7: self delete() now orphans content and logs ----

    @Test
    public void deleteUserSelf_shouldOrphanContentAndLogTest() throws Exception {

        User self = userOptional.get();
        Context ownContext = ContextBuilder.anContext().withId(1L).withCreator(self).buildContext();
        Challenge ownChallenge = ChallengeBuilder.anChallenge().withId(1L).withCreator(self).buildOptionalChallenge().get();

        Mockito.when(contextRepository.findContextsByCreator(self)).thenReturn(List.of(ownContext));
        Mockito.when(challengeRepository.findChallengesByCreator(self)).thenReturn(List.of(ownChallenge));

        LoginResponse loginResponse = jwtService.authenticate(userLoginDTO);
        String token = jwtService.tokenBearerFormat(loginResponse.getToken());

        userService.delete(token, false);

        assertNull(ownContext.getCreator());
        assertNull(ownChallenge.getCreator());
        verify(contextRepository).save(ownContext);
        verify(challengeRepository).save(ownChallenge);
        verify(userRepository).deleteById(1L);
        verify(logAuditoriaService).registrar(eq(self), eq(AcaoAuditoria.EXCLUSAO_USUARIO), eq("User"), eq(1L), any());
    }

    @Test
    public void deleteUserSelf_withDeleteChallengesTrue_shouldDeleteContentAndLogTest() throws Exception {

        User self = userOptional.get();
        Context ownContext = ContextBuilder.anContext().withId(1L).withCreator(self).buildContext();
        Challenge ownChallenge = ChallengeBuilder.anChallenge().withId(1L).withCreator(self).buildOptionalChallenge().get();

        Mockito.when(contextRepository.findContextsByCreator(self)).thenReturn(List.of(ownContext));
        Mockito.when(challengeRepository.findChallengesByCreator(self)).thenReturn(List.of(ownChallenge));

        LoginResponse loginResponse = jwtService.authenticate(userLoginDTO);
        String token = jwtService.tokenBearerFormat(loginResponse.getToken());

        userService.delete(token, true);

        verify(challengeRepository).deleteAll(List.of(ownChallenge));
        verify(contextRepository).deleteAll(List.of(ownContext));
        verify(contextRepository, never()).save(any());
        verify(challengeRepository, never()).save(any());
        verify(userRepository).deleteById(1L);
        verify(logAuditoriaService).registrar(eq(self), eq(AcaoAuditoria.EXCLUSAO_USUARIO), eq("User"), eq(1L), any());
    }

}
