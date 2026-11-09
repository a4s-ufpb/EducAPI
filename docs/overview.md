# Visao Geral

## O que e o EducAPI

O EducAPI e uma API Spring Boot que fornece uma base colaborativa de contextos e desafios para aplicacoes educacionais, especialmente relacionadas a alfabetizacao. O README descreve o projeto como uma API que disponibiliza contextos e desafios em um banco de dados colaborativo.

## Objetivo geral da API

O objetivo geral da API e permitir que usuarios cadastrem, consultem, atualizem e removam contextos e desafios. Cada usuario pode criar seus proprios contextos e desafios, e os dados tambem podem ser consultados por outros fluxos da API.

## Principais modulos e dominios

### User

Representa o usuario do sistema. O usuario possui nome, email e senha. A senha e marcada com `@JsonIgnore` na entidade para nao ser retornada no JSON.

O usuario e o criador de contextos e desafios.

### Auth/Login

Responsavel por autenticar um usuario usando email e senha. Quando a autenticacao e bem-sucedida, a API retorna um token JWT.

Os endpoints protegidos esperam o token no header:

```http
Authorization: Bearer <token>
```

### Context

Representa um conjunto de desafios relacionados. Um contexto possui:

- nome;
- criador;
- `imageUrl`;
- `soundUrl`;
- `videoUrl`;
- `imageBackup`;
- lista de desafios associados.

O cadastro e a atualizacao de contextos aceitam `multipart/form-data`, permitindo enviar os campos do contexto junto com um arquivo de imagem.

### Challenge

Representa um desafio baseado em uma palavra. Um desafio possui:

- palavra;
- criador;
- `imageUrl`;
- `soundUrl`;
- `videoUrl`;
- contextos associados.

No estado atual do codigo, o desafio e criado dentro de um contexto existente.

### Upload/Image/MinIO

O modulo de upload envia imagens para o MinIO. Existe um endpoint generico de upload e tambem existe upload integrado ao cadastro e atualizacao de Context.

O servico `UploadImageService` cria o bucket caso ele nao exista, envia o arquivo para o MinIO e retorna uma URL no formato:

```text
http://localhost:9000/<bucket>/<folder>/<timestamp>_<nome-do-arquivo>
```

## Como os modulos se relacionam

Um `User` pode criar varios `Context` e varios `Challenge`.

Um `Context` pertence a um usuario criador e pode estar associado a varios desafios.

Um `Challenge` pertence a um usuario criador e pode estar associado a varios contextos. A relacao entre `Context` e `Challenge` e muitos-para-muitos.

O modulo de upload nao representa uma entidade propria no dominio. Ele e usado para armazenar arquivos no MinIO e retornar URLs que podem ser salvas em campos como `imageUrl`.

## Fluxo basico de uso

1. Um usuario cria uma conta em `POST /v1/api/users`.
2. O usuario faz login em `POST /v1/api/auth/login`.
3. A API retorna um token JWT.
4. O cliente envia o token nos endpoints protegidos usando `Authorization: Bearer <token>`.
5. O usuario cria contextos em `POST /v1/api/auth/contexts`.
6. O usuario cria desafios associados a um contexto em `POST /v1/api/auth/challenges/{idContext}`.
7. Contextos e desafios podem ser consultados por endpoints publicos ou por endpoints autenticados, dependendo da rota.

## Observacoes

- O codigo nao identifica nenhum mecanismo de criptografia de senha. A autenticacao consulta email e senha diretamente no repositorio.
- O README menciona tecnologias antigas, como Spring Boot 2.2.7, Java 11 e Swagger 2.9.2, mas o `pom.xml` atual usa Spring Boot 3.1.5, Java 17 e springdoc-openapi 2.3.0.
- A documentacao aqui descreve o estado atual do codigo, nao um estado ideal do sistema.
