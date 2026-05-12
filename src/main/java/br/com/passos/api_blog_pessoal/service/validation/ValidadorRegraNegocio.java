package br.com.passos.api_blog_pessoal.service.validation;

import br.com.passos.api_blog_pessoal.exception.BusinessException;
import br.com.passos.api_blog_pessoal.model.Role;
import br.com.passos.api_blog_pessoal.model.Usuario;

public interface ValidadorRegraNegocio<T> {
    
    void validar(Usuario autor, T request);

    default void validarSeAutorNaoEhAdmin(Usuario autor, String mensagem) {
        if (autor.getRole() == Role.ADMIN) {
            throw new BusinessException(mensagem);
        }
    }
}
