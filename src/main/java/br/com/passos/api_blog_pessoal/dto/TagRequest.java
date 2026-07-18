package br.com.passos.api_blog_pessoal.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@Schema(description = "Dados para criação de uma tag")
public record TagRequest(
    @Schema(description = "Nome da tag", example = "Java")
    @NotBlank(message = "O nome da tag é obrigatório")
    @Size(min = 2, max = 30, message = "O nome deve ter entre 2 e 30 caracteres")
    String nome
) {
}
