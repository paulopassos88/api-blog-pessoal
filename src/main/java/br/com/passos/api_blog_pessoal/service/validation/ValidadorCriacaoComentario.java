package br.com.passos.api_blog_pessoal.service.validation;

import br.com.passos.api_blog_pessoal.dto.ComentarioRequest;
import br.com.passos.api_blog_pessoal.model.Usuario;

public interface ValidadorCriacaoComentario {
    void validar(Usuario autor, ComentarioRequest request);
}
