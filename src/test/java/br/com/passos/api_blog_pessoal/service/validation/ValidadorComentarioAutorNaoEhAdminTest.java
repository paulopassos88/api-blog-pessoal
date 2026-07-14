package br.com.passos.api_blog_pessoal.service.validation;

import br.com.passos.api_blog_pessoal.dto.ComentarioRequest;
import br.com.passos.api_blog_pessoal.exception.BusinessException;
import br.com.passos.api_blog_pessoal.model.Role;
import br.com.passos.api_blog_pessoal.model.Usuario;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class ValidadorComentarioAutorNaoEhAdminTest {

    private ValidadorComentarioAutorNaoEhAdmin validador;

    @BeforeEach
    void setUp() {
        validador = new ValidadorComentarioAutorNaoEhAdmin();
    }

    @Test
    void deveLancarExcecaoSeAutorForAdmin() {
        Usuario admin = new Usuario();
        admin.setRole(Role.ADMIN);
        ComentarioRequest request = new ComentarioRequest("Comentário admin");

        BusinessException exception = assertThrows(BusinessException.class, () -> validador.validar(admin, request));
        assertEquals("Administradores não podem comentar", exception.getMessage());
    }

    @Test
    void naoDeveLancarExcecaoSeAutorForUser() {
        Usuario user = new Usuario();
        user.setRole(Role.USER);
        ComentarioRequest request = new ComentarioRequest("Comentário user");

        assertDoesNotThrow(() -> validador.validar(user, request));
    }
}
