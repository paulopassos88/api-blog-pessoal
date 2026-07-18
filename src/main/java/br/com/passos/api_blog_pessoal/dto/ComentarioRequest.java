package br.com.passos.api_blog_pessoal.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

@Schema(description = "Dados para criação ou resposta de um comentário")
public record ComentarioRequest(
    @Schema(description = "Texto/conteúdo do comentário", example = "Excelente artigo, muito explicativo!")
    @NotBlank(message = "O texto do comentário é obrigatório")
    String texto
) {
}
