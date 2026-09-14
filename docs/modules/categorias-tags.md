# 📦 Módulo: Categorias & Tags (Taxonomia)

Este módulo especifica a classificação temática primária (Categorias) e a rotulagem transversal (Tags) das postagens na **API Blog Pessoal**.

---

## 1. Entidades & Estrutura de Dados

### Entidade: `Categoria`
- `id`: (`Long`, PK, Identity `BIGINT`) — Identificador unívoco da categoria.
- `nome`: (`String`, obrigatório, min 3, max 50, único) — Nome legível da categoria.
- `slug`: (`String`, obrigatório, min 3, max 60, único) — Versão normalizada para URLs.

### Entidade: `Tag`
- `id`: (`Long`, PK, Identity `BIGINT`) — Identificador unívoco do marcador/tag.
- `nome`: (`String`, obrigatório, min 2, max 30, único) — Nome da tag em formato textual.

---

## 2. Regras de Negócio (RN)

- **RN-CAT-01 (Unicidade de Nome de Categoria):** O nome da categoria deve ser único no sistema. Tentativas de cadastrar categorias já existentes disparam `BusinessException("Categoria já cadastrada")` (`HTTP 400 Bad Request`).
- **RN-CAT-02 (Geração Automática de Slug de Categoria):** O slug da categoria é gerado de forma automática e transparente a partir do nome utilizando `SlugUtils.makeSlug(nome)` antes da persistência.
- **RN-TAG-01 (Unicidade de Nome de Tag):** O nome de uma tag é único. Não é permitida a duplicação de tags com a mesma grafia.
- **RN-TAG-02 (Criação On-Demand de Tags via `buscarOuCriar`):** Durante o cadastro ou edição de uma publicação (`PostService`), a lista de nomes de tags informadas em texto é processada por `tagService.buscarOuCriar(nome)`:
  - Se a tag já existir no banco, a entidade existente é reaproveitada.
  - Se a tag não existir, ela é instanciada e persistida sob demanda de forma automática, sem necessidade de chamada prévia isolada.

---

## 3. Contratos de API (DTOs & Endpoints)

### DTOs
```java
// Categoria: Entrada
public record CategoriaRequest(
    @NotBlank @Size(min = 3, max = 50) String nome
) {}

// Categoria: Resposta
public record CategoriaResponse(
    Long id,
    String nome,
    String slug
) {}

// Tag: Entrada
public record TagRequest(
    @NotBlank @Size(min = 2, max = 30) String nome
) {}

// Tag: Resposta
public record TagResponse(
    Long id,
    String nome
) {}
```

### Endpoints

#### `POST /api/v1/categorias`
- **Permissão:** Pública
- **Body:** `CategoriaRequest`
- **Sucesso:** `201 Created` → `EntityModel<CategoriaResponse>`
- **Erros Mapeados:** `400 Bad Request` (validação de tamanho ou nome já existente).

#### `GET /api/v1/categorias`
- **Permissão:** Pública
- **Sucesso:** `200 OK` → `CollectionModel<EntityModel<CategoriaResponse>>`

#### `GET /api/v1/tags`
- **Permissão:** Pública
- **Sucesso:** `200 OK` → `CollectionModel<EntityModel<TagResponse>>`

---

## 4. Critérios de Aceite (BDD / Cenários de Teste)

### Cenário 1: Cadastro de categoria com sucesso
- **Dado** que a categoria `"Tecnologia"` não existe no banco,
- **Quando** for enviada requisição `POST /api/v1/categorias` com o nome `"Tecnologia"`,
- **Então** o registro deve ser criado com slug `"tecnologia"` e retornar status `201 Created`.

### Cenário 2: Tentativa de cadastro de categoria duplicada
- **Dado** que a categoria `"Tecnologia"` já se encontra cadastrada,
- **Quando** for enviada nova tentativa de cadastro com o nome `"Tecnologia"`,
- **Então** a operação deve ser rejeitada com `HTTP 400 Bad Request` e mensagem `"Categoria já cadastrada"`.

### Cenário 3: Listagem de todas as categorias
- **Dado** que existem categorias cadastradas no sistema,
- **Quando** for requisitado `GET /api/v1/categorias`,
- **Então** o sistema deve retornar a coleção completa enriquecida com links HATEOAS e status `200 OK`.

### Cenário 4: Criação on-demand de novas tags durante publicação de post
- **Dado** que a tag `"Microsserviços"` não existe no banco,
- **Quando** um post for cadastrado contendo a tag `"Microsserviços"`,
- **Então** o sistema deve persistir a nova tag automaticamente e vinculá-la ao post.

### Cenário 5: Reutilização de tag existente durante publicação de post
- **Dado** que a tag `"Java"` com ID 1 já está cadastrada,
- **Quando** um post for cadastrado incluindo `"Java"` em sua lista de tags,
- **Então** nenhuma tag nova deve ser criada e a tag existente de ID 1 deve ser associada ao post.

### Cenário 6: Listagem de todas as tags
- **Dado** que existem tags cadastradas,
- **Quando** for consultado `GET /api/v1/tags`,
- **Então** o sistema deve retornar a lista completa com status `200 OK`.
