package br.com.passos.api_blog_pessoal.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record PostRequest(
    @NotBlank(message = "O título é obrigatório")
    @Size(min = 5, max = 150, message = "O título deve ter entre 5 e 150 caracteres")
    String titulo,

    @NotBlank(message = "O conteúdo é obrigatório")
    String conteudo,

    Long categoriaId,

    java.util.List<String> tags
) {
}
