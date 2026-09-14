# 📦 Módulo: Infraestrutura, Migrações & Observabilidade

Este módulo especifica a infraestrutura de banco de dados, histórico de migrações Flyway, conteinerização Docker, orquestração com Docker Compose, métricas de observabilidade e estratégia de testes de integração na **API Blog Pessoal**.

---

## 1. Estruturas de Dados & Migrações Flyway

### 1.1 Linha do Tempo de Migrações (`db/migration`)

| Versão | Arquivo SQL | Propósito |
| :--- | :--- | :--- |
| **V1** | `V1__create_table_usuarios.sql` | Tabela `usuarios`, constraints de integridade e auditoria temporal. |
| **V2** | `V2__create_tables_posts_comentarios.sql` | Tabelas fundamentais `posts` e `comentarios` com FKs e `ON DELETE CASCADE`. |
| **V3** | `V3__add_categories_tags_and_slug.sql` | Tabelas `categorias`, `tags`, associação `posts_tags` e slugs únicos. |
| **V4** | `V4__add_likes_and_threaded_comments.sql` | Auto-relacionamento `pai_id` em comentários e tabelas de curtidas (`posts_curtidas` e `comentarios_curtidas`). |
| **V5** | `V5__insert_dados_teste.sql` | Carga inicial de desenvolvimento para usuários, posts, categorias e comentários. |

### 1.2 Perfis de Execução
- **`dev` (H2 Database):** Banco em memória (`jdbc:h2:mem:blogdb`), console ativo em `/h2-console`, `ddl-auto: validate`.
- **`prod` (PostgreSQL 16):** Banco relacional PostgreSQL em container Docker, `ddl-auto: validate`.
- **`test` (Testcontainers):** Container dinâmico oficial `postgres:16-alpine` instanciado durante os testes de integração.

---

## 2. Regras Operacionais (RN)

- **RN-OPS-01 (Multi-Stage Build Otimizado):** O [`Dockerfile`](file:///c:/Users/paulo/Documents/GitHub/api-blog-pessoal/Dockerfile) deve utilizar build em dois estágios. O estágio final de execução deve conter unicamente o JRE enxuto (`eclipse-temurin:21-jre-alpine`), eliminando compiladores e ferramentas de compilação da imagem final para reduzir o tamanho e a superfície de vulnerabilidade.
- **RN-OPS-02 (Orquestração e Resiliência de Inicialização):** No [`docker-compose.yml`](file:///c:/Users/paulo/Documents/GitHub/api-blog-pessoal/docker-compose.yml), a inicialização do container da aplicação (`api`) depende obrigatoriamente da integridade prévia do banco de dados (`depends_on: db: condition: service_healthy` avaliado via `pg_isready`).
- **RN-OPS-03 (Scrape de Métricas Padronizado):** O Spring Boot Actuator expõe o endpoint `/actuator/prometheus`. O servidor Prometheus efetua scraping em intervalos configurados de 5 segundos (`scrape_interval: 5s`).
- **RN-OPS-04 (Isolamento Hermético de Testes com Testcontainers):** Todos os testes de integração que herdam de [`AbstractIntegrationTest`](file:///c:/Users/paulo/Documents/GitHub/api-blog-pessoal/src/test/java/br/com/passos/api_blog_pessoal/AbstractIntegrationTest.java) devem executar em container real do PostgreSQL com limpeza total de tabelas no `@BeforeEach` via `TRUNCATE TABLE ... RESTART IDENTITY CASCADE`.

---

## 3. Contratos de API (Endpoints de Gerenciamento)

### Endpoints Actuator

#### `GET /actuator/health`
- **Permissão:** Pública
- **Status:** `200 OK`
- **Payload Exemplo:**
```json
{
  "status": "UP",
  "components": {
    "db": {
      "status": "UP",
      "details": { "database": "PostgreSQL", "validationQuery": "isValid()" }
    },
    "diskSpace": {
      "status": "UP"
    },
    "ping": {
      "status": "UP"
    }
  }
}
```

#### `GET /actuator/info`
- **Permissão:** Pública
- **Status:** `200 OK`
- **Descrição:** Metadados do projeto e versão do runtime.

#### `GET /actuator/prometheus`
- **Permissão:** Pública
- **Status:** `200 OK`
- **Formato:** Texto plano (Padrão OpenMetrics).

---

## 4. Critérios de Aceite (BDD / Cenários de Teste)

### Cenário 1: Diagnóstico de integridade da API e banco
- **Dado** que a aplicação está em execução com o banco de dados conectado,
- **Quando** for requisitado `GET /actuator/health`,
- **Então** a resposta deve retornar status `200 OK` com `"status": "UP"`.

### Cenário 2: Exposição de telemetria para o Prometheus
- **Dado** que a aplicação está processando requisições REST,
- **Quando** o agente Prometheus consulta `GET /actuator/prometheus`,
- **Então** o sistema deve retornar as métricas no formato padrão de scraping com status `200 OK`.

### Cenário 3: Isolamento hermético em suíte de testes de integração
- **Dado** que um teste de integração insere registros no banco de dados do Testcontainers,
- **Quando** o método de teste é concluído e o próximo método inicia,
- **Então** o método `@BeforeEach cleanDatabase()` deve executar o truncate de todas as tabelas, assegurando estado zerado para o novo teste.
