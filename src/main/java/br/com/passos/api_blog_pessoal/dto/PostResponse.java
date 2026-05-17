package br.com.passos.api_blog_pessoal.dto;

import java.time.LocalDateTime;

public record PostResponse(
    Long id,
    String titulo,
    String slug,
    String conteudo,
    String nomeAutor,
    CategoriaResponse categoria,
    java.util.List<TagResponse> tags,
    java.time.LocalDateTime dataCriacao
) {
}
