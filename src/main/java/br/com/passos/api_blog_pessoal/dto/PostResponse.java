package br.com.passos.api_blog_pessoal.dto;

import java.time.LocalDateTime;

public record PostResponse(
    Long id,
    String titulo,
    String conteudo,
    String nomeAutor,
    LocalDateTime dataCriacao
) {
}
