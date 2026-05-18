package br.com.passos.api_blog_pessoal.service;

import br.com.passos.api_blog_pessoal.dto.ComentarioRequest;
import br.com.passos.api_blog_pessoal.dto.ComentarioResponse;
import br.com.passos.api_blog_pessoal.exception.BusinessException;
import br.com.passos.api_blog_pessoal.mapper.ComentarioMapper;
import br.com.passos.api_blog_pessoal.model.Comentario;
import br.com.passos.api_blog_pessoal.model.Post;
import br.com.passos.api_blog_pessoal.model.Role;
import br.com.passos.api_blog_pessoal.model.Usuario;
import br.com.passos.api_blog_pessoal.repository.ComentarioRepository;
import br.com.passos.api_blog_pessoal.repository.PostRepository;
import br.com.passos.api_blog_pessoal.repository.UsuarioRepository;
import br.com.passos.api_blog_pessoal.service.validation.ValidadorCriacaoComentario;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ComentarioService {

    private final ComentarioRepository repository;
    private final PostRepository postRepository;
    private final UsuarioRepository usuarioRepository;
    private final ComentarioMapper mapper;
    private final List<ValidadorCriacaoComentario> validadores;

    @Transactional
    public ComentarioResponse comentar(Long postId, Long autorId, ComentarioRequest request) {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new BusinessException("Postagem não encontrada"));

        Usuario autor = usuarioRepository.findById(autorId)
                .orElseThrow(() -> new BusinessException("Usuário não encontrado"));

        validadores.forEach(v -> v.validar(autor, request));

        Comentario comentario = mapper.toEntity(request);
        comentario.setPost(post);
        comentario.setAutor(autor);

        return mapper.toResponse(repository.save(comentario));
    }

    @Transactional
    public ComentarioResponse responder(Long paiId, Long autorId, ComentarioRequest request) {
        Comentario pai = repository.findById(paiId)
                .orElseThrow(() -> new BusinessException("Comentário pai não encontrado"));

        Usuario autor = usuarioRepository.findById(autorId)
                .orElseThrow(() -> new BusinessException("Usuário não encontrado"));

        validadores.forEach(v -> v.validar(autor, request));

        Comentario resposta = mapper.toEntity(request);
        resposta.setPost(pai.getPost());
        resposta.setAutor(autor);
        resposta.setPai(pai);

        return mapper.toResponse(repository.save(resposta));
    }

    @Transactional
    public void curtir(Long comentarioId, Long usuarioId) {
        Comentario comentario = repository.findById(comentarioId)
                .orElseThrow(() -> new BusinessException("Comentário não encontrado"));

        Usuario usuario = usuarioRepository.findById(usuarioId)
                .orElseThrow(() -> new BusinessException("Usuário não encontrado"));

        if (comentario.getCurtidas().contains(usuario)) {
            comentario.getCurtidas().remove(usuario);
        } else {
            comentario.getCurtidas().add(usuario);
        }
        repository.save(comentario);
    }

    @Transactional(readOnly = true)
    public List<ComentarioResponse> listarPorPost(Long postId) {
        // Retornar apenas comentários raiz para o feed de comentários
        return repository.findByPostId(postId).stream()
                .filter(c -> c.getPai() == null)
                .map(mapper::toResponse)
                .collect(Collectors.toList());
    }
}
