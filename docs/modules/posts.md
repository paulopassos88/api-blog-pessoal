# 📦 Módulo: Posts & Feed Otimizado

Este módulo especifica a criação, consulta, busca dinâmica, paginação de alta performance e interação social (curtidas) de publicações na **API Blog Pessoal**.

---

## 1. Entidades & Estrutura de Dados

### Entidade: `Post` (Herda de `BaseEntity`)
- `id`: (`Long`, PK, auto-incremento `BIGSERIAL`) — Identificador unívoco do post.
- `titulo`: (`String`, obrigatório, min 5, max 150) — Título da publicação.
- `slug`: (`String`, obrigatório, min 5, max 160, único) — Identificador amigável para rotas e SEO.
- `conteudo`: (`String`, tipo SQL `TEXT`, obrigatório) — Corpo completo do artigo.
- `autor`: (`Usuario`, `@ManyToOne(fetch = LAZY)`, FK `autor_id`, não nulo) — Autor criador do artigo.
- `categoria`: (`Categoria`, `@ManyToOne(fetch = LAZY)`, FK `categoria_id`, opcional) — Categoria associada.
- `tags`: (`Set<Tag>`, `@ManyToMany` via tabela `posts_tags`) — Tags associadas à publicação.
- `comentarios`: (`List<Comentario>`, `@OneToMany`, cascata total `ALL`, `orphanRemoval = true`).
- `curtidas`: (`Set<Usuario>`, `@ManyToMany` via tabela `posts_curtidas`).
- `dataCriacao`: (`LocalDateTime`, preenchimento automático `@CreatedDate`, não mutável).
- `dataAtualizacao`: (`LocalDateTime`, preenchimento automático `@LastModifiedDate`).

---

## 2. Regras de Negócio (RN)

- **RN-PST-01 (Restrição de Autoria por Perfil):** Apenas contas com perfil `USER` podem publicar posts. Administradores (`ADMIN`) são moderadores e não podem ser autores de publicações. A validação é desacoplada via `ValidadorPostAutorNaoEhAdmin` (`ValidadorCriacaoPost`), disparando `BusinessException` (`HTTP 400 Bad Request`) em caso de violação.
- **RN-PST-02 (Propriedade de Modificação e Exclusão):** Uma publicação só pode ser alterada (`PUT`) ou excluída (`DELETE`) pelo mesmo usuário que a criou (`autorId == post.autor.id`). Tentativas de outros usuários resultam em `BusinessException("Você não tem permissão para realizar esta operação")` (`HTTP 400 Bad Request`).
- **RN-PST-03 (Geração e Unicidade de Slugs):** O slug é gerado a partir do título via `SlugUtils.makeSlug(titulo)`. Em caso de colisão, o sistema consulta os prefixos existentes no repositório e aplica um sufixo numérico incremental (ex: `artigo-spring`, `artigo-spring-1`, `artigo-spring-2`).
- **RN-PST-04 (Cálculo Dinâmico de Tempo de Leitura):** Calculado pelo `PostMapper` com base na média humana de 200 palavras por minuto: $\text{tempoLeitura} = \lceil \text{totalPalavras} / 200 \rceil$. Mínimo retornado é 1 minuto.
- **RN-PST-05 (Feed Otimizado por Keyset Pagination):**
  - Utiliza `org.springframework.data.domain.Slice` para eliminar a contagem total de páginas (`COUNT(*)`).
  - Paginação baseada em cursor: filtra posts onde `id < :lastId` ordenados por `id DESC`.
  - Resumo de até 200 caracteres gerado diretamente pelo banco de dados com `SUBSTRING(p.conteudo, 1, 200)`.
- **RN-PST-06 (Toggle Idempotente de Curtidas):** O endpoint `POST /api/v1/posts/{id}/curtir` funciona como alternador (*toggle*): se o usuário já curtiu o post, a curtida é removida; se ainda não curtiu, é adicionada.
- **RN-PST-07 (Busca Segura com JPA Specifications):** A busca em `/api/v1/posts/search` aplica filtros combinados (título, conteúdo, slug da categoria e nome da tag) com escape seguro de metacaracteres SQL (`%`, `_`, `\`).

---

## 3. Contratos de API (DTOs & Endpoints)

### DTOs
```java
// Payload de Criação e Atualização
public record PostRequest(
    @NotBlank @Size(min = 5, max = 150) String titulo,
    @NotBlank String conteudo,
    Long categoriaId,
    List<String> tags
) {}

// Payload Completo do Post (com HATEOAS)
public record PostResponse(
    Long id,
    String titulo,
    String slug,
    String conteudo,
    String nomeAutor,
    CategoriaResponse categoria,
    List<TagResponse> tags,
    int totalCurtidas,
    int tempoLeitura,
    LocalDateTime dataCriacao
) {}

// Card Otimizado para Feed
public record PostFeedResponse(
    Long id,
    String titulo,
    String slug,
    String resumo,
    String nomeAutor,
    LocalDateTime dataCriacao,
    long quantidadeComentarios,
    long totalCurtidas
) {}
```

### Endpoints

#### `POST /api/v1/posts`
- **Permissão:** `Role: USER` (`autorId`)
- **Query Params:** `autorId` (Long, obrigatório)
- **Body:** `PostRequest`
- **Sucesso:** `201 Created` → `EntityModel<PostResponse>`
- **Erros Mapeados:** `400 Bad Request` (validação ou autor com role `ADMIN`), `404 Not Found` (autor inexistente).

#### `GET /api/v1/posts/feed`
- **Permissão:** Pública
- **Query Params:** `lastId` (Long, opcional - cursor), `size` (int, padrão 10)
- **Sucesso:** `200 OK` → `Slice<EntityModel<PostFeedResponse>>`

#### `GET /api/v1/posts`
- **Permissão:** Pública
- **Sucesso:** `200 OK` → `CollectionModel<EntityModel<PostResponse>>`

#### `GET /api/v1/posts/{id}`
- **Permissão:** Pública
- **Path Params:** `id` (Long, obrigatório)
- **Sucesso:** `200 OK` → `EntityModel<PostResponse>`
- **Erros Mapeados:** `400 Bad Request` (post não encontrado).

#### `GET /api/v1/posts/slug/{slug}`
- **Permissão:** Pública
- **Path Params:** `slug` (String, obrigatório)
- **Sucesso:** `200 OK` → `EntityModel<PostResponse>`
- **Erros Mapeados:** `400 Bad Request` (post não encontrado).

#### `GET /api/v1/posts/search`
- **Permissão:** Pública
- **Query Params:** `titulo`, `conteudo`, `categoria`, `tag` (todos opcionais)
- **Sucesso:** `200 OK` → `CollectionModel<EntityModel<PostResponse>>`

#### `PUT /api/v1/posts/{id}`
- **Permissão:** `Role: USER` (autor proprietário do post)
- **Path Params:** `id` (Long, obrigatório)
- **Query Params:** `autorId` (Long, obrigatório)
- **Body:** `PostRequest`
- **Sucesso:** `200 OK` → `EntityModel<PostResponse>`
- **Erros Mapeados:** `400 Bad Request` (dados inválidos ou usuário não é o autor proprietário).

#### `DELETE /api/v1/posts/{id}`
- **Permissão:** `Role: USER` (autor proprietário do post)
- **Path Params:** `id` (Long, obrigatório)
- **Query Params:** `autorId` (Long, obrigatório)
- **Sucesso:** `204 No Content`
- **Erros Mapeados:** `400 Bad Request` (post não encontrado ou usuário não é o autor).

#### `POST /api/v1/posts/{id}/curtir`
- **Permissão:** Livre (`usuarioId`)
- **Path Params:** `id` (Long, obrigatório)
- **Query Params:** `usuarioId` (Long, obrigatório)
- **Sucesso:** `200 OK` → `EntityModel<PostResponse>` (retorna estado atualizado)

---

## 4. Critérios de Aceite (BDD / Cenários de Teste)

### Cenário 1: Publicação de post com sucesso por usuário comum
- **Dado** que um usuário com papel `USER` está cadastrado,
- **Quando** ele envia um `POST /api/v1/posts?autorId={id}` com título e conteúdo válidos,
- **Então** o post deve ser persistido, o slug gerado e a resposta deve retornar `201 Created` com o tempo de leitura calculado.

### Cenário 2: Tentativa de publicação por administrador
- **Dado** que um usuário possui papel `ADMIN`,
- **Quando** ele tenta criar um post via `POST /api/v1/posts?autorId={adminId}`,
- **Então** a operação deve ser rejeitada pela regra de validação com `HTTP 400 Bad Request`.

### Cenário 3: Resolução de conflito de slug idêntico
- **Dado** que já existe uma publicação com o título `"Primeiro Post"` (slug `"primeiro-post"`),
- **Quando** outro post for cadastrado com o mesmo título `"Primeiro Post"`,
- **Então** o sistema deve atribuir automaticamente o slug `"primeiro-post-1"` e persistir com sucesso.

### Cenário 4: Atualização de post pelo autor original
- **Dado** que o autor com ID 1 criou o post com ID 10,
- **Quando** o autor envia um `PUT /api/v1/posts/10?autorId=1` com novo título e conteúdo,
- **Então** os dados do post devem ser atualizados e a resposta retornar `200 OK`.

### Cenário 5: Tentativa de atualização de post por outro usuário
- **Dado** que o post 10 pertence ao autor 1,
- **Quando** o usuário com ID 2 envia um `PUT /api/v1/posts/10?autorId=2`,
- **Então** o sistema deve negar a operação com `HTTP 400 Bad Request` e mensagem de permissão negada.

### Cenário 6: Exclusão de post pelo autor original
- **Dado** que o post 10 pertence ao autor 1 e possui comentários vinculados,
- **Quando** o autor envia um `DELETE /api/v1/posts/10?autorId=1`,
- **Então** o post e todos os seus comentários associados devem ser excluídos, retornando `204 No Content`.

### Cenário 7: Tentativa de exclusão de post por outro usuário
- **Dado** que o post 10 pertence ao autor 1,
- **Quando** o usuário com ID 2 envia um `DELETE /api/v1/posts/10?autorId=2`,
- **Então** a exclusão deve ser bloqueada com `HTTP 400 Bad Request`.

### Cenário 8: Curtir um post pela primeira vez
- **Dado** que o usuário 5 ainda não curtiu o post 10,
- **Quando** ele envia uma requisição `POST /api/v1/posts/10/curtir?usuarioId=5`,
- **Então** a curtida deve ser registrada e o total de curtidas do post incrementado em 1.

### Cenário 9: Descurtir post previamente curtido (toggle)
- **Dado** que o usuário 5 já havia curtido o post 10,
- **Quando** ele envia novamente a requisição `POST /api/v1/posts/10/curtir?usuarioId=5`,
- **Então** o registro da curtida deve ser removido e o total de curtidas decrementado em 1.

### Cenário 10: Consulta ao feed paginado por cursor
- **Dado** que existem dezenas de publicações cadastradas,
- **Quando** o cliente consulta `GET /api/v1/posts/feed?lastId=50&size=10`,
- **Então** o sistema deve retornar os 10 posts anteriores ao ID 50 em ordem decrescente sem executar `COUNT(*)`.

### Cenário 11: Busca de post por slug existente
- **Dado** que existe um post com slug `"artigo-spring-boot"`,
- **Quando** o cliente requisita `GET /api/v1/posts/slug/artigo-spring-boot`,
- **Então** o sistema deve retornar a publicação completa com status `200 OK`.
