package br.com.passos.api_blog_pessoal.dto;

import jakarta.validation.constraints.NotBlank;

public record ComentarioRequest(
    @NotBlank(message = "O texto do comentário é obrigatório")
    String texto
) {
}
