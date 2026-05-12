package br.com.passos.api_blog_pessoal.dto;

import java.time.LocalDateTime;

public record ComentarioResponse(
    Long id,
    String texto,
    String nomeAutor,
    LocalDateTime dataCriacao
) {
}
