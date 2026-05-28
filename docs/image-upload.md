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

Na atualizacao de Context, se nenhum novo `file` for enviado, `imageBackup` e mantido. Se `imageUrl` vier preenchida, a URL manual e atualizada; se `imageUrl` vier vazia ou nula, a URL antiga e mantida.

Quando um Context e atualizado com uma nova imagem, a imagem antiga do Context e removida do MinIO depois que a nova imagem foi enviada e o Context foi salvo com sucesso. Se a remocao da imagem antiga falhar, o update permanece valido e a falha fica registrada em log.

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

## Acesso publico de leitura no MinIO

As imagens sao armazenadas no MinIO. O upload continua sendo feito pelo backend, usando as credenciais configuradas em `minio.access-key` e `minio.secret-key`.

Para que o frontend consiga carregar a imagem diretamente pela `imageUrl`, o bucket configurado em `MINIO_BUCKET` deve permitir leitura/download publico dos objetos. Isso nao deve liberar upload publico: o envio de arquivos continua restrito a API.

No Docker Compose, o servico `minio-init` deve criar o bucket definido em `MINIO_BUCKET` e aplicar politica equivalente a:

```bash
mc anonymous set download myminio/$MINIO_BUCKET
```

Essa politica permite `GetObject` publico para download das imagens, sem permitir `PutObject` publico direto no bucket.

## Organizacao dos folders no bucket

No endpoint generico de upload, o folder usado e:

```text
uploads
```

No upload integrado em Context, o folder usado e:

```text
user_<id-do-usuario>/context_<id-do-contexto>
```

Exemplo:

```text
user_1/context_1/1710000000000_imagem.png
```

No upload integrado em Challenge, o folder usado e:

```text
user_<id-do-usuario>/context_<id-do-contexto>/challenges
```

Exemplo:

```text
user_1/context_1/challenges/1710000000000_imagem.png
```

O nome do Context nao e usado como identificador de pasta porque ele pode ser editado. Novos uploads usam o ID do Context, que permanece estavel mesmo quando o nome muda. Arquivos antigos que ainda tenham sido salvos com `context_<nome-do-contexto>` nao sao migrados automaticamente nesta etapa.

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

Na atualizacao de Challenge, se nenhum novo `file` for enviado, `imageBackup` e mantido. Se `imageUrl` vier preenchida, a URL manual e atualizada; se `imageUrl` vier vazia ou nula, a URL antiga e mantida.

Em chamadas multipart de atualizacao pelo Swagger ou curl, o campo `file` pode ser omitido quando nao houver nova imagem. Se o cliente enviar `file=` como valor vazio, os resources tratam esse valor como ausencia de arquivo.

Quando um Challenge e atualizado com uma nova imagem, a imagem antiga do Challenge e removida do MinIO depois que a nova imagem foi enviada e o Challenge foi salvo com sucesso. Se a remocao da imagem antiga falhar, o update permanece valido e a falha fica registrada em log.

Quando um Challenge e deletado diretamente, a imagem associada ao `imageUrl` dele tambem e removida do MinIO.

Quando um Context e deletado, os Challenges associados a ele tambem sao removidos do banco no mesmo fluxo. A imagem do Context e as imagens dos Challenges associados devem ser removidas do MinIO usando as URLs salvas em `imageUrl`. Se alguma remocao no MinIO falhar, a falha e registrada em log como warning e nao impede a delecao dos registros no banco.

## Endpoints de imagem com fallback

Existe um endpoint publico para retornar a imagem de um Context como arquivo/bytes:

```http
GET /v1/api/contexts/{idContext}/image
```

Esse endpoint nao retorna JSON e nao expoe `imageBackup`. O fluxo e:

1. Busca o Context pelo ID.
2. Tenta baixar a imagem principal do MinIO usando `context.imageUrl`.
3. Se a imagem principal estiver disponivel, retorna os bytes da imagem.
4. Se a imagem principal falhar ou nao estiver disponivel, usa `context.imageBackup`.
5. O `imageBackup` e decodificado de Base64 e retornado como `image/jpeg`.
6. Se nao houver imagem principal nem `imageBackup`, a API retorna 404.

Existe um endpoint publico para retornar a imagem de um Challenge como arquivo/bytes:

```http
GET /v1/api/challenges/{idChallenge}/image
```

Esse endpoint nao retorna JSON e nao expoe `imageBackup`. O fluxo e:

1. Busca o Challenge pelo ID.
2. Tenta baixar a imagem principal do MinIO usando `challenge.imageUrl`.
3. Se a imagem principal estiver disponivel, retorna os bytes da imagem.
4. Se a imagem principal falhar ou nao estiver disponivel, usa `challenge.imageBackup`.
5. O `imageBackup` e decodificado de Base64 e retornado como `image/jpeg`.
6. Se nao houver imagem principal nem `imageBackup`, a API retorna 404.

O campo `imageBackup` continua interno e escondido do JSON principal. Ele serve apenas como fallback para esses endpoints de imagem.

## Observacoes

- O endpoint generico valida tamanho e tipo do arquivo, mas o upload integrado em Context nao replica essas validacoes.
- `generateBase64Thumbnail` usa dimensoes fixas de 200x200.
- `UploadImageService.uploadFile` retorna URL com `localhost:9000`, mesmo quando a aplicacao roda em container. Isso pode afetar clientes externos dependendo do ambiente.
- Em `application-dev.yml` e `application-test.yml`, as propriedades do MinIO aparecem como `minio.url`, `minio.access-key`, `minio.secret-key` e `minio.bucket`. Em `application.yml` e `application-prod.yml`, foi identificado um nivel extra `minio.minio`, que nao corresponde diretamente aos `@Value("${minio.url}")` usados no service.
