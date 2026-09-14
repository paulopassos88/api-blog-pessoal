# 📦 Módulo: Comentários & Respostas Aninhadas

Este módulo especifica a estrutura de comentários hierárquicos (*Threaded Comments*), sistema de respostas multinível, regras de perfil e interações de curtidas na **API Blog Pessoal**.

---

## 1. Entidades & Estrutura de Dados

### Entidade: `Comentario` (Herda de `BaseEntity`)
- `id`: (`Long`, PK, auto-incremento `BIGSERIAL`) — Identificador unívoco do comentário.
- `texto`: (`String`, tipo SQL `TEXT`, obrigatório) — Conteúdo textual da mensagem.
- `autor`: (`Usuario`, `@ManyToOne(fetch = LAZY)`, FK `autor_id`, não nulo) — Autor do comentário.
- `post`: (`Post`, `@ManyToOne(fetch = LAZY)`, FK `post_id`, cascata `ON DELETE CASCADE`) — Publicação associada.
- `pai`: (`Comentario`, `@ManyToOne(fetch = LAZY)`, FK `pai_id`, cascata `ON DELETE CASCADE`, opcional) — Comentário ascendente; nulo se for comentário raiz.
- `respostas`: (`List<Comentario>`, `@OneToMany(mappedBy = "pai")`, cascata `ALL`, `orphanRemoval = true`).
- `curtidas`: (`Set<Usuario>`, `@ManyToMany` via tabela associativa `comentarios_curtidas`).
- `dataCriacao`: (`LocalDateTime`, preenchimento automático `@CreatedDate`, não mutável).
- `dataAtualizacao`: (`LocalDateTime`, preenchimento automático `@LastModifiedDate`).

---

## 2. Regras de Negócio (RN)

- **RN-COM-01 (Restrição de Comentários por Perfil):** Administradores (`ADMIN`) são exclusivamente moderadores e **NÃO** podem comentar ou enviar respostas. Apenas usuários com perfil `USER` possuem autorização para interagir. A regra é implementada via `ValidadorComentarioAutorNaoEhAdmin` (`ValidadorCriacaoComentario`), disparando `BusinessException` (`HTTP 400 Bad Request`) em caso de tentativa de envio por um administrador.
- **RN-COM-02 (Hierarquia em Árvore - Threaded Comments):**
  - Comentários raiz possuem obrigatoriamente `pai_id IS NULL`.
  - A listagem `GET /api/v1/posts/{postId}/comentarios` carrega exclusivamente os nós raiz, aninhando suas respectivas respostas de forma recursiva no campo `respostas` do DTO `ComentarioResponse`.
- **RN-COM-03 (Exclusão em Cascata Referencial):** Caso uma publicação seja removida, todos os comentários, respostas e curtidas associados são eliminados pelo banco (`ON DELETE CASCADE`). Da mesma forma, ao excluir um comentário pai, todas as suas respostas dependentes são excluídas.
- **RN-COM-04 (Alternância de Curtidas - Toggle):** O endpoint `POST /api/v1/comentarios/{id}/curtir` funciona como alternador de estado: se o usuário já curtiu o comentário, a curtida é desfeita; se ainda não curtiu, é registrada.

---

## 3. Contratos de API (DTOs & Endpoints)

### DTOs
```java
// Payload de Envio (Novo Comentário ou Resposta)
public record ComentarioRequest(
    @NotBlank(message = "O texto do comentário é obrigatório")
    String texto
) {}

// Payload de Resposta Hierárquica (com HATEOAS)
public record ComentarioResponse(
    Long id,
    String texto,
    String nomeAutor,
    int totalCurtidas,
    List<ComentarioResponse> respostas,
    LocalDateTime dataCriacao
) {}
```

### Endpoints

#### `POST /api/v1/posts/{postId}/comentarios`
- **Permissão:** `Role: USER` (`autorId`)
- **Path Params:** `postId` (Long, obrigatório)
- **Query Params:** `autorId` (Long, obrigatório)
- **Body:** `ComentarioRequest`
- **Sucesso:** `201 Created` → `EntityModel<ComentarioResponse>` (com links `responder` e `curtir`)
- **Erros Mapeados:** `400 Bad Request` (validação de texto, post inexistente ou autor com papel `ADMIN`).

#### `GET /api/v1/posts/{postId}/comentarios`
- **Permissão:** Pública
- **Path Params:** `postId` (Long, obrigatório)
- **Sucesso:** `200 OK` → `CollectionModel<EntityModel<ComentarioResponse>>`
- **Comportamento:** Retorna os comentários de nível raiz (`pai IS NULL`) ordenados por data de criação descrescente, com as respostas aninhadas.

#### `POST /api/v1/comentarios/{id}/respostas`
- **Permissão:** `Role: USER` (`autorId`)
- **Path Params:** `id` (Long, obrigatório - ID do comentário-pai)
- **Query Params:** `autorId` (Long, obrigatório)
- **Body:** `ComentarioRequest`
- **Sucesso:** `201 Created` → `EntityModel<ComentarioResponse>`
- **Erros Mapeados:** `400 Bad Request` (comentário-pai não existe ou autor com papel `ADMIN`).

#### `POST /api/v1/comentarios/{id}/curtir`
- **Permissão:** Livre (`usuarioId`)
- **Path Params:** `id` (Long, obrigatório)
- **Query Params:** `usuarioId` (Long, obrigatório)
- **Sucesso:** `200 OK` → `EntityModel<ComentarioResponse>`

---

## 4. Critérios de Aceite (BDD / Cenários de Teste)

### Cenário 1: Comentário raiz publicado com sucesso
- **Dado** que existe um post publicado e um autor com perfil `USER`,
- **Quando** o usuário envia `POST /api/v1/posts/{postId}/comentarios?autorId={userId}` com texto válido,
- **Então** o comentário deve ser gravado com `pai_id` nulo e retornar `201 Created` contendo os links de ação HATEOAS.

### Cenário 2: Tentativa de comentar por usuário administrador
- **Dado** que o solicitante possui perfil `ADMIN`,
- **Quando** ele tenta enviar um comentário para um post,
- **Então** a operação deve ser rejeitada com `HTTP 400 Bad Request` através do `ValidadorComentarioAutorNaoEhAdmin`.

### Cenário 3: Resposta aninhada a comentário existente
- **Dado** que existe um comentário-raiz cadastrado,
- **Quando** um usuário `USER` envia `POST /api/v1/comentarios/{id}/respostas?autorId={userId}` com mensagem válida,
- **Então** o sistema deve associar a resposta ao comentário-pai indicado e retornar `201 Created`.

### Cenário 4: Listagem hierárquica de comentários
- **Dado** que um post possui comentários-raiz e cada um possui respostas aninhadas,
- **Quando** for requisitado `GET /api/v1/posts/{postId}/comentarios`,
- **Então** o sistema deve retornar exclusivamente a lista de nós raiz no nível superior, embutindo as respostas dentro de cada nó.

### Cenário 5: Curtir comentário pela primeira vez
- **Dado** que o usuário 3 ainda não curtiu o comentário 8,
- **Quando** ele envia `POST /api/v1/comentarios/8/curtir?usuarioId=3`,
- **Então** a curtida deve ser registrada e o total de curtidas do comentário deve ser incrementado.

### Cenário 6: Descurtir comentário previamente curtido
- **Dado** que o usuário 3 já curtiu o comentário 8,
- **Quando** ele envia novamente `POST /api/v1/comentarios/8/curtir?usuarioId=3`,
- **Então** a curtida deve ser removida e o total decrementado.
