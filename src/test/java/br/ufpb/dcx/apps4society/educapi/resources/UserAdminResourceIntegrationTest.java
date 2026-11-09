package br.ufpb.dcx.apps4society.educapi.resources;

import br.ufpb.dcx.apps4society.educapi.EducApiApplicationTests;
import br.ufpb.dcx.apps4society.educapi.domain.Role;
import br.ufpb.dcx.apps4society.educapi.domain.User;
import br.ufpb.dcx.apps4society.educapi.repositories.UserRepository;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static io.restassured.RestAssured.*;

/**
 * Integration tests (Fase 7) for the Fase 5/6 administrative endpoints:
 * - PUT /admin/users/{id}/promote
 * - DELETE /admin/users/{id}
 * and, as a bonus, the Fase 3 admin log endpoint (GET /admin/logs), which
 * had no integration coverage yet.
 *
 * Since there is no API path to create the very first ADMIN/SYSADMIN
 * account (that's exactly the problem these endpoints solve), each test
 * registers regular CLIENTE users through the public API/login flow and
 * then reaches into the UserRepository directly to bump their Role. The
 * JwtAuthenticationFilter re-reads the User (and its Role) from the
 * database on every request, so this is enough to simulate an
 * already-provisioned ADMIN/SYSADMIN account without bypassing any of the
 * actual authorization logic under test.
 */
public class UserAdminResourceIntegrationTest extends EducApiApplicationTests {

    @Autowired
    private UserRepository userRepository;

    private final List<String> createdEmails = new ArrayList<>();

    @AfterEach
    public void cleanUp() {
        for (String email : createdEmails) {
            userRepository.findByEmail(email).ifPresent(u -> userRepository.deleteById(u.getId()));
        }
        createdEmails.clear();
    }

    private String registerAndLogin(String email, Role role) {
        String body = "{\"name\":\"" + email + "\",\"email\":\"" + email + "\",\"password\":\"12345678\"}";

        given().body(body).contentType(ContentType.JSON)
                .when().post(baseURI + ":" + port + basePath + "users");

        createdEmails.add(email);

        if (role != Role.CLIENTE) {
            Optional<User> userOptional = userRepository.findByEmail(email);
            User user = userOptional.orElseThrow();
            user.setRole(role);
            userRepository.save(user);
        }

        return given().body(body).contentType(ContentType.JSON)
                .when().post(baseURI + ":" + port + basePath + "auth/login")
                .then().extract().path("token");
    }

    private String authHeader(String token) {
        return "Bearer " + token;
    }

    // ---- promote ----

    @Test
    public void promoteBySysAdmin_shouldReturn200AndSetRoleAdminTest() {

        String sysAdminToken = registerAndLogin("sysadmin.promote1@educapi.com", Role.SYSADMIN);
        String clienteToken = registerAndLogin("cliente.promote1@educapi.com", Role.CLIENTE);

        Long clienteId = userRepository.findByEmail("cliente.promote1@educapi.com").orElseThrow().getId();

        given()
                .header("Authorization", authHeader(sysAdminToken))
                .contentType(ContentType.JSON)
                .when()
                .put(baseURI + ":" + port + basePath + "admin/users/" + clienteId + "/promote")
                .then()
                .assertThat().statusCode(200)
                .body("role", org.hamcrest.Matchers.equalTo("ADMIN"));

        Role updatedRole = userRepository.findByEmail("cliente.promote1@educapi.com").orElseThrow().getRole();
        org.junit.jupiter.api.Assertions.assertEquals(Role.ADMIN, updatedRole);
    }

    @Test
    public void promoteByAdmin_shouldReturn403Test() {

        String adminToken = registerAndLogin("admin.promote2@educapi.com", Role.ADMIN);
        registerAndLogin("cliente.promote2@educapi.com", Role.CLIENTE);
        Long clienteId = userRepository.findByEmail("cliente.promote2@educapi.com").orElseThrow().getId();

        given()
                .header("Authorization", authHeader(adminToken))
                .contentType(ContentType.JSON)
                .when()
                .put(baseURI + ":" + port + basePath + "admin/users/" + clienteId + "/promote")
                .then()
                .assertThat().statusCode(403);
    }

    @Test
    public void promoteByCliente_shouldReturn403Test() {

        String clienteToken = registerAndLogin("cliente.promote3@educapi.com", Role.CLIENTE);
        registerAndLogin("cliente.promote3b@educapi.com", Role.CLIENTE);
        Long targetId = userRepository.findByEmail("cliente.promote3b@educapi.com").orElseThrow().getId();

        given()
                .header("Authorization", authHeader(clienteToken))
                .contentType(ContentType.JSON)
                .when()
                .put(baseURI + ":" + port + basePath + "admin/users/" + targetId + "/promote")
                .then()
                .assertThat().statusCode(403);
    }

    @Test
    public void promoteTargetAlreadyAdmin_shouldReturn409Test() {

        String sysAdminToken = registerAndLogin("sysadmin.promote4@educapi.com", Role.SYSADMIN);
        registerAndLogin("admin.promote4@educapi.com", Role.ADMIN);
        Long targetId = userRepository.findByEmail("admin.promote4@educapi.com").orElseThrow().getId();

        given()
                .header("Authorization", authHeader(sysAdminToken))
                .contentType(ContentType.JSON)
                .when()
                .put(baseURI + ":" + port + basePath + "admin/users/" + targetId + "/promote")
                .then()
                .assertThat().statusCode(409);
    }

    @Test
    public void promoteTargetNotFound_shouldReturn404Test() {

        String sysAdminToken = registerAndLogin("sysadmin.promote5@educapi.com", Role.SYSADMIN);

        given()
                .header("Authorization", authHeader(sysAdminToken))
                .contentType(ContentType.JSON)
                .when()
                .put(baseURI + ":" + port + basePath + "admin/users/999999/promote")
                .then()
                .assertThat().statusCode(404);
    }

    // ---- deleteByAdmin ----

    @Test
    public void deleteByAdminBySysAdmin_targetAdmin_shouldReturn200Test() {

        String sysAdminToken = registerAndLogin("sysadmin.del1@educapi.com", Role.SYSADMIN);
        registerAndLogin("admin.del1@educapi.com", Role.ADMIN);
        Long targetId = userRepository.findByEmail("admin.del1@educapi.com").orElseThrow().getId();

        given()
                .header("Authorization", authHeader(sysAdminToken))
                .contentType(ContentType.JSON)
                .when()
                .delete(baseURI + ":" + port + basePath + "admin/users/" + targetId)
                .then()
                .assertThat().statusCode(200);

        org.junit.jupiter.api.Assertions.assertTrue(userRepository.findByEmail("admin.del1@educapi.com").isEmpty());
    }

    @Test
    public void deleteByAdminByAdmin_shouldReturn403Test() {
        // ADMIN's scope was narrowed to theme/challenge moderation only; user
        // management (including deleting CLIENTE accounts) is now exclusive to SYSADMIN.

        String adminToken = registerAndLogin("admin.del2@educapi.com", Role.ADMIN);
        registerAndLogin("cliente.del2@educapi.com", Role.CLIENTE);
        Long targetId = userRepository.findByEmail("cliente.del2@educapi.com").orElseThrow().getId();

        given()
                .header("Authorization", authHeader(adminToken))
                .contentType(ContentType.JSON)
                .when()
                .delete(baseURI + ":" + port + basePath + "admin/users/" + targetId)
                .then()
                .assertThat().statusCode(403);
    }

    @Test
    public void deleteByAdminByAdmin_targetAdmin_shouldReturn403Test() {

        String adminToken = registerAndLogin("admin.del3@educapi.com", Role.ADMIN);
        registerAndLogin("admin.del3b@educapi.com", Role.ADMIN);
        Long targetId = userRepository.findByEmail("admin.del3b@educapi.com").orElseThrow().getId();

        given()
                .header("Authorization", authHeader(adminToken))
                .contentType(ContentType.JSON)
                .when()
                .delete(baseURI + ":" + port + basePath + "admin/users/" + targetId)
                .then()
                .assertThat().statusCode(403);
    }

    @Test
    public void deleteByAdminSelfTarget_shouldReturn400Test() {

        String sysAdminToken = registerAndLogin("sysadmin.del4@educapi.com", Role.SYSADMIN);
        Long selfId = userRepository.findByEmail("sysadmin.del4@educapi.com").orElseThrow().getId();

        given()
                .header("Authorization", authHeader(sysAdminToken))
                .contentType(ContentType.JSON)
                .when()
                .delete(baseURI + ":" + port + basePath + "admin/users/" + selfId)
                .then()
                .assertThat().statusCode(400);
    }

    @Test
    public void deleteByAdminByCliente_shouldReturn403Test() {

        String clienteToken = registerAndLogin("cliente.del5@educapi.com", Role.CLIENTE);
        registerAndLogin("cliente.del5b@educapi.com", Role.CLIENTE);
        Long targetId = userRepository.findByEmail("cliente.del5b@educapi.com").orElseThrow().getId();

        given()
                .header("Authorization", authHeader(clienteToken))
                .contentType(ContentType.JSON)
                .when()
                .delete(baseURI + ":" + port + basePath + "admin/users/" + targetId)
                .then()
                .assertThat().statusCode(403);
    }

    // ---- GET /admin/logs (SYSADMIN-only; ADMIN's scope is limited to theme/challenge moderation) ----

    @Test
    public void getLogsAsSysAdmin_shouldReturn200Test() {

        String sysAdminToken = registerAndLogin("sysadmin.logs1@educapi.com", Role.SYSADMIN);

        given()
                .header("Authorization", authHeader(sysAdminToken))
                .contentType(ContentType.JSON)
                .when()
                .get(baseURI + ":" + port + basePath + "admin/logs")
                .then()
                .assertThat().statusCode(200);
    }

    @Test
    public void getLogsAsAdmin_shouldReturn403Test() {

        String adminToken = registerAndLogin("admin.logs1@educapi.com", Role.ADMIN);

        given()
                .header("Authorization", authHeader(adminToken))
                .contentType(ContentType.JSON)
                .when()
                .get(baseURI + ":" + port + basePath + "admin/logs")
                .then()
                .assertThat().statusCode(403);
    }

    @Test
    public void getLogsAsCliente_shouldReturn403Test() {

        String clienteToken = registerAndLogin("cliente.logs1@educapi.com", Role.CLIENTE);

        given()
                .header("Authorization", authHeader(clienteToken))
                .contentType(ContentType.JSON)
                .when()
                .get(baseURI + ":" + port + basePath + "admin/logs")
                .then()
                .assertThat().statusCode(403);
    }
}
