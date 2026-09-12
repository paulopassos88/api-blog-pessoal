# 🏷️ Especificação Técnica: Módulo de Categorias & Tags

Esta especificação detalha os mecanismos de taxonomia e classificação de publicações através de Categorias e Tags na **API Blog Pessoal**.

---

## 1. Modelos de Domínio

### 1.1 Entidade `Categoria`
- `id`: Identificador numérico único (`Long`, Identity).
- `nome`: Nome da categoria (`String`, min 3, max 50, único, obrigatório).
- `slug`: Versão normalizada para URLs (`String`, min 3, max 60, único, obrigatório).

### 1.2 Entidade `Tag`
- `id`: Identificador numérico único (`Long`, Identity).
- `nome`: Nome do marcador/etiqueta (`String`, min 2, max 30, único, obrigatório).

---

## 2. Regras de Negócio & Padrões

### 2.1 Categorias
- O nome da categoria deve ser único no sistema. Caso já exista, lança `BusinessException("Categoria já cadastrada")`.
- O slug da categoria é gerado automaticamente a partir do nome utilizando `SlugUtils.makeSlug(nome)` no momento da persistência.
- Categorias são associadas aos posts de forma unívoca (1 post pertence a no máximo 1 categoria).

### 2.2 Tags & Mecanismo On-Demand (`buscarOuCriar`)
- O nome da tag deve ser único.
- Ao publicar ou atualizar um post (`PostService`), a lista de tags informadas em texto (ex: `["Java", "Spring Boot", "DevOps"]`) é processada pelo método `tagService.buscarOuCriar(nome)`:
  - Se a tag já existir no banco, a instância existente é reutilizada.
  - Se a tag não existir, ela é criada e persistida instantaneamente sem a necessidade de uma chamada prévia separada.
- O relacionamento entre Posts e Tags é de muitos-para-muitos (`@ManyToMany`) com integridade referencial em cascata.

---

## 3. Estrutura de DTOs

### 3.1 `CategoriaRequest`
```java
public record CategoriaRequest(
    @NotBlank @Size(min = 3, max = 50) String nome
) {}
```

### 3.2 `CategoriaResponse`
```java
public record CategoriaResponse(
    Long id,
    String nome,
    String slug
) {}
```

### 3.3 `TagRequest`
```java
public record TagRequest(
    @NotBlank @Size(min = 2, max = 30) String nome
) {}
```

### 3.4 `TagResponse`
```java
public record TagResponse(
    Long id,
    String nome
) {}
```

---

## 4. Endpoints da API

### 4.1 Categorias (`/api/v1/categorias`)

#### Criar Categoria
- **Método:** `POST /api/v1/categorias`
- **Corpo:** `CategoriaRequest` (JSON)
- **Status de Sucesso:** `201 Created`
- **Exemplo de Envio:**
```json
{
  "nome": "Inteligência Artificial"
}
```
- **Exemplo de Resposta (com HATEOAS):**
```json
{
  "id": 6,
  "nome": "Inteligência Artificial",
  "slug": "inteligencia-artificial",
  "_links": {
    "categorias": {
      "href": "http://localhost:8080/api/v1/categorias"
    }
  }
}
```

#### Listar Todas as Categorias
- **Método:** `GET /api/v1/categorias`
- **Status de Sucesso:** `200 OK`
- **Retorno:** `CollectionModel<EntityModel<CategoriaResponse>>`

---

### 4.2 Tags (`/api/v1/tags`)

#### Listar Todas as Tags
- **Método:** `GET /api/v1/tags`
- **Status de Sucesso:** `200 OK`
- **Retorno:** `CollectionModel<EntityModel<TagResponse>>`
- **Exemplo de Resposta (com HATEOAS):**
```json
{
  "_embedded": {
    "tagResponseList": [
      {
        "id": 1,
        "nome": "Java",
        "_links": {
          "tags": { "href": "http://localhost:8080/api/v1/tags" }
        }
      },
      {
        "id": 2,
        "nome": "Spring Boot",
        "_links": {
          "tags": { "href": "http://localhost:8080/api/v1/tags" }
        }
      }
    ]
  },
  "_links": {
    "self": { "href": "http://localhost:8080/api/v1/tags" }
  }
}
```
