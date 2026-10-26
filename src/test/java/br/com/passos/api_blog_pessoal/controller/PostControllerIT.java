package br.com.passos.api_blog_pessoal.controller;

import br.com.passos.api_blog_pessoal.AbstractIntegrationTest;
import br.com.passos.api_blog_pessoal.dto.PostRequest;
import br.com.passos.api_blog_pessoal.model.Role;
import com.fasterxml.jackson.databind.JsonNode;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;

import java.util.ArrayList;
import java.util.List;

import static br.com.passos.api_blog_pessoal.support.TestDataFactory.postRequest;
import static br.com.passos.api_blog_pessoal.support.TestDataFactory.postRequestValido;
import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("PostController (integração)")
class PostControllerIT extends AbstractIntegrationTest {

    private static final String URL = "/api/v1/posts";

    @Nested
    @DisplayName("Criação de Posts")
    class Criacao {

        @Test
        @Tag("BDD-POSTS-01")
        @DisplayName("BDD-POSTS-01: publica post com sucesso por usuário USER, gerando slug e tempo de leitura")
        void deveCriarPostComSucesso_quandoAutorUsuarioComum() {
            // Arrange
            Long autorId = api.criarUsuario("Autor Comum", "autor@teste.com", Role.USER);
            Long categoriaId = api.criarCategoria("Backend");
            var request = postRequest("Primeiro Post no Blog", categoriaId, List.of("Java", "Spring"));

            // Act
            var resposta = api.post(URL + "?autorId=" + autorId, request);

            // Assert
            assertThat(resposta.getStatusCode()).isEqualTo(HttpStatus.CREATED);
            JsonNode corpo = resposta.getBody();
            assertThat(corpo.get("id").asLong()).isPositive();
            assertThat(corpo.get("slug").asText()).isEqualTo("primeiro-post-no-blog");
            assertThat(corpo.get("tempoLeitura").asInt()).isGreaterThanOrEqualTo(1);
            assertThat(corpo.get("nomeAutor").asText()).isEqualTo("Autor Comum");
            assertThat(corpo.at("/categoria/nome").asText()).isEqualTo("Backend");
            assertThat(corpo.at("/tags").size()).isEqualTo(2);

            assertThat(contarLinhas("SELECT COUNT(*) FROM posts WHERE id = ?", corpo.get("id").asLong()))
                    .isEqualTo(1);
        }

        @Test
        @Tag("BDD-POSTS-02")
        @DisplayName("BDD-POSTS-02: rejeita publicação de post por administrador com 400")
        void deveRejeitarPublicacao_quandoAutorForAdmin() {
            // Arrange
            Long adminId = api.criarUsuario("Admin", "admin@teste.com", Role.ADMIN);
            var request = postRequestValido();

            // Act
            var resposta = api.post(URL + "?autorId=" + adminId, request);

            // Assert
            assertThat(resposta.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
            assertThat(resposta.getBody().get("detail").asText())
                    .contains("Administradores não podem criar postagens");
            assertThat(contarLinhas("SELECT COUNT(*) FROM posts")).isZero();
        }

        @Test
        @Tag("BDD-POSTS-03")
        @DisplayName("BDD-POSTS-03: resolve conflito de slug idêntico gerando sufixo numérico")
        void deveGerarSlugComSufixo_quandoTituloDuplicado() {
            // Arrange
            Long autorId = api.criarUsuario(Role.USER);
            var primeiro = api.criarPost(autorId, "Primeiro Post", null, List.of());
            assertThat(primeiro.get("slug").asText()).isEqualTo("primeiro-post");

            // Act
            var segundo = api.criarPost(autorId, "Primeiro Post", null, List.of());

            // Assert
            assertThat(segundo.get("slug").asText()).isEqualTo("primeiro-post-1");
            assertThat(contarLinhas("SELECT COUNT(*) FROM posts WHERE titulo = 'Primeiro Post'")).isEqualTo(2);
        }
    }

    @Nested
    @DisplayName("Atualização de Posts")
    class Atualizacao {

        @Test
        @Tag("BDD-POSTS-04")
        @DisplayName("BDD-POSTS-04: autor original atualiza título e conteúdo do post com sucesso")
        void deveAtualizarPost_quandoAutorOriginal() {
            // Arrange
            Long autorId = api.criarUsuario(Role.USER);
            Long postId = api.criarPost(autorId, "Título Antigo");
            var novoRequest = new PostRequest("Título Atualizado", "Novo conteúdo completo", null, List.of("Dica"));

            // Act
            var resposta = api.put(URL + "/" + postId + "?autorId=" + autorId, novoRequest);

            // Assert
            assertThat(resposta.getStatusCode()).isEqualTo(HttpStatus.OK);
            assertThat(resposta.getBody().get("titulo").asText()).isEqualTo("Título Atualizado");

            String tituloNoBanco = jdbcTemplate.queryForObject(
                    "SELECT titulo FROM posts WHERE id = ?", String.class, postId);
            assertThat(tituloNoBanco).isEqualTo("Título Atualizado");
        }

        @Test
        @Tag("BDD-POSTS-05")
        @DisplayName("BDD-POSTS-05: rejeita atualização de post por outro usuário com 400")
        void deveRejeitarAtualizacao_quandoUsuarioNaoForAutorOriginal() {
            // Arrange
            Long autor1 = api.criarUsuario(Role.USER);
            Long autor2 = api.criarUsuario(Role.USER);
            Long postId = api.criarPost(autor1, "Post do Autor 1");

            var request = new PostRequest("Tentativa Hacker", "Conteúdo modificado", null, List.of());

            // Act
            var resposta = api.put(URL + "/" + postId + "?autorId=" + autor2, request);

            // Assert
            assertThat(resposta.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
            assertThat(resposta.getBody().get("detail").asText())
                    .isEqualTo("Você não tem permissão para realizar esta operação");

            String tituloNoBanco = jdbcTemplate.queryForObject(
                    "SELECT titulo FROM posts WHERE id = ?", String.class, postId);
            assertThat(tituloNoBanco).isEqualTo("Post do Autor 1");
        }
    }

    @Nested
    @DisplayName("Exclusão de Posts")
    class Exclusao {

        @Test
        @Tag("BDD-POSTS-06")
        @DisplayName("BDD-POSTS-06: autor original exclui post e todos os comentários associados são removidos")
        void deveExcluirPostEComentarios_quandoAutorOriginal() {
            // Arrange
            Long autorPost = api.criarUsuario(Role.USER);
            Long autorComentario = api.criarUsuario(Role.USER);
            Long postId = api.criarPost(autorPost, "Post para Excluir");
            api.criarComentario(postId, autorComentario, "Comentário 1");
            api.criarComentario(postId, autorComentario, "Comentário 2");

            assertThat(contarLinhas("SELECT COUNT(*) FROM comentarios WHERE post_id = ?", postId)).isEqualTo(2);

            // Act
            var resposta = api.delete(URL + "/" + postId + "?autorId=" + autorPost);

            // Assert
            assertThat(resposta.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
            assertThat(contarLinhas("SELECT COUNT(*) FROM posts WHERE id = ?", postId)).isZero();
            assertThat(contarLinhas("SELECT COUNT(*) FROM comentarios WHERE post_id = ?", postId)).isZero();
        }

        @Test
        @Tag("BDD-POSTS-07")
        @DisplayName("BDD-POSTS-07: rejeita exclusão por outro usuário com 400 e mantém o post intacto")
        void deveRejeitarExclusao_quandoUsuarioNaoForAutorOriginal() {
            // Arrange
            Long autorPost = api.criarUsuario(Role.USER);
            Long outroUsuario = api.criarUsuario(Role.USER);
            Long postId = api.criarPost(autorPost, "Post Protegido");

            // Act
            var resposta = api.delete(URL + "/" + postId + "?autorId=" + outroUsuario);

            // Assert
            assertThat(resposta.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
            assertThat(contarLinhas("SELECT COUNT(*) FROM posts WHERE id = ?", postId)).isEqualTo(1);
        }
    }

    @Nested
    @DisplayName("Curtidas")
    class Curtidas {

        @Test
        @Tag("BDD-POSTS-08")
        @DisplayName("BDD-POSTS-08: curte post pela primeira vez incrementando total de curtidas")
        void deveCurtirPostPelaPrimeiraVez() {
            // Arrange
            Long autor = api.criarUsuario(Role.USER);
            Long usuario = api.criarUsuario(Role.USER);
            Long postId = api.criarPost(autor, "Post Para Curtir");

            // Act
            var resposta = api.post(URL + "/" + postId + "/curtir?usuarioId=" + usuario, null);

            // Assert
            assertThat(resposta.getStatusCode()).isEqualTo(HttpStatus.OK);
            assertThat(contarLinhas("SELECT COUNT(*) FROM posts_curtidas WHERE post_id = ? AND usuario_id = ?",
                    postId, usuario)).isEqualTo(1);

            var postDetalhe = api.get(URL + "/" + postId).getBody();
            assertThat(postDetalhe.get("totalCurtidas").asInt()).isEqualTo(1);
        }

        @Test
        @Tag("BDD-POSTS-09")
        @DisplayName("BDD-POSTS-09: descurte post previamente curtido (toggle)")
        void deveDescurtirPost_quandoJaCurtido() {
            // Arrange
            Long autor = api.criarUsuario(Role.USER);
            Long usuario = api.criarUsuario(Role.USER);
            Long postId = api.criarPost(autor, "Post Toggle");
            api.post(URL + "/" + postId + "/curtir?usuarioId=" + usuario, null);
            assertThat(contarLinhas("SELECT COUNT(*) FROM posts_curtidas WHERE post_id = ?", postId)).isEqualTo(1);

            // Act (segunda chamada descurte)
            var resposta = api.post(URL + "/" + postId + "/curtir?usuarioId=" + usuario, null);

            // Assert
            assertThat(resposta.getStatusCode()).isEqualTo(HttpStatus.OK);
            assertThat(contarLinhas("SELECT COUNT(*) FROM posts_curtidas WHERE post_id = ?", postId)).isZero();

            var postDetalhe = api.get(URL + "/" + postId).getBody();
            assertThat(postDetalhe.get("totalCurtidas").asInt()).isZero();
        }
    }

    @Nested
    @DisplayName("Feed e Consultas")
    class FeedEConsultas {

        @Test
        @Tag("BDD-POSTS-10")
        @DisplayName("BDD-POSTS-10: consulta ao feed paginado por cursor retorna posts anteriores ao lastId")
        void deveRetornarPostsPaginadosPorCursor_noFeed() {
            // Arrange
            Long autorId = api.criarUsuario(Role.USER);
            for (int i = 1; i <= 5; i++) {
                api.criarPost(autorId, "Post " + i);
            }

            Long ultimoId = jdbcTemplate.queryForObject("SELECT MAX(id) FROM posts", Long.class);

            // Act
            var resposta = api.get(URL + "/feed?lastId=" + ultimoId + "&size=2");

            // Assert
            assertThat(resposta.getStatusCode()).isEqualTo(HttpStatus.OK);
            JsonNode items = resposta.getBody().get("content");
            assertThat(items.size()).isEqualTo(2);
            long idPrimeiro = items.get(0).get("id").asLong();
            long idSegundo = items.get(1).get("id").asLong();
            assertThat(idPrimeiro).isLessThan(ultimoId);
            assertThat(idSegundo).isLessThan(idPrimeiro);
        }

        @Test
        @Tag("BDD-POSTS-11")
        @DisplayName("BDD-POSTS-11: busca post por slug existente")
        void deveBuscarPostPorSlugExistente() {
            // Arrange
            Long autorId = api.criarUsuario(Role.USER);
            api.criarPost(autorId, "Artigo Spring Boot");

            // Act
            var resposta = api.get(URL + "/slug/artigo-spring-boot");

            // Assert
            assertThat(resposta.getStatusCode()).isEqualTo(HttpStatus.OK);
            assertThat(resposta.getBody().get("titulo").asText()).isEqualTo("Artigo Spring Boot");
            assertThat(resposta.getBody().get("slug").asText()).isEqualTo("artigo-spring-boot");
        }

        @Test
        @DisplayName("[TEC] pesquisa dinâmica filtra por título, conteúdo, categoria e tag")
        void devePesquisarPostsDinamicamente() {
            // Arrange
            Long autorId = api.criarUsuario(Role.USER);
            Long catJava = api.criarCategoria("Java");
            Long catOutra = api.criarCategoria("Outra");

            api.criarPost(autorId, "Spring Web", catJava, List.of("Framework"));
            api.criarPost(autorId, "Spring Data", catJava, List.of("Database"));
            api.criarPost(autorId, "Python Fast", catOutra, List.of("Framework"));

            // Act
            var resposta = api.get(URL + "/search?titulo=Spring&categoria=java&tag=Framework");

            // Assert
            assertThat(resposta.getStatusCode()).isEqualTo(HttpStatus.OK);
            JsonNode lista = resposta.getBody().at("/_embedded/postResponseList");
            assertThat(lista.size()).isEqualTo(1);
            assertThat(lista.get(0).get("titulo").asText()).isEqualTo("Spring Web");
        }
    }
}
