package br.com.passos.api_blog_pessoal.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@Schema(description = "Dados para criação ou atualização de um post")
public record PostRequest(
    @Schema(description = "Título do post", example = "Começando com Spring Boot 3")
    @NotBlank(message = "O título é obrigatório")
    @Size(min = 5, max = 150, message = "O título deve ter entre 5 e 150 caracteres")
    String titulo,

    @Schema(description = "Conteúdo completo em markdown ou texto do post", example = "Neste post vamos falar sobre...")
    @NotBlank(message = "O conteúdo é obrigatório")
    String conteudo,

    @Schema(description = "Identificador único da categoria associada", example = "1")
    Long categoriaId,

    @Schema(description = "Lista com nomes das tags para o post", example = "[\"Java\", \"Spring Boot\"]")
    java.util.List<String> tags
) {
}
