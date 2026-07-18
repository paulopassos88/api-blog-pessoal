package br.com.passos.api_blog_pessoal.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Dados de retorno de uma tag")
public record TagResponse(
    @Schema(description = "Identificador único da tag", example = "1")
    Long id,

    @Schema(description = "Nome da tag", example = "Java")
    String nome
) {
}
