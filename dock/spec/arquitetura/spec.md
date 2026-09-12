# 🏗️ Especificação Técnica: Arquitetura & Padrões

Esta especificação descreve os pilares arquiteturais, padrões de design, convenções e configurações transversais aplicadas na **API Blog Pessoal**.

---

## 1. Visão Geral da Arquitetura

A aplicação foi construída sobre o ecossistema **Spring Boot 3.5.14** e **Java 21 (LTS)**, adotando uma arquitetura em camadas orientada a domínio (*Layered Architecture* aliada a princípios de *Package-by-Feature*).

```
br.com.passos.api_blog_pessoal
├── assembler/      # Conversores HATEOAS (RepresentationModelAssembler)
├── config/         # Configurações globais (JPA Auditing, OpenAPI, CORS)
├── controller/     # Camada de apresentação REST (Controllers)
├── dto/            # Data Transfer Objects imutáveis (Java Records)
├── exception/      # Tratamento global de exceções (@RestControllerAdvice)
├── mapper/         # Mapeadores de entidade/DTO (MapStruct)
├── model/          # Entidades de domínio JPA / Hibernate
├── repository/     # Repositórios Spring Data JPA e Specifications
├── service/        # Regras de negócio e casos de uso
│   └── validation/ # Validações de negócio desacopladas (Strategy Pattern)
└── util/           # Utilitários de domínio (ex: SlugUtils)
```

---

## 2. Tecnologias & Bibliotecas

| Componente | Tecnologia | Versão | Propósito |
| :--- | :--- | :--- | :--- |
| **Linguagem** | Java | 21 (LTS) | Uso de Records, Pattern Matching, Virtual Threads |
| **Framework Base** | Spring Boot | 3.5.14 | Auto-configuração, DI e ecossistema web |
| **Persistência** | Spring Data JPA / Hibernate | 6.x | Mapeamento Objeto-Relacional |
| **Bancos de Dados** | H2 Database / PostgreSQL | 16 | H2 em memória (Dev) e PostgreSQL (Prod/Test) |
| **Migrações de DB** | Flyway | Core + PostgreSQL | Versionamento e controle de schema SQL |
| **DTO Mapping** | MapStruct | 1.6.3 | Geração estática e performática de mapeamento |
| **Boilerplate** | Project Lombok | - | Redução de getters, setters e builders |
| **Hipermídia** | Spring HATEOAS | Starter | Enriquecimento de respostas com links de navegação |
| **Documentação API** | SpringDoc OpenAPI | 2.8.5 | Geração automática de OpenAPI 3.0 e Swagger UI |
| **Métricas** | Actuator + Micrometer | Prometheus | Exposição de métricas para monitoramento |
| **Testes** | JUnit 5 + Mockito + Testcontainers | 16-alpine | Testes unitários e de integração com banco real |

---

## 3. Princípios de Design & Boas Práticas

### 3.1 SOLID & Clean Code
- **Single Responsibility Principle (SRP):** Controllers apenas orquestram chamadas HTTP e HATEOAS; Services contêm regras de negócio; Mappers convertem dados; Validadores cuidam de regras específicas.
- **Open/Closed Principle (OCP):** Validações de negócio usam a interface `ValidadorRegraNegocio<T>` (ex: `ValidadorCriacaoPost`, `ValidadorCriacaoComentario`), permitindo adicionar novas regras sem modificar a classe de serviço.
- **Dependency Inversion Principle (DIP):** Injeção de dependências estrita via construtor com anotação `@RequiredArgsConstructor` do Lombok, facilitando mocks em testes.
- **Imutabilidade e Segurança de Tipos:** Todos os DTOs de entrada e saída utilizam **Java Records**, eliminando estado mutável entre camadas.

### 3.2 HATEOAS (Hypermedia As The Engine Of Application State)
A API utiliza o **Spring HATEOAS** para fornecer hipermídia de navegação aos clientes:
- Cada recurso retornado por um controller é encapsulado em `EntityModel<T>` ou `CollectionModel<EntityModel<T>>`.
- Os assemblers (`PostAssembler`, `PostFeedAssembler`, `UsuarioAssembler`, etc.) geram links `self`, links para coleções e ações contextuais (como `curtir` ou `respostas`).

### 3.3 Separação de DTOs e Entidades
- **Entidades de domínio (`@Entity`) NUNCA são expostas na camada web.**
- DTOs dedicados para criação/atualização (`*Request`, `*UpdateRequest`) e saída (`*Response`).
- O **MapStruct** realiza a conversão em tempo de compilação, prevenindo vazamento de dados internos (como hash de senha).

---

## 4. Configurações Transversais

### 4.1 Auditoria JPA (`JpaConfig`)
- Habilitada através da anotação `@EnableJpaAuditing`.
- Todas as entidades principais herdam de `BaseEntity`, que preenche automaticamente:
  - `dataCriacao` (`@CreatedDate`, imutável após criação).
  - `dataAtualizacao` (`@LastModifiedDate`, atualizada a cada modificação).

### 4.2 Segurança & CORS (`WebConfig`)
- Configurado via `WebMvcConfigurer` no endpoint padrão `/**`.
- Origem permitida: `http://localhost:4200` (Front-end Angular padrão).
- Métodos permitidos: `GET`, `POST`, `PUT`, `DELETE`, `OPTIONS`.
- Cabeçalhos: Permite qualquer cabeçalho (`*`) e suporte a credenciais (`allowCredentials: true`).

### 4.3 Documentação Swagger/OpenAPI (`OpenApiConfig`)
- Configuração de metadados gerais da API via bean `OpenAPI`:
  - **Título:** API Blog Pessoal
  - **Versão:** v1.0.0
  - **Licença:** Apache 2.0
  - **Contato:** Paulo Passos (`paulo.passos.pereira1@outlook.com`)
- Acesso à interface Swagger: `/swagger-ui.html`
- Acesso ao documento OpenAPI em JSON: `/v3/api-docs`

---

## 5. Padrão Global de Tratamento de Exceções

A aplicação centraliza o tratamento de erros em [`GlobalExceptionHandler`](file:///c:/Users/paulo/Documents/GitHub/api-blog-pessoal/src/main/java/br/com/passos/api_blog_pessoal/exception/GlobalExceptionHandler.java) com `@RestControllerAdvice`.

### 5.1 Estrutura Padronizada (`ErrorResponse`)
```json
{
  "timestamp": "2026-09-12 15:30:00",
  "status": 400,
  "error": "Erro de validação de campos",
  "message": "Um ou mais campos estão inválidos",
  "path": "/api/v1/usuarios",
  "details": {
    "email": "Email inválido",
    "senha": "A senha deve ter no mínimo 8 caracteres"
  }
}
```

### 5.2 Mapeamento de Exceções e Status HTTP

| Exceção Capturada | Status HTTP | Descrição do Erro |
| :--- | :--- | :--- |
| `MethodArgumentNotValidException` | `400 Bad Request` | Falha de validação Bean Validation nos DTOs (`@Valid`). Retorna mapa de campos e mensagens em `details`. |
| `BusinessException` | `400 Bad Request` | Violação de regra de negócio da aplicação (ex: autor inválido, post não encontrado). |
| `EmailJaCadastradoException` | `409 Conflict` | Tentativa de cadastro ou atualização com e-mail já existente. |
| `NoHandlerFoundException` | `404 Not Found` | Rota ou endpoint inexistente. |
| `HttpMessageNotReadableException` | `400 Bad Request` | JSON malformatado ou corpo de requisição ausente. |
| `MissingServletRequestParameterException` | `400 Bad Request` | Parâmetro obrigatório da requisição não informado (query param). |
| `MethodArgumentTypeMismatchException` | `400 Bad Request` | Tipo incompatível de parâmetro (ex: passar texto em ID numérico). |
| `Exception` (Genérica) | `500 Internal Server Error` | Erro não mapeado. Registra stack trace em log (`@Slf4j`) e mascara detalhes para o cliente. |
