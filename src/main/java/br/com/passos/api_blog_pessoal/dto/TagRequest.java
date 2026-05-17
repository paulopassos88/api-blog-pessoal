package br.com.passos.api_blog_pessoal.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record TagRequest(
    @NotBlank(message = "O nome da tag é obrigatório")
    @Size(min = 2, max = 30, message = "O nome deve ter entre 2 e 30 caracteres")
    String nome
) {
}
