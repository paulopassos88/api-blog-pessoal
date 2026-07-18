package br.com.passos.api_blog_pessoal.dto;

import br.com.passos.api_blog_pessoal.model.Role;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

@Schema(description = "Dados para cadastro de um novo usuário")
public record UsuarioRequest(
    @Schema(description = "Nome do usuário", example = "Paulo Passos")
    @NotBlank(message = "O nome é obrigatório")
    @Size(min = 3, max = 100, message = "O nome deve ter entre 3 e 100 caracteres")
    String nome,

    @Schema(description = "Endereço de e-mail do usuário (deve ser único)", example = "paulopassos88@gmail.com")
    @NotBlank(message = "O email é obrigatório")
    @Email(message = "Email inválido")
    String email,

    @Schema(description = "Senha de acesso do usuário (mínimo 8 caracteres)", example = "senha1234")
    @NotBlank(message = "A senha é obrigatória")
    @Size(min = 8, message = "A senha deve ter no mínimo 8 caracteres")
    String senha,

    @Schema(description = "Perfil/papel de permissão do usuário no sistema", example = "USER")
    @NotNull(message = "O perfil (role) é obrigatório")
    Role role
) {
}
