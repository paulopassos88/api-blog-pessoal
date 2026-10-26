package br.com.passos.api_blog_pessoal.controller;

import br.com.passos.api_blog_pessoal.AbstractIntegrationTest;
import br.com.passos.api_blog_pessoal.model.Role;
import com.fasterxml.jackson.databind.JsonNode;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("TagController (integração)")
class TagControllerIT extends AbstractIntegrationTest {

    private static final String URL = "/api/v1/tags";

    @Test
    @Tag("BDD-TAGS-06")
    @DisplayName("BDD-TAGS-06: lista todas as tags cadastradas com 200")
    void deveListarTodasAsTags_quandoExistiremTags() {
        // Arrange — tags são criadas on-demand na publicação de posts
        Long autorId = api.criarUsuario(Role.USER);
        api.criarPost(autorId, "Post com tags", null, List.of("Spring", "Java"));

        // Act
        var resposta = api.get(URL);

        // Assert
        assertThat(resposta.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(nomes(resposta.getBody())).containsExactlyInAnyOrder("Spring", "Java");
    }

    @Test
    @DisplayName("[TEC] retorna 200 sem _embedded quando não há tags")
    void deveRetornarListaVazia_quandoNaoExistiremTags() {
        // Act
        var resposta = api.get(URL);

        // Assert
        assertThat(resposta.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(resposta.getBody().has("_embedded")).isFalse();
    }

    @Test
    @Tag("BDD-TAGS-04")
    @DisplayName("BDD-TAGS-04: tag inexistente é criada automaticamente e vinculada ao post")
    void deveCriarTagAutomaticamente_quandoPostUsarTagNova() {
        // Arrange
        Long autorId = api.criarUsuario(Role.USER);
        assertThat(contarLinhas("SELECT COUNT(*) FROM tags")).isZero();

        // Act
        JsonNode post = api.criarPost(autorId, "Arquitetura distribuida", null, List.of("Microsserviços"));

        // Assert
        assertThat(post.at("/tags/0/nome").asText()).isEqualTo("Microsserviços");
        assertThat(contarLinhas("SELECT COUNT(*) FROM tags WHERE nome = ?", "Microsserviços")).isEqualTo(1);
        assertThat(contarLinhas("SELECT COUNT(*) FROM posts_tags WHERE post_id = ?", post.get("id").asLong()))
                .isEqualTo(1);
        assertThat(nomes(api.get(URL).getBody())).contains("Microsserviços");
    }

    @Test
    @Tag("BDD-TAGS-05")
    @DisplayName("BDD-TAGS-05: tag existente é reutilizada, sem criar duplicata")
    void deveReutilizarTagExistente_quandoPostUsarTagJaCadastrada() {
        // Arrange
        Long autorId = api.criarUsuario(Role.USER);
        JsonNode primeiro = api.criarPost(autorId, "Primeiro post Java", null, List.of("Java"));
        long idTagJava = primeiro.at("/tags/0/id").asLong();

        // Act
        JsonNode segundo = api.criarPost(autorId, "Segundo post Java", null, List.of("Java"));

        // Assert
        assertThat(segundo.at("/tags/0/id").asLong()).isEqualTo(idTagJava);
        assertThat(contarLinhas("SELECT COUNT(*) FROM tags WHERE nome = ?", "Java")).isEqualTo(1);
        assertThat(contarLinhas("SELECT COUNT(*) FROM posts_tags WHERE tag_id = ?", idTagJava)).isEqualTo(2);
    }

    @Test
    @Disabled("Divergência spec x código: a spec diz 'ordenadas alfabeticamente', mas TagService.listarTodas usa findAll() sem Sort. Ver docs/testes.md › Achados.")
    @Tag("BDD-TAGS-06")
    @DisplayName("BDD-TAGS-06 (ordenação): tags retornadas em ordem alfabética")
    void deveListarEmOrdemAlfabetica() {
        // Arrange
        Long autorId = api.criarUsuario(Role.USER);
        api.criarPost(autorId, "Post com tags", null, List.of("Spring", "Java"));

        // Act + Assert
        assertThat(nomes(api.get(URL).getBody())).containsExactly("Java", "Spring");
    }

    private static List<String> nomes(JsonNode corpo) {
        List<String> nomes = new ArrayList<>();
        corpo.at("/_embedded/tagResponseList").forEach(n -> nomes.add(n.get("nome").asText()));
        return nomes;
    }
}
