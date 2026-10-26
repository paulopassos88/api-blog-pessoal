package br.com.passos.api_blog_pessoal.controller;

import br.com.passos.api_blog_pessoal.AbstractIntegrationTest;
import br.com.passos.api_blog_pessoal.dto.ComentarioRequest;
import br.com.passos.api_blog_pessoal.model.Role;
import com.fasterxml.jackson.databind.JsonNode;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;

import static br.com.passos.api_blog_pessoal.support.TestDataFactory.comentarioRequest;
import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("ComentarioController (integração)")
class ComentarioControllerIT extends AbstractIntegrationTest {

    private static final String BASE_URL = "/api/v1";

    @Nested
    @DisplayName("Criação de Comentários")
    class Criacao {

        @Test
        @Tag("BDD-COMENTARIOS-01")
        @DisplayName("BDD-COMENTARIOS-01: comentário raiz publicado com sucesso com pai_id nulo no banco")
        void devePublicarComentarioRaizComSucesso() {
            // Arrange
            Long autorPost = api.criarUsuario(Role.USER);
            Long autorComentario = api.criarUsuario(Role.USER);
            Long postId = api.criarPost(autorPost, "Post para Comentar");

            // Act
            var resposta = api.post(
                    BASE_URL + "/posts/" + postId + "/comentarios?autorId=" + autorComentario,
                    comentarioRequest("Excelente publicação!"));

            // Assert
            assertThat(resposta.getStatusCode()).isEqualTo(HttpStatus.CREATED);
            JsonNode corpo = resposta.getBody();
            assertThat(corpo.get("id").asLong()).isPositive();
            assertThat(corpo.get("texto").asText()).isEqualTo("Excelente publicação!");
            assertThat(corpo.at("/_links/responder/href").asText()).contains("/comentarios/");
            assertThat(corpo.at("/_links/curtir/href").asText()).contains("/curtir");

            Long paiId = jdbcTemplate.queryForObject(
                    "SELECT pai_id FROM comentarios WHERE id = ?", Long.class, corpo.get("id").asLong());
            assertThat(paiId).isNull();
        }

        @Test
        @Tag("BDD-COMENTARIOS-02")
        @DisplayName("BDD-COMENTARIOS-02: rejeita comentário publicado por usuário administrador com 400")
        void deveRejeitarComentario_quandoAutorForAdmin() {
            // Arrange
            Long autorPost = api.criarUsuario(Role.USER);
            Long adminId = api.criarUsuario(Role.ADMIN);
            Long postId = api.criarPost(autorPost, "Post no Blog");

            // Act
            var resposta = api.post(
                    BASE_URL + "/posts/" + postId + "/comentarios?autorId=" + adminId,
                    comentarioRequest("Comentário proibido do admin"));

            // Assert
            assertThat(resposta.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
            assertThat(resposta.getBody().get("detail").asText())
                    .contains("Administradores não podem comentar");
            assertThat(contarLinhas("SELECT COUNT(*) FROM comentarios")).isZero();
        }

        @Test
        @DisplayName("[TEC] rejeita texto em branco com 400")
        void deveRetornar400_quandoTextoEmBranco() {
            // Arrange
            Long autorPost = api.criarUsuario(Role.USER);
            Long autorComentario = api.criarUsuario(Role.USER);
            Long postId = api.criarPost(autorPost, "Post Válido");

            // Act
            var resposta = api.post(
                    BASE_URL + "/posts/" + postId + "/comentarios?autorId=" + autorComentario,
                    new ComentarioRequest("   "));

            // Assert
            assertThat(resposta.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
            assertThat(resposta.getBody().at("/details/texto").isMissingNode()).isFalse();
        }
    }

    @Nested
    @DisplayName("Respostas Aninhadas")
    class Respostas {

        @Test
        @Tag("BDD-COMENTARIOS-03")
        @DisplayName("BDD-COMENTARIOS-03: resposta vinculada ao comentário-pai correto no banco")
        void deveResponderComentarioComSucesso() {
            // Arrange
            Long autorPost = api.criarUsuario(Role.USER);
            Long autorComentario = api.criarUsuario(Role.USER);
            Long autorResposta = api.criarUsuario(Role.USER);
            Long postId = api.criarPost(autorPost, "Post com Resposta");
            Long comentarioPaiId = api.criarComentario(postId, autorComentario, "Comentário Original");

            // Act
            var resposta = api.post(
                    BASE_URL + "/comentarios/" + comentarioPaiId + "/respostas?autorId=" + autorResposta,
                    comentarioRequest("Concordo plenamente!"));

            // Assert
            assertThat(resposta.getStatusCode()).isEqualTo(HttpStatus.CREATED);
            JsonNode corpo = resposta.getBody();
            long respostaId = corpo.get("id").asLong();

            Long paiIdNoBanco = jdbcTemplate.queryForObject(
                    "SELECT pai_id FROM comentarios WHERE id = ?", Long.class, respostaId);
            assertThat(paiIdNoBanco).isEqualTo(comentarioPaiId);
        }
    }

    @Nested
    @DisplayName("Listagem Hierárquica")
    class Listagem {

        @Test
        @Tag("BDD-COMENTARIOS-04")
        @DisplayName("BDD-COMENTARIOS-04: lista apenas comentários raiz no nível superior com respostas embutidas")
        void deveListarComentariosHierarquicamente() {
            // Arrange
            Long autorPost = api.criarUsuario(Role.USER);
            Long autor1 = api.criarUsuario(Role.USER);
            Long autor2 = api.criarUsuario(Role.USER);
            Long postId = api.criarPost(autorPost, "Post para Discussão");

            Long paiId = api.criarComentario(postId, autor1, "Comentário Raiz");
            api.responderComentario(paiId, autor2, "Primeira Resposta");
            api.responderComentario(paiId, autor1, "Segunda Resposta");

            // Act
            var resposta = api.get(BASE_URL + "/posts/" + postId + "/comentarios");

            // Assert
            assertThat(resposta.getStatusCode()).isEqualTo(HttpStatus.OK);
            JsonNode lista = resposta.getBody().at("/_embedded/comentarioResponseList");
            assertThat(lista.size()).isEqualTo(1); // Apenas o nó raiz no nível superior
            assertThat(lista.get(0).get("id").asLong()).isEqualTo(paiId);

            JsonNode respostas = lista.get(0).get("respostas");
            assertThat(respostas.size()).isEqualTo(2); // Ambas as respostas embutidas
        }
    }

    @Nested
    @DisplayName("Curtidas")
    class Curtidas {

        @Test
        @Tag("BDD-COMENTARIOS-05")
        @DisplayName("BDD-COMENTARIOS-05: curte comentário pela primeira vez incrementando total de curtidas")
        void deveCurtirComentarioPelaPrimeiraVez() {
            // Arrange
            Long autorPost = api.criarUsuario(Role.USER);
            Long autorComentario = api.criarUsuario(Role.USER);
            Long usuario = api.criarUsuario(Role.USER);
            Long postId = api.criarPost(autorPost, "Post Válido");
            Long comentarioId = api.criarComentario(postId, autorComentario, "Comentário Bacana");

            // Act
            var resposta = api.post(
                    BASE_URL + "/comentarios/" + comentarioId + "/curtir?usuarioId=" + usuario, null);

            // Assert
            assertThat(resposta.getStatusCode()).isEqualTo(HttpStatus.OK);
            assertThat(contarLinhas("SELECT COUNT(*) FROM comentarios_curtidas WHERE comentario_id = ? AND usuario_id = ?",
                    comentarioId, usuario)).isEqualTo(1);
        }

        @Test
        @Tag("BDD-COMENTARIOS-06")
        @DisplayName("BDD-COMENTARIOS-06: descurte comentário previamente curtido (toggle)")
        void deveDescurtirComentario_quandoJaCurtido() {
            // Arrange
            Long autorPost = api.criarUsuario(Role.USER);
            Long autorComentario = api.criarUsuario(Role.USER);
            Long usuario = api.criarUsuario(Role.USER);
            Long postId = api.criarPost(autorPost, "Post Válido");
            Long comentarioId = api.criarComentario(postId, autorComentario, "Comentário Bacana");

            api.post(BASE_URL + "/comentarios/" + comentarioId + "/curtir?usuarioId=" + usuario, null);
            assertThat(contarLinhas("SELECT COUNT(*) FROM comentarios_curtidas WHERE comentario_id = ?",
                    comentarioId)).isEqualTo(1);

            // Act (segunda chamada)
            var resposta = api.post(
                    BASE_URL + "/comentarios/" + comentarioId + "/curtir?usuarioId=" + usuario, null);

            // Assert
            assertThat(resposta.getStatusCode()).isEqualTo(HttpStatus.OK);
            assertThat(contarLinhas("SELECT COUNT(*) FROM comentarios_curtidas WHERE comentario_id = ?",
                    comentarioId)).isZero();
        }
    }
}
