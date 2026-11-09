# Desenvolvimento

## Como rodar com Docker Compose

Na raiz do projeto, crie ou configure o arquivo `.env` com as variaveis necessarias. Existe um `.env.example` com os nomes esperados.

Depois execute:

```bash
docker compose up --build
```

Dependendo da versao instalada do Docker Compose, tambem pode ser usado:

```bash
docker-compose up --build
```

A API fica exposta em:

```text
http://localhost:8080
```

Para parar os containers:

```bash
docker compose down
```

## Servicos no Docker

O `docker-compose.yml` define os seguintes servicos:

### educapi

Container da aplicacao Spring Boot.

- Porta: `8080:8080`
- Usa variaveis de ambiente do arquivo `.env`
- Depende do servico `db`
- Build feito a partir do `Dockerfile`

### db

Banco PostgreSQL.

- Imagem: `postgres:12.3`
- Porta: `5432:5432`
- Volume: `postgres-educapi`
- Variaveis: `POSTGRES_USER`, `POSTGRES_PASSWORD`, `POSTGRES_DB`

### minio

Servico de armazenamento de objetos.

- Imagem: `minio/minio`
- Porta da API: `9000:9000`
- Porta do console: `9001:9001`
- Volume: `minio-data`
- Variaveis: `MINIO_USER`, `MINIO_PASSWORD`

### minio-init

Container auxiliar que usa `minio/mc` para:

- configurar o alias `myminio`;
- criar o bucket definido em `MINIO_BUCKET`;
- liberar download anonimo no bucket.

## Variaveis de ambiente

Variaveis identificadas no projeto:

```text
POSTGRES_USER
POSTGRES_PASSWORD
POSTGRES_DB
DB_URL
DB_USER
DB_PASSWORD
MINIO_USER
MINIO_PASSWORD
MINIO_BUCKET
TOKEN_KEY
PROFILE_ACTIVE
EDUCAPI_VERSION
```

O arquivo `.env.example` atual lista:

```text
POSTGRES_USER=
POSTGRES_PASSWORD=
POSTGRES_DB=

MINIO_USER=
MINIO_PASSWORD=
MINIO_BUCKET=
```

O `application.yml` define:

```text
app.token.key=${TOKEN_KEY:token}
app.version=${EDUCAPI_VERSION:1.0.7}
spring.profiles.active=${PROFILE_ACTIVE:test}
```

Ou seja, se `PROFILE_ACTIVE` nao for informado, o perfil padrao e `test`.

## Cuidados com `.env` e `.env.example`

O arquivo `.env` deve guardar valores reais usados localmente ou no ambiente de execucao.

O arquivo `.env.example` deve servir como modelo, sem segredos reais.

Nao coloque senhas reais, tokens reais ou credenciais de producao em `.env.example`.

## Swagger

O projeto usa springdoc-openapi. Localmente, a documentacao Swagger normalmente fica em:

```text
http://localhost:8080/swagger-ui/index.html
```

O README tambem menciona:

```text
http://localhost:8080/swagger-ui.html
```

Para testar endpoints protegidos no Swagger:

1. Crie um usuario em `POST /v1/api/users`.
2. Faca login em `POST /v1/api/auth/login`.
3. Copie o token retornado.
4. Use a autorizacao Bearer no Swagger.
5. Chame endpoints com rota `/auth/...`.

## Comandos basicos uteis

Rodar com Docker:

```bash
docker compose up --build
```

Parar containers:

```bash
docker compose down
```

Rodar a aplicacao localmente pelo Maven Wrapper:

```bash
./mvnw spring-boot:run
```

No Windows PowerShell:

```powershell
.\mvnw.cmd spring-boot:run
```

Rodar testes:

```bash
./mvnw test
```

Build sem testes, como usado no Dockerfile:

```bash
./mvnw install -DskipTests -Djacoco.skip=true
```

## Observacoes

- O `docker-compose.yml` passa `.env` para `educapi` e `db`, mas o servico `minio` usa variaveis no bloco `environment` e nao declara `env_file`. Ainda assim, o Compose pode interpolar valores vindos do `.env` da raiz.
- O servico `educapi` depende apenas de `db`; `depends_on` para `minio` foi marcado como: não identificado no código atual.
- O perfil padrao e `test`, que usa H2 em arquivo local. Para rodar com PostgreSQL do Docker, e necessario configurar `PROFILE_ACTIVE=dev` e as variaveis esperadas pelo perfil `dev`.
- O `.env.example` nao lista `PROFILE_ACTIVE`, `TOKEN_KEY`, `DB_URL`, `EDUCAPI_VERSION`, `DB_USER` ou `DB_PASSWORD`.
- O README esta parcialmente desatualizado em relacao ao `pom.xml` atual.
