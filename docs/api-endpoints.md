# Endpoints da API

Base path identificado nos controllers:

```text
/v1/api/
```

Endpoints com `auth` no caminho exigem header:

```http
Authorization: Bearer <token>
```

## User

### Criar usuario

- Metodo: `POST`
- Rota: `/v1/api/users`
- Authorization: nao
- Body: JSON
- Objetivo: cadastrar um novo usuario.

Exemplo:

```http
POST /v1/api/users
Content-Type: application/json

{
  "name": "Maria",
  "email": "maria@example.com",
  "password": "12345678"
}
```

### Buscar usuario autenticado

- Metodo: `GET`
- Rota: `/v1/api/auth/users`
- Authorization: sim
- Body: nenhum
- Objetivo: retornar o usuario associado ao token.

Exemplo:

```http
GET /v1/api/auth/users
Authorization: Bearer <token>
```

### Atualizar usuario autenticado

- Metodo: `PUT`
- Rota: `/v1/api/auth/users`
- Authorization: sim
- Body: JSON
- Objetivo: atualizar os dados do usuario associado ao token.

Exemplo:

```http
PUT /v1/api/auth/users
Authorization: Bearer <token>
Content-Type: application/json

{
  "name": "Maria Silva",
  "email": "maria@example.com",
  "password": "12345678"
}
```

### Remover usuario autenticado

- Metodo: `DELETE`
- Rota: `/v1/api/auth/users`
- Authorization: sim
- Body: nenhum
- Objetivo: remover o usuario associado ao token.

Exemplo:

```http
DELETE /v1/api/auth/users
Authorization: Bearer <token>
```

## Auth/Login

### Login

- Metodo: `POST`
- Rota: `/v1/api/auth/login`
- Authorization: nao
- Body: JSON
- Objetivo: autenticar usuario e retornar token JWT.

Exemplo:

```http
POST /v1/api/auth/login
Content-Type: application/json

{
  "email": "maria@example.com",
  "password": "12345678"
}
```

## Context

### Buscar contexto por ID

- Metodo: `GET`
- Rota: `/v1/api/contexts/{idContext}`
- Authorization: nao
- Body: nenhum
- Objetivo: retornar um contexto pelo ID.

Exemplo:

```http
GET /v1/api/contexts/1
```

### Listar contextos

- Metodo: `GET`
- Rota: `/v1/api/contexts`
- Authorization: nao
- Body: nenhum
- Objetivo: retornar uma pagina de contextos.
- Query params: `email`, `name`, `size`, `page`.

Exemplo:

```http
GET /v1/api/contexts?name=animais&page=0&size=20
```

### Criar contexto

- Metodo: `POST`
- Rota: `/v1/api/auth/contexts`
- Authorization: sim
- Body: `multipart/form-data`
- Objetivo: criar um contexto para o usuario autenticado, com possibilidade de enviar imagem.

Campos esperados:

- `name`
- `imageUrl`
- `soundUrl`
- `videoUrl`
- `file`

Exemplo com `curl`:

```bash
curl -X POST http://localhost:8080/v1/api/auth/contexts \
  -H "Authorization: Bearer <token>" \
  -F "name=Animais" \
  -F "imageUrl=" \
  -F "soundUrl=" \
  -F "videoUrl=" \
  -F "file=@imagem.png"
```

### Atualizar contexto

- Metodo: `PUT`
- Rota: `/v1/api/auth/contexts/{idContext}`
- Authorization: sim
- Body: `multipart/form-data`
- Objetivo: atualizar um contexto do usuario autenticado, com possibilidade de enviar nova imagem.

Exemplo com `curl`:

```bash
curl -X PUT http://localhost:8080/v1/api/auth/contexts/1 \
  -H "Authorization: Bearer <token>" \
  -F "name=Animais domesticos" \
  -F "imageUrl=" \
  -F "soundUrl=" \
  -F "videoUrl=" \
  -F "file=@imagem.png"
```

### Remover contexto

- Metodo: `DELETE`
- Rota: `/v1/api/auth/contexts/{idContext}`
- Authorization: sim
- Body: nenhum
- Objetivo: remover um contexto do usuario autenticado.

Exemplo:

```http
DELETE /v1/api/auth/contexts/1
Authorization: Bearer <token>
```

### Listar contextos do usuario autenticado

- Metodo: `GET`
- Rota: `/v1/api/auth/contexts`
- Authorization: sim
- Body: nenhum
- Objetivo: retornar contextos criados pelo usuario autenticado.

Exemplo:

```http
GET /v1/api/auth/contexts
Authorization: Bearer <token>
```

## Challenge

### Buscar desafio por ID

- Metodo: `GET`
- Rota: `/v1/api/auth/challenges/{idChallenge}`
- Authorization: sim
- Body: nenhum
- Objetivo: retornar um desafio pelo ID.

Exemplo:

```http
GET /v1/api/auth/challenges/1
Authorization: Bearer <token>
```

### Criar desafio em um contexto

- Metodo: `POST`
- Rota: `/v1/api/auth/challenges/{idContext}`
- Authorization: sim
- Body: JSON
- Objetivo: criar um desafio associado ao contexto informado.

Exemplo:

```http
POST /v1/api/auth/challenges/1
Authorization: Bearer <token>
Content-Type: application/json

{
  "word": "gato",
  "imageUrl": "http://example.com/gato.png",
  "soundUrl": "",
  "videoUrl": ""
}
```

### Atualizar desafio

- Metodo: `PUT`
- Rota: `/v1/api/auth/challenges/{idChallenge}`
- Authorization: sim
- Body: JSON
- Objetivo: atualizar um desafio do usuario autenticado.

Exemplo:

```http
PUT /v1/api/auth/challenges/1
Authorization: Bearer <token>
Content-Type: application/json

{
  "word": "cachorro",
  "imageUrl": "http://example.com/cachorro.png",
  "soundUrl": "",
  "videoUrl": ""
}
```

### Remover desafio

- Metodo: `DELETE`
- Rota: `/v1/api/auth/challenges/{idChallenge}`
- Authorization: sim
- Body: nenhum
- Objetivo: remover um desafio do usuario autenticado.

Exemplo:

```http
DELETE /v1/api/auth/challenges/1
Authorization: Bearer <token>
```

### Listar desafios do usuario autenticado

- Metodo: `GET`
- Rota: `/v1/api/auth/challenges`
- Authorization: sim
- Body: nenhum
- Objetivo: retornar desafios criados pelo usuario autenticado.

Exemplo:

```http
GET /v1/api/auth/challenges
Authorization: Bearer <token>
```

### Listar desafios

- Metodo: `GET`
- Rota: `/v1/api/challenges`
- Authorization: nao
- Body: nenhum
- Objetivo: retornar uma pagina de desafios.
- Query params: `word`, `size`, `page`.

Exemplo:

```http
GET /v1/api/challenges?word=ga&page=0&size=20
```

## Upload

### Upload generico de imagem

- Metodo: `POST`
- Rota: `/v1/api/auth/upload`
- Authorization: sim
- Body: `multipart/form-data`
- Objetivo: enviar uma imagem PNG ou JPEG para o MinIO e retornar a URL gerada.

Campo esperado:

- `file`

Exemplo com `curl`:

```bash
curl -X POST http://localhost:8080/v1/api/auth/upload \
  -H "Authorization: Bearer <token>" \
  -F "file=@imagem.png"
```

Resposta esperada:

```json
{
  "url": "http://localhost:9000/<bucket>/uploads/<timestamp>_imagem.png"
}
```

## Observacoes

- Os parametros `size` e `page` aparecem nos controllers de listagem, mas o metodo tambem recebe `Pageable`. No codigo atual, quem e passado ao service e o `Pageable`.
- O endpoint generico de upload valida tipo e tamanho do arquivo. O upload integrado em Context nao possui as mesmas validacoes no controller/service.
- Challenge ainda usa JSON com `imageUrl`; não identificado no código atual upload multipart integrado para Challenge.
