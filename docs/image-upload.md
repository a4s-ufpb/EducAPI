# Upload de Imagens

## Endpoint generico de upload

O endpoint generico fica em:

```http
POST /v1/api/auth/upload
```

Ele aceita `multipart/form-data` com o campo:

```text
file
```

O endpoint exige token no header:

```http
Authorization: Bearer <token>
```

Fluxo identificado:

1. `UploadResource` recebe o arquivo.
2. `UploadResource` valida o token chamando `userService.find(token)`.
3. O endpoint verifica se o arquivo esta vazio.
4. O endpoint limita o tamanho a 5 MB.
5. O endpoint aceita apenas `image/png` e `image/jpeg`.
6. O arquivo e enviado para o MinIO por `UploadImageService.uploadFile`.
7. A resposta retorna um JSON com a URL gerada.

Exemplo de resposta:

```json
{
  "url": "http://localhost:9000/<bucket>/uploads/<timestamp>_imagem.png"
}
```

## Upload integrado em Context

Os endpoints de criacao e atualizacao de Context usam `multipart/form-data`:

```http
POST /v1/api/auth/contexts
PUT /v1/api/auth/contexts/{idContext}
```

O DTO usado e `ContextRegisterDTO`, recebido por `@ModelAttribute`.

Campos identificados:

- `name`
- `imageUrl`
- `soundUrl`
- `videoUrl`
- `file`
- `imageBackup`

Quando `file` e enviado e nao esta vazio:

1. O usuario e validado pelo token.
2. O contexto e criado ou carregado para atualizacao.
3. O arquivo e enviado para o MinIO.
4. A URL retornada e salva em `context.imageUrl`.
5. Um thumbnail em Base64 e gerado.
6. O Base64 e salvo em `context.imageBackup`.
7. O contexto e salvo no banco.
8. A resposta retorna `ContextDTO`, que contem `id`, `name`, `imageUrl`, `soundUrl` e `videoUrl`.

## `imageUrl`, `file` e `imageBackup`

### `file`

E o arquivo enviado pelo cliente em `multipart/form-data`.

No Context, ele nao e persistido diretamente. Ele serve como entrada para upload no MinIO e para geracao do thumbnail Base64.

### `imageUrl`

E a URL da imagem. Pode vir no DTO como texto, mas quando um `file` e enviado no cadastro ou atualizacao de Context, o codigo substitui `imageUrl` pela URL retornada pelo MinIO.

### `imageBackup`

E uma string Base64 gerada por `UploadImageService.generateBase64Thumbnail`.

No dominio `Context`, o campo e persistido como `TEXT`.

No DTO `ContextRegisterDTO`, o campo possui:

```java
@JsonIgnore
@Schema(hidden = true)
```

Isso indica que ele e interno e nao deve aparecer no JSON nem na documentacao do schema do Swagger.

## Como o `UploadImageService` envia arquivos para o MinIO

O `UploadImageService` cria um `MinioClient` com:

- `minio.url`
- `minio.access-key`
- `minio.secret-key`

O bucket usado vem de:

```text
minio.bucket
```

O metodo `uploadFile` faz:

1. Verifica se o bucket existe.
2. Cria o bucket se ele nao existir.
3. Monta o nome seguro do objeto com folder, timestamp e nome original.
4. Envia o arquivo com `minioClient.putObject`.
5. Retorna uma URL HTTP apontando para o MinIO local.

Formato do objeto:

```text
<folder>/<timestamp>_<nome-original>
```

Formato da URL retornada:

```text
http://localhost:9000/<bucket>/<folder>/<timestamp>_<nome-original>
```

## Organizacao dos folders no bucket

No endpoint generico de upload, o folder usado e:

```text
uploads
```

No upload integrado em Context, o folder usado e:

```text
user_<id-do-usuario>/context_<nome-do-contexto>
```

Exemplo:

```text
user_1/context_Animais/1710000000000_imagem.png
```

No upload integrado em Challenge, o folder usado e:

```text
user_<id-do-usuario>/context_<nome-do-contexto>/challenges
```

Exemplo:

```text
user_1/context_Animais/challenges/1710000000000_imagem.png
```

## Por que `imageBackup` e interno

O `imageBackup` guarda uma representacao Base64 reduzida da imagem. Ele parece existir como copia auxiliar da imagem, mas nao e retornado no `ContextDTO`.

Motivos identificados no codigo para ser interno:

- O campo no DTO esta anotado com `@JsonIgnore`.
- O campo esta anotado com `@Schema(hidden = true)`.
- O DTO de resposta `ContextDTO` nao possui `imageBackup`.

## O que ainda falta fazer em Challenge

Challenge esta preparado na entidade, no DTO, no resource e no service para receber e salvar imagem.

Na entidade `Challenge`, existe o campo `imageBackup`, persistido como `TEXT` e escondido do JSON.

No DTO `ChallengeRegisterDTO`, existem os campos `file` e `imageBackup`. O campo `imageBackup` e interno e fica escondido do JSON/Swagger, seguindo o mesmo padrao usado em `ContextRegisterDTO`.

O upload funcional em `ChallengeService` segue o mesmo fluxo usado em Context: se `file` vier preenchido, o arquivo e lido uma vez em bytes, esses bytes geram o `imageBackup` e tambem sao enviados para o MinIO. `imageUrl` recebe a URL retornada e `imageBackup` recebe o thumbnail Base64.

No estado atual dos endpoints, `ChallengeResource` ja aceita `multipart/form-data` no cadastro e na atualizacao de Challenge.

Campos aceitos pelo DTO:

- `word`
- `imageUrl`
- `soundUrl`
- `videoUrl`
- `file`

O cadastro e atualizacao de Challenge recebem `ChallengeRegisterDTO` por `@ModelAttribute`.

No update, quando ha arquivo novo, o service usa o primeiro contexto associado ao Challenge para montar o folder.

## Observacoes

- O endpoint generico valida tamanho e tipo do arquivo, mas o upload integrado em Context nao replica essas validacoes.
- `generateBase64Thumbnail` usa dimensoes fixas de 200x200.
- `UploadImageService.uploadFile` retorna URL com `localhost:9000`, mesmo quando a aplicacao roda em container. Isso pode afetar clientes externos dependendo do ambiente.
- Em `application-dev.yml` e `application-test.yml`, as propriedades do MinIO aparecem como `minio.url`, `minio.access-key`, `minio.secret-key` e `minio.bucket`. Em `application.yml` e `application-prod.yml`, foi identificado um nivel extra `minio.minio`, que nao corresponde diretamente aos `@Value("${minio.url}")` usados no service.
