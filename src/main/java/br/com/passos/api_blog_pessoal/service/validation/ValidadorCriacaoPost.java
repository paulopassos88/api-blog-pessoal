package br.com.passos.api_blog_pessoal.service.validation;

import br.com.passos.api_blog_pessoal.dto.PostRequest;
import br.com.passos.api_blog_pessoal.model.Usuario;

public interface ValidadorCriacaoPost {
    void validar(Usuario autor, PostRequest request);
}
