## 🛠 Stack & Ambiente
- **Framework:** Spring Boot 3.2+ (Java 17 ou 21)
- **Build:** Gradle (Kotlin DSL) / Maven (ajuste conforme seu projeto)
- **Arquitetura:** Package-by-feature + Camadas (Controller → Service → Repository)
- **Banco:** PostgreSQL + Spring Data JPA (Hibernate 6)
- **API:** RESTful, JSON, OpenAPI 3.0 via SpringDoc
- **Segurança:** Spring Security 6, JWT stateless, @PreAuthorize
- **Testes:** JUnit 5, Mockito, Testcontainers, @SpringBootTest + MockMvc

## 📝 Convenções de Código
- Use **injeção por construtor** (`@RequiredArgsConstructor` do Lombok ou manual)
- **Nunca** expira `@Entity` no controller. Sempre mapeie para DTOs (`record` preferível)
- Validação: `@Valid` no controller + `@NotBlank`, `@Email`, `@Size` nos DTOs
- Tratamento de erros: `@RestControllerAdvice` + `ErrorResponse` padronizado
- Entidades: `@Data`/`@Builder` com cuidado; prefira getters/setters explícitos se houver lógica
- Logging: `@Slf4j`, use JSON em produção, evite `System.out.println`
- Imports: **SEMPRE `jakarta.*`**, nunca `javax.*` (Spring Boot 3+)
- Use os padrões **SOLID** para o desenvolvimento do código

## 🔒 Segurança & Boas Práticas
- Secrets via variáveis de ambiente ou `application-{profile}.yml` (nunca commitar)
- Spring Security: configurar via `@Bean SecurityFilterChain`, stateless para APIs
- Método-level security: `@EnableMethodSecurity` + `@PreAuthorize("hasRole('ADMIN')")`
- Validar headers, path variables e query params
- CORS explícito em `WebSecurityConfig` ou `@CrossOrigin` controlado

## 🧪 Padrão de Testes
- **Unitários:** Mockar repositórios, testar regras de negócio puras
- **Integração:** `@SpringBootTest` + `@AutoConfigureMockMvc` + `@Transactional` (rollback)
- **Infra:** Testcontainers para DB, Redis, Kafka em testes reais
- Seguir padrão AAA (Arrange, Act, Assert)
- Cobrir: happy path, validações, 404, 401/403, rollback de transação
- Só implementar teste quando for solicitado. 

## 🤖 Instruções para a IA
1. Gere código compatível com **Spring Boot 3+** (`jakarta.persistence`, etc.)
2. Prefira soluções nativas do Spring antes de bibliotecas externas
3. Ao alterar `application.yml`, especifique qual profile está sendo modificado
4. Sempre inclua imports e explique anotações novas
5. Se sugerir uma migração de banco, inclua script Flyway/Liquibase ou explique o impacto
6. Pare e pergunte antes de modificar configurações de segurança ou transações globais

## 📦 Estrutura Esperada
```
src/main/java/br/com/passos/api_blog_pessoal/
├── config/              # SecurityConfig, WebConfig, OpenApiConfig
├── controller/          # @RestController (User, Point, Report)
├── dto/                 # Request/Response DTOs
├── exception/           # GlobalExceptionHandler, ResourceNotFoundException
├── mapper/              # MapStruct or manual mappers
├── model/               # @Entity (User, Point, PointEvent)
├── repository/          # JpaRepository interfaces
├── security/            # JwtTokenProvider, JwtAuthFilter, UserDetailsImpl
├── service/             # @Service (UserService, PointService, ReportService)
└── util/                # JwtUtils, DateUtils, etc.
```