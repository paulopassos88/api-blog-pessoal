package br.com.passos.api_blog_pessoal.dto;

import br.com.passos.api_blog_pessoal.model.Role;
import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDateTime;

@Schema(description = "Dados de retorno de um usuário")
public record UsuarioResponse(
    @Schema(description = "Identificador único do usuário", example = "1")
    Long id,

    @Schema(description = "Nome do usuário", example = "Paulo Passos")
    String nome,

    @Schema(description = "Endereço de e-mail do usuário", example = "paulopassos88@gmail.com")
    String email,

    @Schema(description = "Perfil/papel de permissão do usuário", example = "USER")
    Role role,

    @Schema(description = "Data e hora de criação do usuário", example = "2026-07-17 10:00:00")
    LocalDateTime dataCriacao
) {
}
