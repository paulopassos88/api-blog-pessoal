package br.com.passos.api_blog_pessoal.dto;

public record CategoriaResponse(
    Long id,
    String nome,
    String slug
) {
}
