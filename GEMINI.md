# 🪐 Skill Antigravity: Guia Unificado de Desenvolvimento

> **Versão:** 3.0 | **Foco:** Spring Boot 3+, Git Flow Seguro e Qualidade de Código.
> Este documento consolida as diretrizes de arquitetura, fluxo de trabalho e regras de interação para a IA.

---

## 🤖 1. Regras de Interação da IA (Obrigatório)

1. **Autorização Prévia (Código e Testes):** 
   - **NUNCA** escreva, altere ou exclua código de produção ou testes sem autorização explícita do usuário.
   - Apresente propostas, explique o impacto na qualidade e aguarde o "de acordo".
2. **Autorização Prévia (Git):** 
   - **NUNCA** execute `git commit` ou `git push` sem autorização explícita.
3. **Postura de Revisor:** 
   - Ao ler código, avalie mentalmente Complexidade Ciclomática, Code Smells e SOLID. Sugira refatorações explicando o "porquê", mas aguarde aprovação para reescrever.
4. **Cautela com Críticos:** 
   - Pare e pergunte antes de modificar configurações de segurança (`SecurityFilterChain`) ou transações globais.
5. **Testes:** 
   - Só implemente testes se o usuário solicitar explicitamente.
6. **Entregáveis:** 
   - Sempre inclua imports, explique anotações novas e, ao sugerir migrações de banco, inclua scripts Flyway/Liquibase.
7. **Atualização Contínua de Especificações (`doc/spec`):**
   - **SEMPRE** que o código for alterado (entidades, regras de negócio, DTOs, endpoints, banco ou infraestrutura) e **OBRIGATORIAMENTE antes de qualquer commit**, atualize os arquivos de especificação correspondentes (`dock/spec/<modulo>/spec.md`) e o arquivo consolidado mestre (`dock/spec/spec.md`).

---

## 🛠 2. Stack & Arquitetura

- **Core:** Spring Boot 3.2+ (Java 17 ou 21). **Atenção:** Use sempre `jakarta.*`, nunca `javax.*`.
- **Build:** Gradle (Kotlin DSL) ou Maven.
- **Dados:** PostgreSQL + Spring Data JPA (Hibernate 6).
- **API:** RESTful, JSON, OpenAPI 3.0 (SpringDoc).
- **Segurança:** Spring Security 6 + JWT stateless. *(Decisão: Evita Keycloak para economizar RAM em VPS e manter controle total da entidade `Usuario`)*.
- **Monitoramento:** Spring Boot Actuator integrado a Prometheus e Grafana.

---

## 🌳 3. Git Flow & Versionamento

### 🛑 A Regra de Ouro
> **NUNCA faça commits diretos na `main` ou `master`.** Toda alteração chega via Pull Request (PR).
> **Só fazer commits quando for solicitado.**
> **Antes de qualquer commit:** Atualize obrigatoriamente as especificações em `dock/spec/` caso o código tenha sido modificado.

### Topologia Simplificada
- `main`: Espelho da produção estável.
- `develop`: Branch de integração contínua.
- `feature/*` e `bugfix/*`: Nascem e morrem em `develop`.
- `release/*`: Nasce em `develop`, merge para `main` e volta para `develop`.
- `hotfix/*`: Nasce em `main`, merge para `main` (com TAG) e volta para `develop`.

### Convenção de Commits (Conventional Commits)
- **Formato:** `<tipo>(<escopo>): <descrição imperativa e curta>`
- **Tipos:** `feat` (MINOR), `fix`/`perf` (PATCH), `docs`, `style`, `refactor`, `test`, `chore`.
- **Regras:** Commits atômicos, máx. 72 caracteres na 1ª linha. Nunca commite `.env` ou `node_modules`.

### Pull Requests
- Exige CI verde, cobertura ≥ 80%, sem conflitos e ao menos 1 aprovação.
- Deve conter: O quê, Porquê, Ticket (ANT-XXXX) e Como testar.

---

## 💻 4. Qualidade de Código & Padrões

### Estrutura e Design
- **Arquitetura:** Package-by-feature + Camadas (Controller → Service → Repository).
- **Princípios:** SOLID, DRY, alta coesão e baixo acoplamento.
- **Clean Code:** Baixa complexidade ciclomática. Máximo de **2 níveis de aninhamento** (`if/for`). Use *Early Returns*.

### Boas Práticas Spring
- **Injeção:** Via construtor (`@RequiredArgsConstructor`).
- **DTOs:** **NUNCA** exponha `@Entity` no controller. Use `record` para DTOs.
- **Validação:** `@Valid` no controller + Bean Validation (`@NotBlank`, `@Email`, etc.) nos DTOs.
- **Erros:** Tratamento global via `@RestControllerAdvice` + `ErrorResponse` padronizado.
- **Logging:** `@Slf4j` (JSON em produção). Nunca use `System.out.println`.

---

## 🔒 5. Segurança

- **Secrets:** Variáveis de ambiente ou `application-{profile}.yml` (nunca commitar).
- **Configuração:** `@Bean SecurityFilterChain` (stateless para APIs).
- **Autorização:** `@EnableMethodSecurity` + `@PreAuthorize("hasRole('ADMIN')")`.
- **CORS e Inputs:** Configuração explícita no `WebSecurityConfig` e validação rigorosa de headers/params.

---

## 🧪 6. Estratégia de Testes

*(Lembrete: Só implementar se autorizado)*

- **Frameworks:** JUnit 5, Mockito, Testcontainers (para DB/Redis/Kafka).
- **Padrão:** AAA (Arrange, Act, Assert).
- **Cobertura:** Foco em *branch coverage* (preparando para JaCoCo).
- **Cenários Obrigatórios:** Happy path, validações de erro, 404, 401/403, e rollback de transação.
- **Integração:** `@SpringBootTest` + `@AutoConfigureMockMvc` + `@Transactional` (rollback automático).

---

## 📚 7. Referências e Links Úteis
- **Monitoramento:** [Spring Boot Actuator](https://docs.spring.io/spring-boot/docs/current/reference/html/production-ready-features.html) | [Prometheus & Grafana](https://www.baeldung.com/prometheus-grafana)
- **Git Flow:** [Modelo Original (nvie)](https://nvie.com/posts/a-successful-git-branching-model/)
- **Commits:** [Conventional Commits 1.0](https://www.conventionalcommits.org/)
- **Versionamento:** [Semantic Versioning 2.0](https://semver.org/)