## 🛠 Stack & Ambiente

- **Framework:** Spring Boot 3.2+ (Java 17 ou 21)
- **Build:** Gradle (Kotlin DSL) / Maven (ajuste conforme seu projeto)
- **Arquitetura:** Package-by-feature + Camadas (Controller → Service → Repository)
- **Banco:** PostgreSQL + Spring Data JPA (Hibernate 6)
- **API:** RESTful, JSON, OpenAPI 3.0 via SpringDoc
- **Segurança:** Spring Security 6, JWT stateless, @PreAuthorize
- **Testes:** JUnit 5, Mockito, Testcontainers, @SpringBootTest + MockMvc

## 📝 Convenções de Código e Qualidade

- Use **injeção por construtor** (`@RequiredArgsConstructor` do Lombok ou manual).
- **Nunca** exponha `@Entity` no controller. Sempre mapeie para DTOs (`record` preferível).
- Validação: `@Valid` no controller + `@NotBlank`, `@Email`, `@Size` nos DTOs.
- Tratamento de erros: `@RestControllerAdvice` + `ErrorResponse` padronizado.
- Entidades: `@Data`/`@Builder` com cuidado; prefira getters/setters explícitos se houver lógica.
- Logging: `@Slf4j`, use JSON em produção, evite `System.out.println`.
- Imports: **SEMPRE `jakarta.*`**, nunca `javax.*` (Spring Boot 3+).
- Use os padrões **SOLID** para o desenvolvimento do código.
- **Métricas e Complexidade:** Mantenha a Complexidade Ciclomática baixa. Evite aninhamentos profundos (máximo de 2 níveis de `if/for`), utilizando retornos antecipados (*Early Returns*).
- **Code Smells e Duplicação:** Respeite o princípio DRY (*Don't Repeat Yourself*). Classes devem ter alta coesão e baixo acoplamento (delegando responsabilidades corretamente entre Controller, Service e Repository).

## 🔒 Segurança & Boas Práticas

- Secrets via variáveis de ambiente ou `application-{profile}.yml` (nunca commitar).
- Spring Security: configurar via `@Bean SecurityFilterChain`, stateless para APIs.
- Método-level security: `@EnableMethodSecurity` + `@PreAuthorize("hasRole('ADMIN')")`.
- Validar headers, path variables e query params.
- CORS explícito em `WebSecurityConfig` ou `@CrossOrigin` controlado.

## 🧪 Padrão de Testes

- **Unitários:** Mockar repositórios, testar regras de negócio puras.
- **Integração:** `@SpringBootTest` + `@AutoConfigureMockMvc` + `@Transactional` (rollback).
- **Infra:** Testcontainers para DB, Redis, Kafka em testes reais.
- Seguir padrão AAA (Arrange, Act, Assert).
- Cobrir: happy path, validações, 404, 401/403, rollback de transação.
- **Cobertura (Code Coverage):** Quando autorizado a criar testes, mire em cobrir todas as ramificações lógicas (*branch coverage*) dos métodos, preparando a base para relatórios de ferramentas como o **JaCoCo**.
- Só implementar teste quando for solicitado explícitamente.

## 🛡️ Decisões Arquiteturais

### Autenticação & Autorização: Spring Security + JWT

Optamos pelo **Spring Security + JWT** em vez do Keycloak pelos seguintes motivos:

- **Eficiência de Recursos:** Ideal para deploy em VPS com hardware limitado (baixo consumo de RAM comparado ao Keycloak).
- **Simplicidade de Infraestrutura:** Menor complexidade de deploy, mantendo toda a lógica de segurança dentro do JAR da aplicação.
- **Controle Total:** Facilita a integração direta com a entidade `Usuario` já existente e customização de regras de negócio específicas do blog.
- **Frontend-Ready:** Suporte nativo a CORS e autenticação stateless, perfeita para consumo por SPAs (React/Angular/Vue).

## 🤖 Instruções para a IA

1. Gere código compatível com **Spring Boot 3+** (`jakarta.persistence`, etc.).
2. Prefira soluções nativas do Spring antes de bibliotecas externas.
3. Ao alterar `application.yml`, especifique qual profile está sendo modificado.
4. Sempre inclua imports e explique anotações novas.
5. Se sugerir uma migração de banco, inclua script Flyway/Liquibase ou explique o impacto.
6. Pare e pergunte antes de modificar configurações de segurança ou transações globais.
7. **Atue como Revisor de Código:** Ao ler trechos de código enviados, avalie mentalmente as métricas de qualidade (Complexidade Ciclomática, Code Smells e Coesão) e aponte sugestões de refatoração, explicando o "porquê" de forma prática, mas aguarde aprovação para reescrever.

## Diretrizes e Regras de Desenvolvimento (Gemini)

Para qualquer interação neste projeto, o assistente deve seguir estritamente as regras abaixo:

1. **Autorização Prévia para Implementação de Código**:
    - Não escreva, altere ou exclua qualquer código de produção sem a autorização prévia e explícita do usuário.
    - Apresente as propostas e soluções, detalhando o impacto nas métricas de qualidade, antes de aplicá-las.

2. **Autorização Prévia para Criação ou Alteração de Testes**:
    - Não adicione novos testes unitários, de integração ou modifique testes existentes sem autorização explícita do usuário.

3. **Autorização Prévia para Commits**:
    - Não execute comandos do Git para realizar commits (`git commit`) ou enviar alterações (`git push`) sem a autorização prévia e explícita do usuário.
    - Sempre confirme com o usuário se ele deseja commitar as alterações feitas antes de executar tais comandos.

## Monitoramento de APIs Spring Boot
- [Spring Boot Actuator](https://docs.spring.io/spring-boot/docs/current/reference/html/production-ready-features.html)
- [Spring Boot Admin](https://github.com/codecentric/spring-boot-admin)
- [Spring Boot Metrics](https://docs.spring.io/spring-boot/docs/current/reference/html/production-ready-features.html#production-ready-metrics)
- [Spring Boot Actuator + Prometheus](https://www.baeldung.com/spring-boot-actuator-prometheus)
- [Spring Boot Metrics + Prometheus](https://www.baeldung.com/spring-boot-metrics-prometheus)
- [Prometheus + Grafana](https://www.baeldung.com/prometheus-grafana)
- [Prometheus + Grafana + Alertmanager](https://www.baeldung.com/prometheus-grafana-alertmanager)