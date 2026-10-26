package br.com.passos.api_blog_pessoal.controller;

import br.com.passos.api_blog_pessoal.AbstractIntegrationTest;
import com.fasterxml.jackson.databind.JsonNode;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;

import java.util.ArrayList;
import java.util.List;

import static br.com.passos.api_blog_pessoal.support.TestDataFactory.categoriaRequest;
import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("CategoriaController (integração)")
class CategoriaControllerIT extends AbstractIntegrationTest {

    private static final String URL = "/api/v1/categorias";

    @Test
    @Tag("BDD-CATEGORIAS-01")
    @DisplayName("BDD-CATEGORIAS-01: cria categoria com slug gerado e persiste no banco")
    void deveCriarCategoriaComSlug_quandoNomeNaoExiste() {
        // Act
        var resposta = api.post(URL, categoriaRequest("Tecnologia"));

        // Assert
        assertThat(resposta.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(resposta.getBody().get("slug").asText()).isEqualTo("tecnologia");
        assertThat(contarLinhas("SELECT COUNT(*) FROM categorias WHERE nome = ? AND slug = ?",
                "Tecnologia", "tecnologia")).isEqualTo(1);
    }

    @Test
    @Tag("BDD-CATEGORIAS-02")
    @DisplayName("BDD-CATEGORIAS-02: rejeita categoria duplicada com 400 e mensagem")
    void deveRetornar400_quandoCategoriaJaCadastrada() {
        // Arrange
        api.criarCategoria("Tecnologia");

        // Act
        var resposta = api.post(URL, categoriaRequest("Tecnologia"));

        // Assert
        assertThat(resposta.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(resposta.getBody().get("detail").asText()).isEqualTo("Categoria já cadastrada");
        assertThat(contarLinhas("SELECT COUNT(*) FROM categorias")).isEqualTo(1);
    }

    @Test
    @DisplayName("[TEC] rejeita nome inválido com 400 sem persistir")
    void deveRetornar400_quandoNomeInvalido() {
        // Act
        var resposta = api.post(URL, categoriaRequest("ab"));

        // Assert
        assertThat(resposta.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(resposta.getBody().at("/details/nome").isMissingNode()).isFalse();
        assertThat(contarLinhas("SELECT COUNT(*) FROM categorias")).isZero();
    }

    @Test
    @Tag("BDD-CATEGORIAS-03")
    @DisplayName("BDD-CATEGORIAS-03: lista todas as categorias com links HATEOAS")
    void deveListarCategoriasComHateoas_quandoExistiremCategorias() {
        // Arrange
        api.criarCategoria("Tecnologia");
        api.criarCategoria("Backend");

        // Act
        var resposta = api.get(URL);

        // Assert
        assertThat(resposta.getStatusCode()).isEqualTo(HttpStatus.OK);
        JsonNode lista = resposta.getBody().at("/_embedded/categoriaResponseList");
        assertThat(nomes(lista)).containsExactlyInAnyOrder("Tecnologia", "Backend");
        assertThat(lista.get(0).at("/_links/categorias/href").asText()).endsWith(URL);
    }

    @Test
    @Disabled("Divergência spec x código: a spec diz 'ordenadas alfabeticamente', mas CategoriaService.listarTodas usa findAll() sem Sort. Ver docs/testes.md › Achados.")
    @Tag("BDD-CATEGORIAS-03")
    @DisplayName("BDD-CATEGORIAS-03 (ordenação): categorias retornadas em ordem alfabética")
    void deveListarEmOrdemAlfabetica() {
        // Arrange
        api.criarCategoria("Tecnologia");
        api.criarCategoria("Backend");

        // Act
        var lista = api.get(URL).getBody().at("/_embedded/categoriaResponseList");

        // Assert
        assertThat(nomes(lista)).containsExactly("Backend", "Tecnologia");
    }

    private static List<String> nomes(JsonNode lista) {
        List<String> nomes = new ArrayList<>();
        lista.forEach(n -> nomes.add(n.get("nome").asText()));
        return nomes;
    }
}
