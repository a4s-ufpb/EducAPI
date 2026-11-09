package br.ufpb.dcx.apps4society.educapi.services;

import br.ufpb.dcx.apps4society.educapi.domain.AcaoAuditoria;
import br.ufpb.dcx.apps4society.educapi.domain.User;
import br.ufpb.dcx.apps4society.educapi.dto.user.UserLoginDTO;
import br.ufpb.dcx.apps4society.educapi.repositories.UserRepository;
import br.ufpb.dcx.apps4society.educapi.response.LoginResponse;
import br.ufpb.dcx.apps4society.educapi.services.exceptions.InvalidUserException;
import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTVerificationException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.validator.routines.EmailValidator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Optional;

@Service
public class JWTService {

    /**
     * Length of the "Bearer " prefix in the Authorization header,
     * used to extract the raw token substring.
     */
    public static final int TOKEN_INDEX = 7;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private LogAuditoriaService logAuditoriaService;

    @Value("${app.token.key}")
    private String TOKEN_KEY;

    // Não precisa mais de google-api-client — validamos via tokeninfo
    @Value("${google.client-id}")
    private String GOOGLE_CLIENT_ID;

    public JWTService(UserRepository userRepository, LogAuditoriaService logAuditoriaService) {
        this.userRepository = userRepository;
        this.logAuditoriaService = logAuditoriaService;
    }

    public LoginResponse authenticate(UserLoginDTO userLoginDTO) throws InvalidUserException {
        Optional<User> userOptional = userRepository.findByEmailAndPassword(
                userLoginDTO.getEmail(), userLoginDTO.getPassword());
        if (userOptional.isEmpty()) {
            throw new InvalidUserException();
        }

        User user = userOptional.get();
        logAuditoriaService.registrar(user, AcaoAuditoria.LOGIN, "User", user.getId(),
                "login local (email/senha), role=" + user.getRole());

        return new LoginResponse(generateToken(userLoginDTO.getEmail()));
    }

    /**
     * Valida o access_token do Google via endpoint tokeninfo (sem biblioteca extra).
     * Cria o usuário automaticamente se for o primeiro acesso.
     */
    public LoginResponse authenticateWithGoogle(String accessToken) throws InvalidUserException {
        try {
            // Chama o endpoint público do Google para validar o access_token
            HttpClient httpClient = HttpClient.newHttpClient();
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create("https://www.googleapis.com/oauth2/v3/userinfo"))
                    .header("Authorization", "Bearer " + accessToken)
                    .GET()
                    .build();

            HttpResponse<String> response = httpClient.send(request,
                    HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() != 200) {
                throw new InvalidUserException("Token do Google inválido ou expirado.");
            }

            // Extrai email e nome do JSON retornado
            ObjectMapper mapper = new ObjectMapper();
            JsonNode json = mapper.readTree(response.body());

            String email = json.path("email").asText(null);
            String name  = json.path("name").asText("Usuário Google");

            if (email == null || email.isBlank()) {
                throw new InvalidUserException("Não foi possível obter o e-mail da conta Google.");
            }

            // Verifica se o e-mail foi confirmado pelo Google
            boolean emailVerified = json.path("email_verified").asBoolean(false);
            if (!emailVerified) {
                throw new InvalidUserException("O e-mail da conta Google não está verificado.");
            }

            // Busca ou cria o usuário no banco
            Optional<User> userOpt = userRepository.findByEmail(email);
            User user;
            if (userOpt.isEmpty()) {
                user = new User(name, email, null);
                user = userRepository.save(user);
            } else {
                user = userOpt.get();
            }

            logAuditoriaService.registrar(user, AcaoAuditoria.LOGIN, "User", user.getId(),
                    "login via Google, role=" + user.getRole());

            return new LoginResponse(generateToken(email));

        } catch (InvalidUserException e) {
            throw e;
        } catch (Exception e) {
            throw new InvalidUserException("Falha ao autenticar com Google: " + e.getMessage());
        }
    }

    private String generateToken(String email) {
        Algorithm algorithm = Algorithm.HMAC256(TOKEN_KEY.getBytes());
        return JWT.create()
                .withSubject(email)
                .withExpiresAt(expirationToken())
                .sign(algorithm)
                .strip();
    }

    // Mesmo bug que já existia em LogAuditoria: LocalDateTime.now() sem fuso
    // pega o relógio de parede no fuso padrão do container (UTC, não
    // America/Sao_Paulo). Aqui esse horário UTC era então rotulado como se
    // já estivesse em -03:00 ao converter para Instant, o que soma 3 horas
    // erradas ao instante real — o token acaba expirando ~4h depois do
    // login em vez de 1h. Corrigido usando Instant.now() (sempre UTC real,
    // sem depender do fuso do container) e somando a duração diretamente.
    private Instant expirationToken() {
        return Instant.now().plus(1, ChronoUnit.HOURS);
    }

    public Optional<String> recoverUser(String header) {
        if (header == null || !header.startsWith("Bearer ")) {
            throw new SecurityException();
        }

        String token = header.substring(TOKEN_INDEX);
        String subject;

        try {
            Algorithm algorithm = Algorithm.HMAC256(TOKEN_KEY.getBytes());
            subject = JWT.require(algorithm).build().verify(token).getSubject();
            if (!emailValidator(subject)) {
                return Optional.empty();
            }
        } catch (JWTVerificationException error) {
            throw new SecurityException("Token invalid or expired!");
        }

        return Optional.of(subject);
    }

    public String tokenBearerFormat(String token) {
        return "Bearer " + token;
    }

    public boolean emailValidator(String email) {
        return EmailValidator.getInstance().isValid(email);
    }
}
