# 🤖 Diretrizes de Atuação da IA no Projeto

Este documento estabelece as regras de conduta, padrões arquiteturais e fluxos de trabalho para agentes de Inteligência Artificial que atuam no repositório **API Blog Pessoal**.

---

## 1. Postura de Pair Programming & Governança

1. **Autorização Prévia Obrigatória:**
   - **NUNCA** escreva, altere ou delete código de produção ou de testes sem autorização explícita do desenvolvedor.
   - Apresente planos técnicos, justifique impactos arquiteturais e aguarde o "de acordo".
2. **Atuação Cirúrgica e Incremental:**
   - Execute tarefas em etapas granulares, mantendo a estabilidade contínua do projeto.
3. **Cautela com Componentes Críticos:**
   - Redobre o cuidado ao propor alterações em configurações de segurança, transações de banco de dados (`@Transactional`) ou migrações do Flyway.

---

## 2. Fluxo de Consulta à Especificação

Antes de propor alterações ou gerar novas funcionalidades:
1. **Consulte a Spec do Módulo:** Leia cirurgicamente o arquivo do domínio específico em `docs/modules/[modulo].md` para carregar apenas o contexto estritamente necessário na janela de contexto.
2. **Consulte o Documento-Mestre:** Caso precise de visão global de relacionamentos ou rotas compartilhadas, consulte `docs/spec.md`.
3. **Mantenha a Spec Atualizada:** Sempre que uma regra de negócio, entidade, DTO ou endpoint for modificado no código, a respectiva especificação técnica **deve** ser atualizada antes da conclusão da tarefa.

---

## 3. Padrões de Código & Arquitetura

- **Stack Oficial:** Java 21 (LTS) e Spring Boot 3.5.14. Utilizar estritamente o namespace `jakarta.*` (nunca `javax.*`).
- **Arquitetura em Camadas:** Controller → Service → Repository.
- **DTOs Imutáveis:** Entidades JPA (`@Entity`) **NUNCA** devem ser expostas na camada de apresentação (Controllers). Utilize sempre `record` para DTOs de entrada e saída.
- **Transformação de Dados:** Utilize **MapStruct** para mappers estáticos em tempo de compilação.
- **Tratamento de Exceções:** Centralizado em [`GlobalExceptionHandler`](file:///c:/Users/paulo/Documents/GitHub/api-blog-pessoal/src/main/java/br/com/passos/api_blog_pessoal/exception/GlobalExceptionHandler.java) com `@RestControllerAdvice`, padronizando o payload em `ErrorResponse`.
- **Clean Code & Injeção:** Injeção de dependências via construtor com `@RequiredArgsConstructor` do Lombok. Mantenha complexidade ciclomática baixa (máximo 2 níveis de aninhamento) e adote *early returns*.

---

## 4. Testes Automatizados Orientados a BDD

1. **Implementação Sob Demanda:** Só implemente testes quando solicitado expressamente pelo usuário.
2. **Cobertura dos Cenários BDD:** Ao criar ou ajustar testes (`JUnit 5`, `Mockito`, `Testcontainers`), cada caso de teste deve cobrir diretamente um cenário formal descrito na seção **Critérios de Aceite (BDD)** da especificação do módulo correspondente (`Dado / Quando / Então`).
3. **Padrão AAA:** Estruture testes unitários e de integração seguindo claramente as fases *Arrange*, *Act* e *Assert*.

---

## 5. Git & Versionamento

1. **Commits Controlados:** **NUNCA** execute `git commit` ou `git push` de forma autônoma. Solicite aprovação prévia.
2. **Branches e Git Flow:** Commits diretos na `main` ou `master` são estritamente proibidos. As alterações devem transitar por branches `feature/*` e `bugfix/*` integrando em `develop` (ou `dev`).
3. **Conventional Commits:** Adote a convenção `<tipo>(<escopo>): <descrição no imperativo>` (ex: `feat(posts): adiciona calculo de tempo de leitura`).
