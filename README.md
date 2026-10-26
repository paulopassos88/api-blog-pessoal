# 📝 API Blog Pessoal

API RESTful desenvolvida com **Java 21 (LTS)** e **Spring Boot 3.5**, projetada como backend completo para uma plataforma de blog pessoal e publicação de artigos técnicos. O projeto aplica boas práticas de engenharia de software, navegabilidade hipermídia (**HATEOAS**), otimização de consultas para ambientes de recursos restritos (VPS), testes de integração reais com **Testcontainers** e stack de observabilidade nativa (**Prometheus + Grafana**).

---

## 🚀 Tecnologias & Stack

- **Linguagem:** Java 21 (LTS) — uso extensivo de `Records` imutáveis.
- **Framework:** Spring Boot 3.5.14 (`Web`, `Data JPA`, `Validation`, `HATEOAS`, `Actuator`).
- **Banco de Dados:**
  - **Desenvolvimento (`dev`):** H2 Database (em memória).
  - **Produção (`prod`) e Testes de Integração (`test`):** PostgreSQL 16.
- **Migrações de Banco:** Flyway (`V1` a `V5` com versionamento de schema e carga inicial).
- **Mapeamento Objeto-Relacional / DTO:** Hibernate 6 + MapStruct 1.6.3 (geração em tempo de compilação, sem reflexão).
- **Hipermídia REST:** Spring HATEOAS (Modelo de Maturidade de Richardson — Nível 3).
- **Documentação Interativa:** SpringDoc OpenAPI 3 / Swagger UI (`2.8.5`).
- **Observabilidade:** Spring Boot Actuator + Micrometer Prometheus + Grafana.
- **Testes Automatizados:** JUnit 5, Mockito (testes unitários) e Testcontainers com PostgreSQL 16 (testes de integração).
- **Containerização:** Docker (Multi-stage build com usuário não-root e tuning de JVM) e Docker Compose.

---

## 🛠️ Funcionalidades Implementadas

- **Gestão de Usuários (`/api/v1/usuarios`):**
  - Cadastro com validação de unicidade de e-mail (`409 Conflict`).
  - Busca por e-mail exato e busca textual parcial por nome (`ILIKE`).
  - Controle de alçada por papel (`Role.ADMIN` vs `Role.USER`) para atualização e exclusão.
- **Gestão de Postagens (`/api/v1/posts`):**
  - CRUD completo com validação de propriedade (apenas o próprio autor com perfil `USER` edita/exclui seus posts).
  - **Validação desacoplada (Strategy Pattern):** Administradores gerenciam a plataforma e são impedidos de publicar artigos ou comentar.
  - **Slugs SEO-friendly:** Geração automática via normalização Unicode (`NFD`) com resolução automática de colisões (`slug`, `slug-1`, `slug-2`...).
  - **Tempo de Leitura:** Calculado dinamicamente no mapeamento (base de 200 palavras/minuto).
  - **Busca Dinâmica (`JPA Specifications`):** Filtros combináveis por título, conteúdo, slug de categoria e nome de tag, com sanitização contra *Wildcard Injection* (`%`, `_`, `\`).
- **Feed Otimizado para Infinite Scroll (`/api/v1/posts/feed`):**
  - **Keyset Pagination (Cursor por `lastId`):** Complexidade constante $O(1)$ evitando o custo de `OFFSET` em tabelas grandes e eliminando itens duplicados no scroll.
  - **Spring Data `Slice`:** Elimina queries extras de `COUNT(*)` no banco.
  - **Projeção Direta em DTO (JPQL):** Resumo gerado no banco via `SUBSTRING(p.conteudo, 1, 200)` sem trafegar o `TEXT` completo pela rede.
- **Comentários Hierárquicos / Threaded Comments (`/api/v1/comentarios`):**
  - Comentários-raiz em postagens e árvore recursiva de respostas aninhadas (auto-relacionamento `pai_id`).
- **Sistema de Curtidas Idempotente (`Toggle`):**
  - Curtir/descurtir tanto para postagens quanto para comentários (`POST .../curtir`), garantido por tabelas associativas com chave primária composta (`Set<Usuario>`).
- **Taxonomia de Categorias e Tags (`/api/v1/categorias`, `/api/v1/tags`):**
  - Categorias únicas com geração automática de slug.
  - Tags com criação sob demanda (*find-or-create*) transparente no momento da publicação do post.

---

## 📦 Como Executar o Projeto

### 1. Localmente (Modo Desenvolvimento — H2 em Memória)
Pré-requisito: **JDK 21** instalado.

```bash
./gradlew bootRun
```
- **API:** `http://localhost:8080`
- **Swagger UI:** `http://localhost:8080/swagger-ui.html`
- **H2 Console:** `http://localhost:8080/h2-console` (JDBC URL: `jdbc:h2:mem:blogdb`, User: `sa`, Password: *vazio*)

### 2. Usando Docker Compose (Produção — PostgreSQL + Prometheus + Grafana)
Antes de subir os containers, crie o arquivo `.env` na raiz do projeto a partir do modelo `.env_exemplo`:

```bash
cp .env_exemplo .env
docker compose up -d --build
```

Serviços disponibilizados:
| Serviço | Porta / URL | Descrição |
| :--- | :--- | :--- |
| **API Spring Boot** | `http://localhost:8080` | Aplicação principal (profile `prod`) |
| **Swagger UI** | `http://localhost:8080/swagger-ui.html` | Documentação interativa OpenAPI 3 |
| **PostgreSQL 16** | `localhost:5432` | Banco de dados relacional com healthcheck |
| **Prometheus** | `http://localhost:9090` | Coleta de métricas de `/actuator/prometheus` |
| **Grafana** | `http://localhost:3000` | Dashboards de observabilidade (senha padrão: `admin`) |

### 3. Executando os Testes Automatizados
Para rodar a suíte completa (testes unitários + testes de integração via Testcontainers, requer Docker ativo):

```bash
./gradlew test
```

---

## 📡 Principais Endpoints da API

| Método | Rota | Descrição |
| :--- | :--- | :--- |
| `POST` | `/api/v1/usuarios` | Cadastra um novo usuário |
| `GET` | `/api/v1/usuarios/busca-por-email?email={email}` | Busca usuário pelo e-mail |
| `GET` | `/api/v1/usuarios/busca-por-nome?nome={nome}` | Busca usuários por parte do nome |
| `PUT` | `/api/v1/usuarios/{id}?executorId={adminId}` | Atualiza usuário (requer perfil `ADMIN`) |
| `DELETE` | `/api/v1/usuarios/{id}?executorId={adminId}` | Exclui usuário (requer perfil `ADMIN`) |
| `POST` | `/api/v1/posts?autorId={userId}` | Publica um novo artigo (requer perfil `USER`) |
| `GET` | `/api/v1/posts/feed?lastId={id}&size=10` | Feed otimizado com paginação por cursor |
| `GET` | `/api/v1/posts` | Lista todas as postagens |
| `GET` | `/api/v1/posts/{id}` | Busca postagem por ID |
| `GET` | `/api/v1/posts/slug/{slug}` | Busca postagem por slug amigável |
| `GET` | `/api/v1/posts/search?titulo=&conteudo=&categoria=&tag=` | Pesquisa dinâmica multi-critério |
| `PUT` | `/api/v1/posts/{id}?autorId={userId}` | Atualiza postagem (apenas o autor) |
| `DELETE` | `/api/v1/posts/{id}?autorId={userId}` | Remove postagem (apenas o autor) |
| `POST` | `/api/v1/posts/{id}/curtir?usuarioId={userId}` | Alterna curtida (like/unlike) no post |
| `POST` | `/api/v1/posts/{postId}/comentarios?autorId={userId}` | Cria comentário-raiz em um post |
| `GET` | `/api/v1/posts/{postId}/comentarios` | Lista árvore hierárquica de comentários do post |
| `POST` | `/api/v1/comentarios/{id}/respostas?autorId={userId}` | Responde a um comentário existente |
| `POST` | `/api/v1/comentarios/{id}/curtir?usuarioId={userId}` | Alterna curtida (like/unlike) no comentário |
| `POST` | `/api/v1/categorias` | Cria uma nova categoria |
| `GET` | `/api/v1/categorias` | Lista todas as categorias |
| `GET` | `/api/v1/tags` | Lista todas as tags cadastradas |

> 💡 Uma coleção completa do Postman está disponível na raiz do repositório em [`api_blog_pessoal_postman_collection.json`](api_blog_pessoal_postman_collection.json).

---

## 📚 Documentação Detalhada

- [**Explicação Detalhada do Projeto (`EXPLICACAO_PROJETO.md`)**](EXPLICACAO_PROJETO.md) — Análise profunda da arquitetura, camadas, modelo de dados, regras de negócio, queries e pontos de evolução.
- [**Especificação Técnica (`docs/spec.md`)**](docs/spec.md) — Visão geral da arquitetura, diagrama ER e matriz de endpoints.
- **Módulos de Domínio:**
  - [Módulo de Usuários](docs/modules/usuarios.md)
  - [Módulo de Posts & Feed](docs/modules/posts.md)
  - [Módulo de Comentários](docs/modules/comentarios.md)
  - [Módulo de Categorias & Tags](docs/modules/categorias-tags.md)
  - [Infraestrutura, DevOps & Qualidade](docs/modules/infra-ops.md)
