package br.com.passos.api_blog_pessoal.service;

import br.com.passos.api_blog_pessoal.dto.UsuarioRequest;
import br.com.passos.api_blog_pessoal.dto.UsuarioResponse;
import br.com.passos.api_blog_pessoal.dto.UsuarioUpdateRequest;
import br.com.passos.api_blog_pessoal.exception.BusinessException;
import br.com.passos.api_blog_pessoal.exception.EmailJaCadastradoException;
import br.com.passos.api_blog_pessoal.mapper.UsuarioMapper;
import br.com.passos.api_blog_pessoal.model.Role;
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

    @Transactional
    public UsuarioResponse atualizar(Long id, Long executorId, UsuarioUpdateRequest request) {
        validarSeExecutorEhAdmin(executorId, "Somente administradores podem atualizar usuários");
        
        Usuario usuario = buscarPorIdInternal(id);
        
        // Verifica se o novo email já pertence a outro usuário
        repository.findByEmail(request.email())
                .ifPresent(u -> {
                    if (!u.getId().equals(id)) {
                        throw new EmailJaCadastradoException("Email já cadastrado em outro usuário");
                    }
                });

        mapper.updateEntityFromDto(request, usuario);
        return mapper.toResponse(repository.save(usuario));
    }

    @Transactional
    public void excluir(Long id, Long executorId) {
        validarSeExecutorEhAdmin(executorId, "Somente administradores podem excluir usuários");
        Usuario usuario = buscarPorIdInternal(id);
        repository.delete(usuario);
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

    // Método comum para busca interna por ID
    private Usuario buscarPorIdInternal(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new BusinessException("Usuário não encontrado com o ID: " + id));
    }

    // Regra de negócio: Somente ADMIN pode realizar a ação
    private void validarSeExecutorEhAdmin(Long executorId, String mensagem) {
        Usuario executor = buscarPorIdInternal(executorId);
        if (executor.getRole() != Role.ADMIN) {
            throw new BusinessException(mensagem);
        }
    }

    private void validarEmailUnico(String email) {
        if (repository.existsByEmail(email)) {
            throw new EmailJaCadastradoException("Email já cadastrado");
        }
    }
}
