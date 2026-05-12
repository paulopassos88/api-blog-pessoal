package br.com.passos.api_blog_pessoal.service;

import br.com.passos.api_blog_pessoal.dto.PostRequest;
import br.com.passos.api_blog_pessoal.dto.PostResponse;
import br.com.passos.api_blog_pessoal.exception.BusinessException;
import br.com.passos.api_blog_pessoal.mapper.PostMapper;
import br.com.passos.api_blog_pessoal.model.Post;
import br.com.passos.api_blog_pessoal.model.Role;
import br.com.passos.api_blog_pessoal.model.Usuario;
import br.com.passos.api_blog_pessoal.repository.PostRepository;
import br.com.passos.api_blog_pessoal.repository.UsuarioRepository;
import br.com.passos.api_blog_pessoal.service.validation.ValidadorCriacaoPost;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PostService {

    private final PostRepository repository;
    private final UsuarioRepository usuarioRepository;
    private final PostMapper mapper;
    private final List<ValidadorCriacaoPost> validadores;

    @Transactional
    public PostResponse criar(Long autorId, PostRequest request) {
        Usuario autor = usuarioRepository.findById(autorId)
                .orElseThrow(() -> new BusinessException("Usuário não encontrado"));

        validadores.forEach(v -> v.validar(autor, request));

        Post post = mapper.toEntity(request);
        post.setAutor(autor);
        
        return mapper.toResponse(repository.save(post));
    }

    @Transactional(readOnly = true)
    public List<PostResponse> listarTodos() {
        return repository.findAll().stream()
                .map(mapper::toResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public PostResponse buscarPorId(Long id) {
        Post post = repository.findById(id)
                .orElseThrow(() -> new BusinessException("Postagem não encontrada"));
        return mapper.toResponse(post);
    }
}
