# 👤 Especificação Técnica: Módulo de Usuários

Esta especificação detalha o gerenciamento de usuários, regras de autorização por perfil, estruturas de dados, DTOs e endpoints da **API Blog Pessoal**.

---

## 1. Modelo de Domínio

### 1.1 Entidade `Usuario`
Estende `BaseEntity` (herdando `dataCriacao` e `dataAtualizacao`).

- `id`: Identificador numérico (`Long`, Auto-incremento).
- `nome`: Nome do usuário (`String`, min 3, max 100).
- `email`: Endereço único (`String`, formato válido de e-mail, max 255).
- `senha`: Hash ou credencial de acesso (`String`, min 8 caracteres).
- `role`: Papel de permissão no sistema (`Role`: `ADMIN` ou `USER`).

---

## 2. Regras de Negócio & Validações

1. **Unicidade de E-mail:**
   - No cadastro: O e-mail informado não pode existir previamente no banco de dados. Caso exista, lança `EmailJaCadastradoException` (HTTP 409 Conflict).
   - Na atualização: Se o e-mail for alterado, ele não pode pertencer a outro usuário já existente.
2. **Autorização Administrativa para Gestão:**
   - Apenas usuários com `Role.ADMIN` podem atualizar dados ou excluir registros de usuários.
   - Os endpoints `PUT` e `DELETE` exigem o parâmetro `executorId` que valida se o solicitante possui permissão `ADMIN`. Caso contrário, uma `BusinessException` é disparada.
3. **Validação de Tamanho e Formato de Campos:**
   - `nome`: Obrigatório, entre 3 e 100 caracteres.
   - `email`: Obrigatório, formato de e-mail RFC válido.
   - `senha`: Obrigatória no cadastro, mínimo 8 caracteres.
   - `role`: Obrigatório, valor enum `ADMIN` ou `USER`.

---

## 3. Estrutura de DTOs

### 3.1 `UsuarioRequest` (Entrada para Cadastro)
```java
public record UsuarioRequest(
    String nome,      // @NotBlank, @Size(min = 3, max = 100)
    String email,     // @NotBlank, @Email
    String senha,     // @NotBlank, @Size(min = 8)
    Role role         // @NotNull
) {}
```

### 3.2 `UsuarioUpdateRequest` (Entrada para Atualização)
```java
public record UsuarioUpdateRequest(
    String nome,      // @NotBlank, @Size(min = 3, max = 100)
    String email,     // @NotBlank, @Email
    Role role         // @NotNull
) {}
```

### 3.3 `UsuarioResponse` (Saída com HATEOAS)
```java
public record UsuarioResponse(
    Long id,
    String nome,
    String email,
    Role role,
    LocalDateTime dataCriacao
) {}
```
> **Nota de Segurança:** A senha do usuário **nunca** é exposta no `UsuarioResponse`.

---

## 4. Endpoints da API

**Caminho base:** `/api/v1/usuarios`

### 4.1 Cadastrar Usuário
- **Método:** `POST /api/v1/usuarios`
- **Corpo da Requisição:** `UsuarioRequest` (JSON)
- **Status de Sucesso:** `201 Created`
- **Exemplo de Requisição:**
```json
{
  "nome": "João Silva",
  "email": "joao.silva@example.com",
  "senha": "senhaSegura123",
  "role": "USER"
}
```
- **Exemplo de Resposta (com HATEOAS):**
```json
{
  "id": 2,
  "nome": "João Silva",
  "email": "joao.silva@example.com",
  "role": "USER",
  "dataCriacao": "2026-09-12T15:00:00",
  "_links": {
    "self": {
      "href": "http://localhost:8080/api/v1/usuarios/busca-por-email?email=joao.silva@example.com"
    },
    "busca-por-nome": {
      "href": "http://localhost:8080/api/v1/usuarios/busca-por-nome?nome=João Silva"
    }
  }
}
```

### 4.2 Buscar Usuário por E-mail
- **Método:** `GET /api/v1/usuarios/busca-por-email?email={email}`
- **Status de Sucesso:** `200 OK`
- **Retorno:** `EntityModel<UsuarioResponse>`
- **Possíveis Erros:** `400 Bad Request` se o e-mail não existir.

### 4.3 Buscar Usuários por Nome
- **Método:** `GET /api/v1/usuarios/busca-por-nome?nome={termo}`
- **Status de Sucesso:** `200 OK`
- **Retorno:** `CollectionModel<EntityModel<UsuarioResponse>>`
- **Comportamento:** Busca parcial (`LIKE %termo%`), case-insensitive, ordenado pelas criações mais recentes.

### 4.4 Atualizar Usuário
- **Método:** `PUT /api/v1/usuarios/{id}?executorId={executorId}`
- **Parâmetros:**
  - `id` (Path): ID do usuário a ser alterado.
  - `executorId` (Query): ID do administrador executando a ação.
- **Corpo da Requisição:** `UsuarioUpdateRequest` (JSON)
- **Status de Sucesso:** `200 OK`
- **Possíveis Erros:**
  - `400 Bad Request`: Dados inválidos ou executor não é `ADMIN`.
  - `409 Conflict`: O novo e-mail já está em uso por outro usuário.

### 4.5 Excluir Usuário
- **Método:** `DELETE /api/v1/usuarios/{id}?executorId={executorId}`
- **Status de Sucesso:** `204 No Content`
- **Possíveis Erros:** `400 Bad Request` se o usuário não for encontrado ou se o executor não for `ADMIN`.
