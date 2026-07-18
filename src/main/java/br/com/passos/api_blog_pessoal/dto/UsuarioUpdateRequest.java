package br.com.passos.api_blog_pessoal.dto;

import br.com.passos.api_blog_pessoal.model.Role;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

@Schema(description = "Dados para atualização de um usuário")
public record UsuarioUpdateRequest(
    @Schema(description = "Novo nome do usuário", example = "Paulo Passos Novo")
    @NotBlank(message = "O nome é obrigatório")
    @Size(min = 3, max = 100, message = "O nome deve ter entre 3 e 100 caracteres")
    String nome,

    @Schema(description = "Novo endereço de e-mail do usuário", example = "paulopassos.novo@gmail.com")
    @NotBlank(message = "O email é obrigatório")
    @Email(message = "Email inválido")
    String email,

    @Schema(description = "Perfil/papel de permissão do usuário no sistema", example = "ADMIN")
    @NotNull(message = "O perfil (role) é obrigatório")
    Role role
) {
}
