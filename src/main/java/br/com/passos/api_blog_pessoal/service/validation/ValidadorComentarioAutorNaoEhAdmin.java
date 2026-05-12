package br.com.passos.api_blog_pessoal.service.validation;

import br.com.passos.api_blog_pessoal.dto.ComentarioRequest;
import br.com.passos.api_blog_pessoal.model.Usuario;
import org.springframework.stereotype.Component;

@Component
public class ValidadorComentarioAutorNaoEhAdmin implements ValidadorCriacaoComentario {

    @Override
    public void validar(Usuario autor, ComentarioRequest request) {
        validarSeAutorNaoEhAdmin(autor, "Administradores não podem comentar");
    }
}
