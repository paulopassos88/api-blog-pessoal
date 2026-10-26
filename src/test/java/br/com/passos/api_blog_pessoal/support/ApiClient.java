package br.com.passos.api_blog_pessoal.support;

import br.com.passos.api_blog_pessoal.dto.CategoriaRequest;
import br.com.passos.api_blog_pessoal.dto.ComentarioRequest;
import br.com.passos.api_blog_pessoal.dto.PostRequest;
import br.com.passos.api_blog_pessoal.model.Role;
import com.fasterxml.jackson.databind.JsonNode;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Cliente HTTP dos testes de integração.
 * <p>
 * Oferece atalhos para montar pré-condições (Arrange) chamando a própria API,
 * garantindo que os dados passem pelas mesmas regras de negócio do uso real.
 * Todas as respostas são lidas como {@link JsonNode}.
 */
public class ApiClient {

    private final TestRestTemplate rest;
    private final AtomicInteger sequencia = new AtomicInteger();

    public ApiClient(TestRestTemplate rest) {
        this.rest = rest;
    }

    // ---------- Verbos genéricos ----------

    public ResponseEntity<JsonNode> get(String url) {
        return rest.getForEntity(url, JsonNode.class);
    }

    public ResponseEntity<JsonNode> post(String url, Object body) {
        return rest.postForEntity(url, body, JsonNode.class);
    }

    public ResponseEntity<JsonNode> put(String url, Object body) {
        return rest.exchange(url, HttpMethod.PUT, new HttpEntity<>(body), JsonNode.class);
    }

    public ResponseEntity<JsonNode> delete(String url) {
        return rest.exchange(url, HttpMethod.DELETE, null, JsonNode.class);
    }

    // ---------- Pré-condições ----------

    public Long criarUsuario(String nome, String email, Role role) {
        var resposta = post("/api/v1/usuarios", TestDataFactory.usuarioRequest(nome, email, role));
        return idCriado(resposta);
    }

    /** Cria usuário com e-mail único gerado automaticamente. */
    public Long criarUsuario(Role role) {
        int n = sequencia.incrementAndGet();
        return criarUsuario("Usuario " + n, "usuario" + n + "@teste.com", role);
    }

    public Long criarCategoria(String nome) {
        return idCriado(post("/api/v1/categorias", new CategoriaRequest(nome)));
    }

    public JsonNode criarPost(Long autorId, String titulo, Long categoriaId, List<String> tags) {
        var request = new PostRequest(titulo, "Conteúdo de teste do post " + titulo, categoriaId, tags);
        var resposta = post("/api/v1/posts?autorId=" + autorId, request);
        assertThat(resposta.getStatusCode()).as("pré-condição: criar post").isEqualTo(HttpStatus.CREATED);
        return resposta.getBody();
    }

    public Long criarPost(Long autorId, String titulo) {
        return criarPost(autorId, titulo, null, List.of()).get("id").asLong();
    }

    public Long criarComentario(Long postId, Long autorId, String texto) {
        var url = "/api/v1/posts/" + postId + "/comentarios?autorId=" + autorId;
        return idCriado(post(url, new ComentarioRequest(texto)));
    }

    public Long responderComentario(Long comentarioId, Long autorId, String texto) {
        var url = "/api/v1/comentarios/" + comentarioId + "/respostas?autorId=" + autorId;
        return idCriado(post(url, new ComentarioRequest(texto)));
    }

    private Long idCriado(ResponseEntity<JsonNode> resposta) {
        assertThat(resposta.getStatusCode()).as("pré-condição: recurso criado").isEqualTo(HttpStatus.CREATED);
        return resposta.getBody().get("id").asLong();
    }
}
