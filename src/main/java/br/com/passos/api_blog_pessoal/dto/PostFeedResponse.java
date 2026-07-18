package br.com.passos.api_blog_pessoal.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDateTime;

/**
 * DTO otimizado para a listagem do feed.
 * Contém apenas os dados necessários para exibir os cards na interface.
 */
@Schema(description = "Dados de retorno simplificados para exibição de um post no feed")
public record PostFeedResponse(
    @Schema(description = "Identificador único do post", example = "1")
    Long id,

    @Schema(description = "Título do post", example = "Começando com Spring Boot 3")
    String titulo,

    @Schema(description = "Slug amigável para URL", example = "comecando-com-spring-boot-3")
    String slug,

    @Schema(description = "Resumo curto do conteúdo do post", example = "Este post apresenta uma introdução prática ao Spring Boot 3...")
    String resumo,

    @Schema(description = "Nome do autor do post", example = "Paulo Passos")
    String nomeAutor,

    @Schema(description = "Data e hora de criação do post", example = "2026-07-17 10:00:00")
    LocalDateTime dataCriacao,

    @Schema(description = "Número total de comentários no post", example = "12")
    long quantidadeComentarios,

    @Schema(description = "Número total de curtidas recebidas pelo post", example = "45")
    long totalCurtidas
) {
}
