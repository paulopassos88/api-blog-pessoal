package br.com.passos.api_blog_pessoal.service;

import br.com.passos.api_blog_pessoal.dto.UsuarioRequest;
import br.com.passos.api_blog_pessoal.dto.UsuarioResponse;
import br.com.passos.api_blog_pessoal.exception.EmailJaCadastradoException;
import br.com.passos.api_blog_pessoal.mapper.UsuarioMapper;
import br.com.passos.api_blog_pessoal.model.Usuario;
import br.com.passos.api_blog_pessoal.repository.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UsuarioService {

    private final UsuarioRepository repository;
    private final UsuarioMapper mapper;

    @Transactional
    public UsuarioResponse cadastrar(UsuarioRequest request) {
        validarEmailUnico(request.email());

        Usuario usuario = mapper.toEntity(request);
        Usuario usuarioSalvo = repository.save(usuario);
        
        return mapper.toResponse(usuarioSalvo);
    }

    private void validarEmailUnico(String email) {
        if (repository.findByEmail(email).isPresent()) {
            throw new EmailJaCadastradoException("Email já cadastrado");
        }
    }
}
