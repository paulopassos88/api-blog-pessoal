package br.com.passos.api_blog_pessoal.service.validation;

import br.com.passos.api_blog_pessoal.dto.PostRequest;
import br.com.passos.api_blog_pessoal.model.Usuario;
import org.springframework.stereotype.Component;

@Component
public class ValidadorPostAutorNaoEhAdmin implements ValidadorCriacaoPost {

    @Override
    public void validar(Usuario autor, PostRequest request) {
        validarSeAutorNaoEhAdmin(autor, "Administradores não podem criar postagens");
    }
}
