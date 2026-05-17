package br.com.passos.api_blog_pessoal.dto;

import java.time.LocalDateTime;

/**
 * DTO otimizado para a listagem do feed.
 * Contém apenas os dados necessários para exibir os cards na interface.
 */
public record PostFeedResponse(
    Long id,
    String titulo,
    String slug,
    String resumo,
    String nomeAutor,
    LocalDateTime dataCriacao,
    long quantidadeComentarios
) {
}
