package br.com.passos.api_blog_pessoal.controller;

import br.com.passos.api_blog_pessoal.AbstractIntegrationTest;
import br.com.passos.api_blog_pessoal.dto.UsuarioRequest;
import br.com.passos.api_blog_pessoal.model.Role;
import com.fasterxml.jackson.databind.JsonNode;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;

import java.util.ArrayList;
import java.util.List;

import static br.com.passos.api_blog_pessoal.support.TestDataFactory.*;
import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("UsuarioController (integração)")
class UsuarioControllerIT extends AbstractIntegrationTest {

    private static final String URL = "/api/v1/usuarios";

    @Nested
    @DisplayName("Cadastro")
    class Cadastro {

        @Test
        @Tag("BDD-USUARIOS-01")
        @DisplayName("BDD-USUARIOS-01: persiste o usuário, armazena a senha e não a devolve na resposta")
        void deveCadastrarUsuario_quandoDadosValidos() {
            // Arrange
            var request = usuarioRequest("Autor Teste", "autor@teste.com", Role.USER);

            // Act
            var resposta = api.post(URL, request);

            // Assert
            assertThat(resposta.getStatusCode()).isEqualTo(HttpStatus.CREATED);
            JsonNode corpo = resposta.getBody();
            assertThat(corpo.get("email").asText()).isEqualTo("autor@teste.com");
            assertThat(corpo.get("role").asText()).isEqualTo("USER");
            assertThat(corpo.has("senha")).isFalse();
            assertThat(corpo.at("/_links/self/href").asText()).contains("/busca-por-email?email=autor%40teste.com");

            String senhaPersistida = jdbcTemplate.queryForObject(
                    "SELECT senha FROM usuarios WHERE id = ?", String.class, corpo.get("id").asLong());
            assertThat(senhaPersistida).isEqualTo(SENHA_VALIDA);
        }

        @Test
        @Tag("BDD-USUARIOS-02")
        @DisplayName("BDD-USUARIOS-02: rejeita e-mail duplicado com 409")
        void deveRetornar409_quandoEmailJaCadastrado() {
            // Arrange
            api.criarUsuario("Existente", "usuario@teste.com", Role.USER);

            // Act
            var resposta = api.post(URL, usuarioRequest("Outro Nome", "usuario@teste.com", Role.USER));

            // Assert
            assertThat(resposta.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);
            assertThat(resposta.getBody().get("detail").asText()).isEqualTo("Email já cadastrado");
            assertThat(contarLinhas("SELECT COUNT(*) FROM usuarios")).isEqualTo(1);
        }

        @Test
        @DisplayName("[TEC] rejeita dados inválidos com 400 listando todos os campos inválidos")
        void deveRetornar400_quandoDadosInvalidos() {
            // Act
            var resposta = api.post(URL, new UsuarioRequest("ab", "invalido", "123", null));

            // Assert
            assertThat(resposta.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
            JsonNode details = resposta.getBody().get("details");
            assertThat(details.has("nome")).isTrue();
            assertThat(details.has("email")).isTrue();
            assertThat(details.has("senha")).isTrue();
            assertThat(details.has("role")).isTrue();
            assertThat(contarLinhas("SELECT COUNT(*) FROM usuarios")).isZero();
        }
    }

    @Nested
    @DisplayName("Atualização")
    class Atualizacao {

        @Test
        @Tag("BDD-USUARIOS-03")
        @DisplayName("BDD-USUARIOS-03: ADMIN atualiza os dados do usuário alvo")
        void deveAtualizarUsuario_quandoExecutorAdmin() {
            // Arrange
            Long adminId = api.criarUsuario("Admin", "admin@teste.com", Role.ADMIN);
            Long alvoId = api.criarUsuario("Alvo", "alvo@teste.com", Role.USER);
            var request = usuarioUpdateRequest("Alvo Atualizado", "alvo.novo@teste.com", Role.USER);

            // Act
            var resposta = api.put(URL + "/" + alvoId + "?executorId=" + adminId, request);

            // Assert
            assertThat(resposta.getStatusCode()).isEqualTo(HttpStatus.OK);
            assertThat(resposta.getBody().get("nome").asText()).isEqualTo("Alvo Atualizado");
            assertThat(contarLinhas("SELECT COUNT(*) FROM usuarios WHERE id = ? AND nome = ? AND email = ?",
                    alvoId, "Alvo Atualizado", "alvo.novo@teste.com")).isEqualTo(1);
        }

        @Test
        @Tag("BDD-USUARIOS-04")
        @DisplayName("BDD-USUARIOS-04: executor USER é bloqueado com 400 e nada é alterado")
        void deveRetornar400_quandoExecutorNaoAdmin() {
            // Arrange
            Long userId = api.criarUsuario("Comum", "comum@teste.com", Role.USER);
            Long alvoId = api.criarUsuario("Alvo", "alvo@teste.com", Role.USER);

            // Act
            var resposta = api.put(URL + "/" + alvoId + "?executorId=" + userId,
                    usuarioUpdateRequest("Hacker", "hacker@teste.com", Role.ADMIN));

            // Assert
            assertThat(resposta.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
            assertThat(resposta.getBody().get("detail").asText())
                    .isEqualTo("Somente administradores podem atualizar usuários");
            assertThat(contarLinhas("SELECT COUNT(*) FROM usuarios WHERE id = ? AND nome = 'Alvo'", alvoId))
                    .isEqualTo(1);
        }

        @Test
        @Tag("BDD-USUARIOS-05")
        @DisplayName("BDD-USUARIOS-05: atualizar para e-mail de outro usuário retorna 409")
        void deveRetornar409_quandoEmailPertenceAOutroUsuario() {
            // Arrange
            Long adminId = api.criarUsuario("Admin", "admin@teste.com", Role.ADMIN);
            api.criarUsuario("Usuario A", "a@teste.com", Role.USER);
            Long usuarioB = api.criarUsuario("Usuario B", "b@teste.com", Role.USER);

            // Act
            var resposta = api.put(URL + "/" + usuarioB + "?executorId=" + adminId,
                    usuarioUpdateRequest("Usuario B", "a@teste.com", Role.USER));

            // Assert
            assertThat(resposta.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);
            assertThat(contarLinhas("SELECT COUNT(*) FROM usuarios WHERE id = ? AND email = 'b@teste.com'", usuarioB))
                    .isEqualTo(1);
        }
    }

    @Nested
    @DisplayName("Exclusão")
    class Exclusao {

        @Test
        @Tag("BDD-USUARIOS-06")
        @DisplayName("BDD-USUARIOS-06: ADMIN exclui o usuário permanentemente com 204")
        void deveExcluirUsuario_quandoExecutorAdmin() {
            // Arrange
            Long adminId = api.criarUsuario("Admin", "admin@teste.com", Role.ADMIN);
            Long alvoId = api.criarUsuario("Alvo", "alvo@teste.com", Role.USER);

            // Act
            var resposta = api.delete(URL + "/" + alvoId + "?executorId=" + adminId);

            // Assert
            assertThat(resposta.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
            assertThat(contarLinhas("SELECT COUNT(*) FROM usuarios WHERE id = ?", alvoId)).isZero();
        }

        @Test
        @Tag("BDD-USUARIOS-07")
        @DisplayName("BDD-USUARIOS-07: executor USER não consegue excluir (400) e o registro permanece")
        void deveRetornar400_quandoExecutorNaoAdmin() {
            // Arrange
            Long userId = api.criarUsuario("Comum", "comum@teste.com", Role.USER);
            Long alvoId = api.criarUsuario("Alvo", "alvo@teste.com", Role.USER);

            // Act
            var resposta = api.delete(URL + "/" + alvoId + "?executorId=" + userId);

            // Assert
            assertThat(resposta.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
            assertThat(contarLinhas("SELECT COUNT(*) FROM usuarios WHERE id = ?", alvoId)).isEqualTo(1);
        }
    }

    @Nested
    @DisplayName("Buscas")
    class Buscas {

        @Test
        @Tag("BDD-USUARIOS-08")
        @DisplayName("BDD-USUARIOS-08: busca por e-mail existente retorna 200 com os dados")
        void deveRetornarUsuario_quandoEmailExiste() {
            // Arrange
            Long id = api.criarUsuario("Busca", "busca@teste.com", Role.USER);

            // Act
            var resposta = api.get(URL + "/busca-por-email?email=busca@teste.com");

            // Assert
            assertThat(resposta.getStatusCode()).isEqualTo(HttpStatus.OK);
            assertThat(resposta.getBody().get("id").asLong()).isEqualTo(id);
            assertThat(resposta.getBody().get("nome").asText()).isEqualTo("Busca");
        }

        @Test
        @Tag("BDD-USUARIOS-09")
        @DisplayName("BDD-USUARIOS-09: busca por e-mail inexistente retorna 400")
        void deveRetornar400_quandoEmailInexistente() {
            // Act
            var resposta = api.get(URL + "/busca-por-email?email=inexistente@teste.com");

            // Assert
            assertThat(resposta.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
            assertThat(resposta.getBody().get("detail").asText())
                    .isEqualTo("Usuário não encontrado com o email informado");
        }

        @Test
        @Tag("BDD-USUARIOS-10")
        @DisplayName("BDD-USUARIOS-10: busca por termo no nome é case-insensitive e retorna todos os correspondentes")
        void deveRetornarUsuariosCorrespondentes_quandoBuscarPorNome() {
            // Arrange
            api.criarUsuario("João Silva", "joao@teste.com", Role.USER);
            api.criarUsuario("Maria Silva", "maria@teste.com", Role.USER);
            api.criarUsuario("Pedro Souza", "pedro@teste.com", Role.USER);

            // Act
            var resposta = api.get(URL + "/busca-por-nome?nome=silva");

            // Assert
            assertThat(resposta.getStatusCode()).isEqualTo(HttpStatus.OK);
            List<String> nomes = new ArrayList<>();
            resposta.getBody().at("/_embedded/usuarioResponseList").forEach(u -> nomes.add(u.get("nome").asText()));
            assertThat(nomes).containsExactlyInAnyOrder("João Silva", "Maria Silva");
        }
    }
}
