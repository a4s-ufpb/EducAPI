# Arquitetura

## Estrutura geral do projeto

O projeto segue uma organizacao comum em APIs Spring Boot:

```text
src/main/java/br/ufpb/dcx/apps4society/educapi
├── config
├── domain
├── dto
├── filter
├── repositories
├── resources
├── response
├── services
├── util
└── EducAPIApplication.java
```

Tambem existem testes em:

```text
src/test/java/br/ufpb/dcx/apps4society/educapi
```

## Camadas

### resources/controllers

Ficam no pacote `resources`.

Sao as classes que recebem as requisicoes HTTP, definem as rotas e chamam os services. No codigo atual existem:

- `UserResource`
- `LoginResource`
- `ContextResource`
- `ChallengeResource`
- `UploadResource`

Essas classes usam anotacoes como `@RestController`, `@RequestMapping`, `@GetMapping`, `@PostMapping`, `@PutMapping` e `@DeleteMapping`.

### services

Ficam no pacote `services`.

Concentram as regras de negocio e coordenam o acesso aos repositorios, validacao de token e upload de imagens. No codigo atual existem:

- `UserService`
- `JWTService`
- `ContextService`
- `ChallengeService`
- `UploadImageService`
- `DBService`

### repositories

Ficam no pacote `repositories`.

Sao interfaces Spring Data JPA responsaveis pelo acesso ao banco de dados. Elas estendem `JpaRepository`.

Repositorios existentes:

- `UserRepository`
- `ContextRepository`
- `ChallengeRepository`

### domain/entities

Ficam no pacote `domain`.

Representam as entidades JPA persistidas no banco:

- `User`
- `Context`
- `Challenge`

Principais relacoes identificadas:

- `User` possui varios `Context`.
- `User` possui varios `Challenge`.
- `Context` e `Challenge` possuem relacao muitos-para-muitos.

### DTOs

Ficam no pacote `dto`.

Sao objetos usados para entrada e saida de dados da API:

- `UserRegisterDTO`
- `UserLoginDTO`
- `UserDTO`
- `ContextRegisterDTO`
- `ContextDTO`
- `ChallengeRegisterDTO`

Os DTOs tambem possuem validacoes com `jakarta.validation`, como `@NotEmpty`, `@Email` e `@Length`.

### exceptions

Existem excecoes de servico no pacote `services.exceptions`:

- `UserAlreadyExistsException`
- `ObjectNotFoundException`
- `InvalidUserException`
- `DataIntegrityException`

Existe tratamento de excecoes HTTP no pacote `resources.exceptions`:

- `ResourceExceptionHandler`
- `StandardError`
- `ValidationError`
- `FieldMessage`

### builders/tests

Existem testes em `src/test/java`.

Ha testes de servico e integracao para usuarios, contextos e desafios. Tambem existem builders e utilitarios de teste:

- `UserBuilder`
- `ContextBuilder`
- `ChallengeBuilder`
- `ServicesBuilder`
- utilitarios de requisicao para User, Context e Challenge

## Fluxo de uma requisicao

O fluxo geral identificado no codigo e:

1. O cliente chama um endpoint em uma classe `Resource`.
2. O `Resource` recebe parametros, body, path variables e headers.
3. O `Resource` chama um metodo de `Service`.
4. O `Service` valida dados e, quando necessario, valida o token via `JWTService`.
5. O `Service` acessa o banco usando um `Repository`.
6. O `Service` retorna uma entidade ou DTO.
7. O `Resource` monta o `ResponseEntity` com o status HTTP adequado.

No caso de upload de imagem:

1. O `Resource` recebe `multipart/form-data`.
2. O service valida o usuario.
3. O arquivo e enviado para o MinIO por `UploadImageService`.
4. A URL retornada e salva em `imageUrl`.

## Autenticacao por token

A autenticacao usa JWT com a biblioteca `java-jwt`.

O login e feito em:

```http
POST /v1/api/auth/login
```

O body contem email e senha. Se as credenciais forem validas, a API retorna um token.

Os endpoints protegidos usam rotas com `auth` no caminho, por exemplo:

```http
GET /v1/api/auth/users
POST /v1/api/auth/contexts
POST /v1/api/auth/challenges/{idContext}
```

Esses endpoints esperam o header:

```http
Authorization: Bearer <token>
```

O `JWTService.recoverUser` valida o token, extrai o email do usuario e consulta o usuario no banco quando necessario.

Existe uma classe `TokenFilter`, que valida headers `Authorization` no formato Bearer. Não identificado no código atual uma configuracao de seguranca registrando explicitamente esse filtro no pipeline do Spring.

## Observacoes

- A protecao dos endpoints acontece principalmente dentro dos services, por chamadas manuais ao `JWTService`.
- A classe `TokenFilter` existe, mas seu registro em uma configuracao de seguranca foi marcado como: não identificado no código atual.
- O projeto usa `@Autowired` e tambem construtores em alguns services; ha uma mistura de estilos de injecao.
- Os endpoints protegidos sao identificados pelo caminho `auth`, mas o uso de Spring Security para proteger rotas por padrao foi marcado como: não identificado no código atual.
