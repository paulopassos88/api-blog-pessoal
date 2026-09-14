# 📦 Módulo: Usuários & Perfis

Este módulo gerencia o ciclo de vida cadastral de usuários, regras de autorização administrativa e proteção de credenciais na **API Blog Pessoal**.

---

## 1. Entidades & Estrutura de Dados

### Entidade: `Usuario` (Herda de `BaseEntity`)
- `id`: (`Long`, PK, auto-incremento `BIGSERIAL`) — Identificador unívoco do usuário.
- `nome`: (`String`, obrigatório, min 3, max 100) — Nome completo de exibição.
- `email`: (`String`, obrigatório, formato de e-mail, max 255, único) — Chave natural para autenticação e contato.
- `senha`: (`String`, obrigatório, min 8 caracteres) — Credencial de segurança do usuário.
- `role`: (`Enum Role`, obrigatório: `ADMIN` ou `USER`) — Papel de autorização atribuído ao usuário.
- `dataCriacao`: (`LocalDateTime`, preenchimento automático via JPA Auditing `@CreatedDate`, não mutável).
- `dataAtualizacao`: (`LocalDateTime`, preenchimento automático via JPA Auditing `@LastModifiedDate`).

---

## 2. Regras de Negócio (RN)

- **RN-USR-01 (Unicidade de E-mail):** O e-mail informado não pode existir previamente no sistema. Em caso de duplicidade tanto na criação quanto na atualização cadastral, a operação deve ser rejeitada com `EmailJaCadastradoException` (`HTTP 409 Conflict`).
- **RN-USR-02 (Autorização Administrativa de Gestão):** Apenas usuários com perfil `ADMIN` podem atualizar dados ou excluir outros usuários. Os endpoints `PUT` e `DELETE` exigem o parâmetro `executorId`; caso o usuário correspondente a esse ID não possua permissão de administrador, o sistema rejeita com `BusinessException("Usuário executor não tem permissão para realizar esta operação")` (`HTTP 400 Bad Request`).
- **RN-USR-03 (Validação e Integridade Cadastral):**
  - Nome: obrigatório, não em branco, entre 3 e 100 caracteres.
  - E-mail: obrigatório, não em branco, formato RFC válido.
  - Senha: obrigatória na criação, mínimo de 8 caracteres.
  - Role: obrigatório, valor restrito a `ADMIN` ou `USER`.
- **RN-USR-04 (Proteção de Credenciais):** O campo `senha` nunca deve ser serializado ou exposto no payload de resposta `UsuarioResponse`.

---

## 3. Contratos de API (DTOs & Endpoints)

### DTOs
```java
// Payload de Cadastro
public record UsuarioRequest(
    @NotBlank @Size(min = 3, max = 100) String nome,
    @NotBlank @Email String email,
    @NotBlank @Size(min = 8) String senha,
    @NotNull Role role
) {}

// Payload de Atualização
public record UsuarioUpdateRequest(
    @NotBlank @Size(min = 3, max = 100) String nome,
    @NotBlank @Email String email,
    @NotNull Role role
) {}

// Payload de Resposta
public record UsuarioResponse(
    Long id,
    String nome,
    String email,
    Role role,
    LocalDateTime dataCriacao
) {}
```

### Endpoints

#### `POST /api/v1/usuarios`
- **Permissão:** Pública
- **Body:** `UsuarioRequest`
- **Sucesso:** `201 Created` → `EntityModel<UsuarioResponse>` (com links HATEOAS)
- **Erros Mapeados:** `400 Bad Request` (validação de campos), `409 Conflict` (e-mail já cadastrado).

#### `GET /api/v1/usuarios/busca-por-email`
- **Permissão:** Pública
- **Query Params:** `email` (obrigatório, String)
- **Sucesso:** `200 OK` → `EntityModel<UsuarioResponse>`
- **Erros Mapeados:** `400 Bad Request` (e-mail não encontrado).

#### `GET /api/v1/usuarios/busca-por-nome`
- **Permissão:** Pública
- **Query Params:** `nome` (obrigatório, String)
- **Sucesso:** `200 OK` → `CollectionModel<EntityModel<UsuarioResponse>>`
- **Comportamento:** Busca parcial (`LIKE %termo%`), case-insensitive, ordenado por data de criação descrescente.

#### `PUT /api/v1/usuarios/{id}`
- **Permissão:** `Role: ADMIN` (`executorId`)
- **Path Params:** `id` (Long, obrigatório)
- **Query Params:** `executorId` (Long, obrigatório)
- **Body:** `UsuarioUpdateRequest`
- **Sucesso:** `200 OK` → `EntityModel<UsuarioResponse>`
- **Erros Mapeados:** `400 Bad Request` (validação ou executor sem papel `ADMIN`), `404 Not Found` (usuário não encontrado), `409 Conflict` (novo e-mail pertence a outro usuário).

#### `DELETE /api/v1/usuarios/{id}`
- **Permissão:** `Role: ADMIN` (`executorId`)
- **Path Params:** `id` (Long, obrigatório)
- **Query Params:** `executorId` (Long, obrigatório)
- **Sucesso:** `204 No Content`
- **Erros Mapeados:** `400 Bad Request` (usuário alvo inexistente ou executor não é `ADMIN`).

---

## 4. Critérios de Aceite (BDD / Cenários de Teste)

### Cenário 1: Cadastro de usuário com sucesso
- **Dado** que os dados de cadastro são válidos e o e-mail `"autor@teste.com"` não existe no banco,
- **Quando** o cliente envia uma requisição `POST /api/v1/usuarios`,
- **Então** o usuário deve ser persistido, a senha deve ser armazenada e a resposta deve retornar `201 Created` com os dados do usuário (sem a senha).

### Cenário 2: Tentativa de cadastro com e-mail duplicado
- **Dado** que já existe um usuário com o e-mail `"usuario@teste.com"`,
- **Quando** é enviado um `POST /api/v1/usuarios` utilizando esse mesmo e-mail,
- **Então** o sistema deve rejeitar o cadastro com `HTTP 409 Conflict` e mensagem informando a duplicidade.

### Cenário 3: Atualização de usuário executada por administrador
- **Dado** que o executor possui ID correspondente a um usuário com perfil `ADMIN`,
- **Quando** o executor envia um `PUT /api/v1/usuarios/{id}?executorId={adminId}` com dados válidos,
- **Então** os dados cadastrais do usuário alvo devem ser atualizados e o retorno deve ser `200 OK`.

### Cenário 4: Tentativa de atualização executada por perfil comum (não ADMIN)
- **Dado** que o executor informado possui perfil `USER`,
- **Quando** o executor tenta enviar um `PUT /api/v1/usuarios/{id}?executorId={userId}`,
- **Então** a alteração deve ser bloqueada com `HTTP 400 Bad Request` informando falta de permissão.

### Cenário 5: Tentativa de atualização para e-mail já pertencente a outro usuário
- **Dado** que o usuário A tem o e-mail `"a@teste.com"` e o usuário B tem o e-mail `"b@teste.com"`,
- **Quando** um administrador tenta atualizar o usuário B com o e-mail `"a@teste.com"`,
- **Então** o sistema deve recusar com `HTTP 409 Conflict`.

### Cenário 6: Exclusão de usuário executada por administrador
- **Dado** que existe um usuário cadastrado e o executor possui perfil `ADMIN`,
- **Quando** o executor envia um `DELETE /api/v1/usuarios/{id}?executorId={adminId}`,
- **Então** o registro do usuário deve ser removido permanentemente e retornar `204 No Content`.

### Cenário 7: Tentativa de exclusão executada por usuário não-administrador
- **Dado** que o executor informado possui perfil `USER`,
- **Quando** ele envia um `DELETE /api/v1/usuarios/{id}?executorId={userId}`,
- **Então** o sistema deve negar a exclusão retornando `HTTP 400 Bad Request`.

### Cenário 8: Busca de usuário por e-mail com sucesso
- **Dado** que existe um usuário cadastrado com e-mail `"busca@teste.com"`,
- **Quando** for realizada uma requisição `GET /api/v1/usuarios/busca-por-email?email=busca@teste.com`,
- **Então** o sistema deve retornar os dados do usuário com status `200 OK`.

### Cenário 9: Busca de usuário por e-mail inexistente
- **Dado** que o e-mail `"inexistente@teste.com"` não está cadastrado,
- **Quando** for consultado o endpoint `GET /api/v1/usuarios/busca-por-email?email=inexistente@teste.com`,
- **Então** o sistema deve retornar `HTTP 400 Bad Request` indicando que o usuário não foi localizado.

### Cenário 10: Busca de usuários por termo no nome
- **Dado** que existem usuários com os nomes `"João Silva"` e `"Maria Silva"`,
- **Quando** for requisitado `GET /api/v1/usuarios/busca-por-nome?nome=silva`,
- **Então** o sistema deve retornar uma coleção com ambos os usuários com status `200 OK`.
