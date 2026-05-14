# 📝 API Blog Pessoal - MVP Portfolio

Esta é uma API RESTful robusta desenvolvida com **Spring Boot 3** e **Java 21**, projetada para servir como o backend de um sistema de blog pessoal. O projeto foca em boas práticas de arquitetura, segurança e performance para implantação em ambientes de produção (VPS).

## 🚀 Tecnologias & Arquitetura

- **Linguagem:** Java 21 (Long Term Support).
- **Framework:** Spring Boot 3.2+.
- **Banco de Dados:**
  - **Desenvolvimento:** H2 Database (em memória).
  - **Produção:** PostgreSQL 16.
- **Migrações:** Flyway para controle de versão do banco de dados.
- **Segurança:** Spring Security (preparado para JWT).
- **Mapeamento:** MapStruct para conversão entre Entidades e DTOs.
- **Infraestrutura:** Docker e Docker Compose (Multi-stage build).
- **Testes:** JUnit 5 e Mockito (Camada de Service).

## 🛠️ Funcionalidades Principais

- **Gestão de Usuários:** Cadastro e atualização de perfis com controle de permissões (ADMIN/USER).
- **Gestão de Postagens:**
  - CRUD completo com validação de propriedade (apenas o autor pode editar/excluir seus posts).
  - Restrição de criação: Apenas usuários comuns podem criar posts (Admins gerenciam a plataforma).
- **Feed Otimizado (Infinite Scroll Ready):**
  - Implementação de **Keyset Pagination (Cursor)** para evitar duplicidade de itens no scroll.
  - Uso de **Spring Data Slice** para eliminar consultas de `COUNT(*)` pesadas, otimizando o uso de memória na VPS.
  - Resumo de conteúdo gerado diretamente via SQL (`SUBSTRING`).
- **Dados Iniciais:** Carregamento automático de registros fictícios para testes rápidos via Flyway.

## 📦 Como executar o projeto

### Usando Docker (Recomendado para Produção)
Para subir a API e o banco PostgreSQL simultaneamente:
```bash
docker-compose up -d --build
```
A API estará disponível em `http://localhost:8080`.

### Localmente (Desenvolvimento)
Certifique-se de ter o JDK 21 instalado:
```bash
./gradlew bootRun
```
O projeto iniciará usando o profile `dev` (H2 Database) e o console do H2 estará disponível em `http://localhost:8080/h2-console`.

## 📡 Principais Endpoints

- **Feed:** `GET /api/v1/posts/feed?lastId={id}&size=10`
- **Posts:** `GET|POST|PUT|DELETE /api/v1/posts`
- **Usuários:** `GET|POST|PUT|DELETE /api/v1/usuarios`

## 🛡️ Decisões Técnicas
- **Package-by-feature**: Organização que facilita a escalabilidade.
- **DTO Records**: Uso de Java Records para imutabilidade e concisão.
- **S.O.L.I.D**: Princípios aplicados para garantir código limpo e testável.
