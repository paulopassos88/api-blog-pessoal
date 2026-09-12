# 📰 Especificação Técnica: Módulo de Posts & Feed

Esta especificação aborda o ciclo de vida completo de publicações, cálculo dinâmico de tempo de leitura, geração resiliente de slugs, paginação por cursor (feed de alta performance), pesquisa dinâmica e sistema de curtidas.

---

## 1. Modelo de Domínio

### 1.1 Entidade `Post`
Estende `BaseEntity` (herdando `dataCriacao` e `dataAtualizacao`).

- `id`: Identificador numérico único (`Long`, Auto-incremento).
- `titulo`: Título da publicação (`String`, min 5, max 150).
- `slug`: Identificador textual amigável para URLs (`String`, único, min 5, max 160).
- `conteudo`: Corpo completo do artigo em texto/markdown (`String`, tipo SQL `TEXT`).
- `autor`: Relacionamento `@ManyToOne(fetch = LAZY)` com `Usuario`.
- `categoria`: Relacionamento opcional `@ManyToOne(fetch = LAZY)` com `Categoria`.
- `tags`: Relacionamento `@ManyToMany` mapeado via tabela `posts_tags`.
- `comentarios`: Relacionamento `@OneToMany` com remoção em cascata (`orphanRemoval = true`).
- `curtidas`: Coleção `@ManyToMany` de usuários que curtiram o post via `posts_curtidas`.

---

## 2. Regras de Negócio & Algoritmos

### 2.1 Restrição de Criação por Perfil (Strategy Pattern)
- Administradores (`ADMIN`) gerenciam a infraestrutura da plataforma e **NÃO** podem criar posts.
- Apenas usuários com perfil comum (`USER`) podem criar postagens.
- Regra implementada desacopladamente via `ValidadorPostAutorNaoEhAdmin` (que implementa `ValidadorCriacaoPost` e `ValidadorRegraNegocio<PostRequest>`).

### 2.2 Propriedade & Autorização de Modificação
- Apenas o autor original que publicou o post tem permissão para editá-lo (`PUT`) ou excluí-lo (`DELETE`).
- Caso outro usuário tente alterar ou excluir a publicação, uma `BusinessException("Você não tem permissão para realizar esta operação")` é lançada.

### 2.3 Geração e Resolução de Conflitos de Slug
- O slug é gerado a partir do título utilizando `SlugUtils.makeSlug(titulo)` (normalização Unicode NFKD, remoção de diacríticos/acentos e substituição de caracteres não alfanuméricos por hífens).
- Se houver colisão de slugs, o repositório consulta a quantidade existente via `countBySlugPrefix(slugBase, slugBase + "-%")` e adiciona um sufixo numérico incremental (ex: `artigo-spring`, `artigo-spring-1`, `artigo-spring-2`).

### 2.4 Cálculo de Tempo Estimado de Leitura
- Executado automaticamente pelo `PostMapper` com base no volume de texto.
- Adota o padrão de leitura humana de **200 palavras por minuto**:
  $$\text{tempoLeitura (min)} = \left\lceil \frac{\text{totalPalavras}}{200} \right\rceil$$

### 2.5 Feed Otimizado com Keyset Pagination (Cursor)
Para viabilizar scroll infinito e evitar degradação de performance em servidores VPS com recursos limitados:
1. **Eliminação do `COUNT(*)`:** Utiliza `org.springframework.data.domain.Slice` em vez de `Page`. O `Slice` apenas verifica se há uma próxima página sem disparar a onerosa consulta de contagem total.
2. **Paginação por Cursor (`lastId`):** Filtro `WHERE (:lastId IS NULL OR p.id < :lastId) ORDER BY p.id DESC`. Isso evita o problema de duplicidade/salto de itens característico do `OFFSET/LIMIT` tradicional quando novos posts são inseridos.
3. **Resumo Otimizado no Banco:** O resumo dos primeiros 200 caracteres é gerado diretamente pela função SQL `SUBSTRING(p.conteudo, 1, 200)` dentro da projeção do repositório, economizando tráfego de rede e memória JVM.

### 2.6 Sistema de Curtidas (Toggle Idempotente)
- O endpoint `POST /api/v1/posts/{id}/curtir` funciona em modo alternância (*toggle*): se o usuário já curtiu o post, a curtida é removida; se ainda não curtiu, a curtida é adicionada.

---

## 3. DTOs do Módulo

### 3.1 `PostRequest`
```java
public record PostRequest(
    @NotBlank @Size(min = 5, max = 150) String titulo,
    @NotBlank String conteudo,
    Long categoriaId,
    List<String> tags
) {}
```

### 3.2 `PostResponse` (Visualização Completa com HATEOAS)
```java
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
```

### 3.3 `PostFeedResponse` (Card Simplificado para Feed)
```java
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

---

## 4. Endpoints da API

**Caminho base:** `/api/v1/posts`

### 4.1 Criar Post
- **Método:** `POST /api/v1/posts?autorId={id}`
- **Corpo:** `PostRequest` (JSON)
- **Status:** `201 Created`
- **Exemplo de Envio:**
```json
{
  "titulo": "Explorando Spring Boot 3.5 com Java 21",
  "conteudo": "Neste post abordaremos como tirar proveito dos novos recursos...",
  "categoriaId": 1,
  "tags": ["Spring Boot", "Java", "Backend"]
}
```

### 4.2 Listar Feed Paginado (Cursor)
- **Método:** `GET /api/v1/posts/feed?lastId={id}&size={size}`
- **Parâmetros:**
  - `lastId` (opcional): Cursor correspondente ao menor ID recebido na página anterior.
  - `size` (padrão: 10): Quantidade de itens por fatia.
- **Status:** `200 OK`
- **Retorno:** `Slice<EntityModel<PostFeedResponse>>` com metadados de paginação (`hasNext`, `hasPrevious`, `numberOfElements`).

### 4.3 Listar Todos os Posts
- **Método:** `GET /api/v1/posts`
- **Status:** `200 OK`
- **Retorno:** `CollectionModel<EntityModel<PostResponse>>`

### 4.4 Buscar Post por ID
- **Método:** `GET /api/v1/posts/{id}`
- **Status:** `200 OK`

### 4.5 Buscar Post por Slug
- **Método:** `GET /api/v1/posts/slug/{slug}`
- **Status:** `200 OK`
- **Exemplo:** `GET /api/v1/posts/slug/explorando-spring-boot-3-5-com-java-21`

### 4.6 Pesquisar Posts Dinamicamente
- **Método:** `GET /api/v1/posts/search`
- **Parâmetros de Filtro (todos opcionais):**
  - `titulo`: Termo parcial no título.
  - `conteudo`: Termo parcial no texto.
  - `categoria`: Slug exato da categoria.
  - `tag`: Nome exato da tag.
- **Implementação Técnica:** Utiliza `PostSpecifications` com sanitização e escape seguro de caracteres coringa (`%`, `_`, `\`).

### 4.7 Atualizar Post
- **Método:** `PUT /api/v1/posts/{id}?autorId={id}`
- **Corpo:** `PostRequest` (JSON)
- **Status:** `200 OK`

### 4.8 Excluir Post
- **Método:** `DELETE /api/v1/posts/{id}?autorId={id}`
- **Status:** `204 No Content` (exclui recursivamente todos os comentários e curtidas).

### 4.9 Curtir / Descurtir Post
- **Método:** `POST /api/v1/posts/{id}/curtir?usuarioId={usuarioId}`
- **Status:** `200 OK`
