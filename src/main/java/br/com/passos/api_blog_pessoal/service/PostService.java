package br.com.passos.api_blog_pessoal.service;

import br.com.passos.api_blog_pessoal.dto.PostFeedResponse;
import br.com.passos.api_blog_pessoal.dto.PostRequest;
import br.com.passos.api_blog_pessoal.dto.PostResponse;
import br.com.passos.api_blog_pessoal.exception.BusinessException;
import br.com.passos.api_blog_pessoal.mapper.PostMapper;
import br.com.passos.api_blog_pessoal.model.*;
import br.com.passos.api_blog_pessoal.repository.PostRepository;
import br.com.passos.api_blog_pessoal.repository.PostSpecifications;
import br.com.passos.api_blog_pessoal.repository.UsuarioRepository;
import br.com.passos.api_blog_pessoal.service.validation.ValidadorCriacaoPost;
import br.com.passos.api_blog_pessoal.util.SlugUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Slice;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PostService {

    private final PostRepository repository;
    private final UsuarioRepository usuarioRepository;
    private final CategoriaService categoriaService;
    private final TagService tagService;
    private final PostMapper mapper;
    private final List<ValidadorCriacaoPost> validadores;

    @Transactional
    public PostResponse criar(Long autorId, PostRequest request) {
        Usuario autor = usuarioRepository.findById(autorId)
                .orElseThrow(() -> new BusinessException("Usuário não encontrado"));

        validadores.forEach(v -> v.validar(autor, request));

        Post post = mapper.toEntity(request);
        post.setAutor(autor);
        post.setSlug(gerarSlugUnico(request.titulo()));
        
        if (request.categoriaId() != null) {
            post.setCategoria(categoriaService.buscarPorId(request.categoriaId()));
        }

        if (request.tags() != null && !request.tags().isEmpty()) {
            post.setTags(request.tags().stream()
                    .map(tagService::buscarOuCriar)
                    .collect(Collectors.toList()));
        }
        
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

    @Transactional(readOnly = true)
    public PostResponse buscarPorSlug(String slug) {
        Post post = repository.findBySlug(slug)
                .orElseThrow(() -> new BusinessException("Postagem não encontrada"));
        return mapper.toResponse(post);
    }

    @Transactional(readOnly = true)
    public List<PostResponse> pesquisar(String titulo, String conteudo, String categoria, String tag) {
        Specification<Post> spec = Specification.where(PostSpecifications.hasTitulo(titulo))
                .and(PostSpecifications.hasConteudo(conteudo))
                .and(PostSpecifications.hasCategoria(categoria))
                .and(PostSpecifications.hasTagJoin(tag));

        return repository.findAll(spec).stream()
                .map(mapper::toResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public Slice<PostFeedResponse> listarFeed(Long lastId, int size) {
        return repository.findFeed(lastId, PageRequest.of(0, size));
    }

    @Transactional
    public PostResponse atualizar(Long postId, Long autorId, PostRequest request) {
        Post post = buscarPostPorId(postId);
        validarPropriedadePost(post, autorId);

        post.setTitulo(request.titulo());
        post.setConteudo(request.conteudo());
        
        if (!post.getTitulo().equals(request.titulo())) {
            post.setSlug(gerarSlugUnico(request.titulo()));
        }

        if (request.categoriaId() != null) {
            post.setCategoria(categoriaService.buscarPorId(request.categoriaId()));
        }

        if (request.tags() != null) {
            post.setTags(request.tags().stream()
                    .map(tagService::buscarOuCriar)
                    .collect(Collectors.toList()));
        }

        return mapper.toResponse(repository.save(post));
    }

    @Transactional
    public void curtir(Long postId, Long usuarioId) {
        Post post = buscarPostPorId(postId);
        Usuario usuario = usuarioRepository.findById(usuarioId)
                .orElseThrow(() -> new BusinessException("Usuário não encontrado"));

        if (post.getCurtidas().contains(usuario)) {
            post.getCurtidas().remove(usuario);
        } else {
            post.getCurtidas().add(usuario);
        }
        repository.save(post);
    }

    @Transactional
    public void excluir(Long postId, Long autorId) {
        Post post = buscarPostPorId(postId);
        validarPropriedadePost(post, autorId);

        repository.delete(post);
    }

    private Post buscarPostPorId(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new BusinessException("Postagem não encontrada"));
    }

    private void validarPropriedadePost(Post post, Long autorId) {
        if (!post.getAutor().getId().equals(autorId)) {
            throw new BusinessException("Você não tem permissão para realizar esta operação");
        }

        if (post.getAutor().getRole() != Role.USER) {
            throw new BusinessException("Apenas usuários comuns podem gerenciar suas postagens");
        }
    }

    private String gerarSlugUnico(String titulo) {
        String slugBase = SlugUtils.makeSlug(titulo);
        String slug = slugBase;
        int count = 1;
        
        while (repository.findBySlug(slug).isPresent()) {
            slug = slugBase + "-" + count++;
        }
        
        return slug;
    }
}
