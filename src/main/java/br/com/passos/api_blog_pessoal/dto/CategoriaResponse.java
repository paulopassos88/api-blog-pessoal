package br.com.passos.api_blog_pessoal.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Dados de retorno de uma categoria")
public record CategoriaResponse(
    @Schema(description = "Identificador único da categoria", example = "1")
    Long id,

    @Schema(description = "Nome da categoria", example = "Tecnologia")
    String nome,

    @Schema(description = "Slug gerado automaticamente a partir do nome", example = "tecnologia")
    String slug
) {
}
