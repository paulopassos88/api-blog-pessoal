package br.com.passos.api_blog_pessoal.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@Schema(description = "Dados para criação de uma categoria")
public record CategoriaRequest(
    @Schema(description = "Nome da categoria", example = "Tecnologia")
    @NotBlank(message = "O nome da categoria é obrigatório")
    @Size(min = 3, max = 50, message = "O nome deve ter entre 3 e 50 caracteres")
    String nome
) {
}
