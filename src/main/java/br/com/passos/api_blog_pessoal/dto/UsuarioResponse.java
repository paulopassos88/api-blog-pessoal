package br.com.passos.api_blog_pessoal.dto;

import br.com.passos.api_blog_pessoal.model.Role;
import java.time.LocalDateTime;

public record UsuarioResponse(
    Long id,
    String nome,
    String email,
    Role role,
    LocalDateTime dataCriacao
) {
}
