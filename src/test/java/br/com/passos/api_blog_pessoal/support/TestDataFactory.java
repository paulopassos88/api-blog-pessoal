package br.com.passos.api_blog_pessoal.support;

import br.com.passos.api_blog_pessoal.dto.*;
import br.com.passos.api_blog_pessoal.model.Role;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Fábrica de DTOs válidos usada pelos testes unitários e de integração.
 * Centraliza os dados de teste para que cada teste altere apenas o que é relevante ao cenário.
 */
public final class TestDataFactory {

    public static final LocalDateTime DATA_FIXA = LocalDateTime.of(2026, 1, 15, 10, 30, 0);
    public static final String SENHA_VALIDA = "senha1234";

    private TestDataFactory() {
    }

    // ---------- Usuários ----------

    public static UsuarioRequest usuarioRequest(String nome, String email, Role role) {
        return new UsuarioRequest(nome, email, SENHA_VALIDA, role);
    }

    public static UsuarioRequest usuarioRequestValido() {
        return usuarioRequest("Autor Teste", "autor@teste.com", Role.USER);
    }

    public static UsuarioUpdateRequest usuarioUpdateRequest(String nome, String email, Role role) {
        return new UsuarioUpdateRequest(nome, email, role);
    }

    public static UsuarioResponse usuarioResponse(Long id, String nome, String email, Role role) {
        return new UsuarioResponse(id, nome, email, role, DATA_FIXA);
    }

    // ---------- Categorias e Tags ----------

    public static CategoriaRequest categoriaRequest(String nome) {
        return new CategoriaRequest(nome);
    }

    public static CategoriaResponse categoriaResponse(Long id, String nome, String slug) {
        return new CategoriaResponse(id, nome, slug);
    }

    public static TagResponse tagResponse(Long id, String nome) {
        return new TagResponse(id, nome);
    }

    // ---------- Posts ----------

    public static PostRequest postRequest(String titulo, Long categoriaId, List<String> tags) {
        return new PostRequest(titulo, "Conteúdo de teste com algumas palavras para leitura.", categoriaId, tags);
    }

    public static PostRequest postRequestValido() {
        return postRequest("Primeiro Post", null, List.of());
    }

    public static PostResponse postResponse(Long id, String titulo, String slug) {
        return new PostResponse(id, titulo, slug, "Conteúdo de teste", "Autor Teste",
                categoriaResponse(1L, "Tecnologia", "tecnologia"),
                List.of(tagResponse(1L, "Java")), 0, 1, DATA_FIXA);
    }

    public static PostFeedResponse postFeedResponse(Long id, String titulo) {
        return new PostFeedResponse(id, titulo, "slug-" + id, "Resumo", "Autor Teste", DATA_FIXA, 0L, 0L);
    }

    // ---------- Comentários ----------

    public static ComentarioRequest comentarioRequest(String texto) {
        return new ComentarioRequest(texto);
    }

    public static ComentarioResponse comentarioResponse(Long id, String texto, List<ComentarioResponse> respostas) {
        return new ComentarioResponse(id, texto, "Autor Teste", 0, respostas, DATA_FIXA);
    }
}
