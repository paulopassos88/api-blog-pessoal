package br.com.passos.api_blog_pessoal.service.validation;

import br.com.passos.api_blog_pessoal.dto.PostRequest;
import br.com.passos.api_blog_pessoal.exception.BusinessException;
import br.com.passos.api_blog_pessoal.model.Role;
import br.com.passos.api_blog_pessoal.model.Usuario;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Collections;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class ValidadorPostAutorNaoEhAdminTest {

    private ValidadorPostAutorNaoEhAdmin validador;

    @BeforeEach
    void setUp() {
        validador = new ValidadorPostAutorNaoEhAdmin();
    }

    @Test
    void deveLancarExcecaoSeAutorForAdmin() {
        Usuario admin = new Usuario();
        admin.setRole(Role.ADMIN);
        PostRequest request = new PostRequest("Título", "Conteúdo", 1L, Collections.emptyList());

        BusinessException exception = assertThrows(BusinessException.class, () -> validador.validar(admin, request));
        assertEquals("Administradores não podem criar postagens", exception.getMessage());
    }

    @Test
    void naoDeveLancarExcecaoSeAutorForUser() {
        Usuario user = new Usuario();
        user.setRole(Role.USER);
        PostRequest request = new PostRequest("Título", "Conteúdo", 1L, Collections.emptyList());

        assertDoesNotThrow(() -> validador.validar(user, request));
    }
}
