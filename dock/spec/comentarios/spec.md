# 💬 Especificação Técnica: Módulo de Comentários

Esta especificação descreve a arquitetura de comentários hierárquicos (*Threaded Comments*), sistema de respostas multinível, regras de autorização e curtidas na **API Blog Pessoal**.

---

## 1. Modelo de Domínio

### 1.1 Entidade `Comentario`
Estende `BaseEntity` (herdando `dataCriacao` e `dataAtualizacao`).

- `id`: Identificador numérico único (`Long`, Auto-incremento).
- `texto`: Conteúdo textual do comentário (`String`, tipo SQL `TEXT`, obrigatório).
- `autor`: Relacionamento `@ManyToOne(fetch = LAZY)` com `Usuario`.
- `post`: Relacionamento `@ManyToOne(fetch = LAZY)` com `Post`.
- `pai`: Auto-relacionamento `@ManyToOne(fetch = LAZY)` com `Comentario` (chave estrangeira `pai_id`). É nulo para comentários de nível raiz.
- `respostas`: Lista `@OneToMany(mappedBy = "pai")` com deleção em cascata (`cascade = ALL, orphanRemoval = true`).
- `curtidas`: Conjunto `@ManyToMany` de usuários que curtiram o comentário via `comentarios_curtidas`.

---

## 2. Regras de Negócio & Padrões

### 2.1 Restrição de Comentários por Perfil
- Administradores (`ADMIN`) são responsáveis apenas pela moderação da plataforma e **NÃO** podem postar comentários ou respostas.
- Apenas usuários comuns (`USER`) possuem autorização para comentar.
- Validação desacoplada através de `ValidadorComentarioAutorNaoEhAdmin` (que implementa `ValidadorCriacaoComentario` e `ValidadorRegraNegocio<ComentarioRequest>`).

### 2.2 Estrutura em Árvore (Threaded Comments)
- A listagem de comentários do post (`GET /api/v1/posts/{postId}/comentarios`) carrega exclusivamente os **comentários-raiz** (`c.pai IS NULL`).
- As respostas a cada comentário são aninhadas automaticamente na propriedade `respostas` do DTO `ComentarioResponse`, suportando múltiplos níveis de profundidade de discussão.

### 2.3 Exclusão em Cascata
- Se um post for excluído, todos os comentários e curtidas associados são deletados pelo banco de dados (`ON DELETE CASCADE`).
- Se um comentário-pai for removido, todas as suas respostas são automaticamente eliminadas.

### 2.4 Curtidas em Comentários (Toggle Idempotente)
- O endpoint `POST /api/v1/comentarios/{id}/curtir` alterna o status da curtida do usuário: remove se já curtiu, adiciona se ainda não curtiu.

---

## 3. Estrutura de DTOs

### 3.1 `ComentarioRequest`
```java
public record ComentarioRequest(
    @NotBlank(message = "O texto do comentário é obrigatório")
    String texto
) {}
```

### 3.2 `ComentarioResponse` (Estrutura Recursiva com HATEOAS)
```java
public record ComentarioResponse(
    Long id,
    String texto,
    String nomeAutor,
    int totalCurtidas,
    List<ComentarioResponse> respostas,
    LocalDateTime dataCriacao
) {}
```

---

## 4. Endpoints da API

**Caminho base:** `/api/v1`

### 4.1 Adicionar Comentário a um Post
- **Método:** `POST /api/v1/posts/{postId}/comentarios?autorId={autorId}`
- **Corpo da Requisição:** `ComentarioRequest` (JSON)
- **Status de Sucesso:** `201 Created`
- **Exemplo de Envio:**
```json
{
  "texto": "Parabéns pelo artigo! Muito esclarecedor sobre Spring Boot."
}
```
- **Links HATEOAS Inclusos na Resposta:**
  - `responder`: `/api/v1/comentarios/{id}/respostas`
  - `curtir`: `/api/v1/comentarios/{id}/curtir`

### 4.2 Listar Comentários de um Post
- **Método:** `GET /api/v1/posts/{postId}/comentarios`
- **Status de Sucesso:** `200 OK`
- **Retorno:** `CollectionModel<EntityModel<ComentarioResponse>>`
- **Comportamento:** Retorna a árvore hierárquica de comentários ordenados do mais recente para o mais antigo.

### 4.3 Responder a um Comentário Existente
- **Método:** `POST /api/v1/comentarios/{id}/respostas?autorId={autorId}`
- **Parâmetros:**
  - `id` (Path): Identificador do comentário-pai que receberá a resposta.
  - `autorId` (Query): Identificador do usuário que está respondendo.
- **Corpo da Requisição:** `ComentarioRequest` (JSON)
- **Status de Sucesso:** `201 Created`

### 4.4 Curtir / Descurtir Comentário
- **Método:** `POST /api/v1/comentarios/{id}/curtir?usuarioId={usuarioId}`
- **Status de Sucesso:** `200 OK`
