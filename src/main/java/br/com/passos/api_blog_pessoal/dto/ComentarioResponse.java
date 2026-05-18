package br.com.passos.api_blog_pessoal.dto;

import java.time.LocalDateTime;

public record ComentarioResponse(
    Long id,
    String texto,
    String nomeAutor,
    int totalCurtidas,
    java.util.List<ComentarioResponse> respostas,
    java.time.LocalDateTime dataCriacao
) {
}
