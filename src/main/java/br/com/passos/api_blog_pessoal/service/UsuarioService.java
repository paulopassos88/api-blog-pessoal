package br.com.passos.api_blog_pessoal.service;

import br.com.passos.api_blog_pessoal.dto.UsuarioRequest;
import br.com.passos.api_blog_pessoal.dto.UsuarioResponse;
import br.com.passos.api_blog_pessoal.exception.BusinessException;
import br.com.passos.api_blog_pessoal.exception.EmailJaCadastradoException;
import br.com.passos.api_blog_pessoal.mapper.UsuarioMapper;
import br.com.passos.api_blog_pessoal.model.Usuario;
import br.com.passos.api_blog_pessoal.repository.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

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

    @Transactional(readOnly = true)
    public UsuarioResponse buscarPorEmail(String email) {
        return repository.findByEmail(email)
                .map(mapper::toResponse)
                .orElseThrow(() -> new BusinessException("Usuário não encontrado com o email informado"));
    }

    @Transactional(readOnly = true)
    public List<UsuarioResponse> buscarPorNome(String nome) {
        return repository.findByNomeContainingIgnoreCaseOrderByDataCriacaoDesc(nome)
                .stream()
                .map(mapper::toResponse)
                .collect(Collectors.toList());
    }

    private void validarEmailUnico(String email) {
        if (repository.findByEmail(email).isPresent()) {
            throw new EmailJaCadastradoException("Email já cadastrado");
        }
    }
}
