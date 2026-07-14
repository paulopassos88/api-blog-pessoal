package br.com.passos.api_blog_pessoal.service;

import br.com.passos.api_blog_pessoal.dto.ComentarioRequest;
import br.com.passos.api_blog_pessoal.dto.ComentarioResponse;
import br.com.passos.api_blog_pessoal.exception.BusinessException;
import br.com.passos.api_blog_pessoal.mapper.ComentarioMapper;
import br.com.passos.api_blog_pessoal.model.Comentario;
import br.com.passos.api_blog_pessoal.model.Post;
import br.com.passos.api_blog_pessoal.model.Usuario;
import br.com.passos.api_blog_pessoal.repository.ComentarioRepository;
import br.com.passos.api_blog_pessoal.repository.PostRepository;
import br.com.passos.api_blog_pessoal.repository.UsuarioRepository;
import br.com.passos.api_blog_pessoal.service.validation.ValidadorCriacaoComentario;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ComentarioServiceTest {

    @Mock
    private ComentarioRepository repository;
    @Mock
    private PostRepository postRepository;
    @Mock
    private UsuarioRepository usuarioRepository;
    @Mock
    private ComentarioMapper mapper;
    @Mock
    private ValidadorCriacaoComentario validador;

    @InjectMocks
    private ComentarioService service;

    @BeforeEach
    void setup() {
        service = new ComentarioService(repository, postRepository, usuarioRepository, mapper, List.of(validador));
    }

    @Test
    void deveComentarComSucesso() {
        ComentarioRequest request = new ComentarioRequest("Ótimo post!");
        Post post = new Post();
        Usuario autor = new Usuario();
        Comentario comentario = new Comentario();
        ComentarioResponse response = new ComentarioResponse(1L, "Ótimo post!", "Autor", 0, new ArrayList<>(), null);

        when(postRepository.findById(1L)).thenReturn(Optional.of(post));
        when(usuarioRepository.findById(2L)).thenReturn(Optional.of(autor));
        doNothing().when(validador).validar(autor, request);
        when(mapper.toEntity(request)).thenReturn(comentario);
        when(repository.save(any(Comentario.class))).thenReturn(comentario);
        when(mapper.toResponse(comentario)).thenReturn(response);

        ComentarioResponse result = service.comentar(1L, 2L, request);

        assertNotNull(result);
        assertEquals("Ótimo post!", result.texto());
        verify(repository).save(any(Comentario.class));
    }

    @Test
    void deveResponderComSucesso() {
        ComentarioRequest request = new ComentarioRequest("Resposta!");
        Post post = new Post();
        Comentario pai = new Comentario();
        pai.setPost(post);
        Usuario autor = new Usuario();
        Comentario resposta = new Comentario();
        ComentarioResponse response = new ComentarioResponse(2L, "Resposta!", "Autor", 0, new ArrayList<>(), null);

        when(repository.findById(1L)).thenReturn(Optional.of(pai));
        when(usuarioRepository.findById(2L)).thenReturn(Optional.of(autor));
        doNothing().when(validador).validar(autor, request);
        when(mapper.toEntity(request)).thenReturn(resposta);
        when(repository.save(any(Comentario.class))).thenReturn(resposta);
        when(mapper.toResponse(resposta)).thenReturn(response);

        ComentarioResponse result = service.responder(1L, 2L, request);

        assertNotNull(result);
        verify(repository).save(any(Comentario.class));
    }

    @Test
    void deveCurtirComentario() {
        Comentario comentario = new Comentario();
        comentario.setCurtidas(new HashSet<>());
        Usuario usuario = new Usuario();

        when(repository.findById(1L)).thenReturn(Optional.of(comentario));
        when(usuarioRepository.findById(2L)).thenReturn(Optional.of(usuario));

        service.curtir(1L, 2L);

        assertTrue(comentario.getCurtidas().contains(usuario));
        verify(repository).save(comentario);
    }

    @Test
    void deveDescurtirComentarioSeJaCurtido() {
        Usuario usuario = new Usuario();
        Comentario comentario = new Comentario();
        comentario.setCurtidas(new HashSet<>(List.of(usuario)));

        when(repository.findById(1L)).thenReturn(Optional.of(comentario));
        when(usuarioRepository.findById(2L)).thenReturn(Optional.of(usuario));

        service.curtir(1L, 2L);

        assertFalse(comentario.getCurtidas().contains(usuario));
        verify(repository).save(comentario);
    }

    @Test
    void deveListarPorPost() {
        Comentario comentario = new Comentario();
        ComentarioResponse response = new ComentarioResponse(1L, "Ótimo post!", "Autor", 0, new ArrayList<>(), null);

        when(repository.findRootByPostId(1L)).thenReturn(List.of(comentario));
        when(mapper.toResponse(comentario)).thenReturn(response);

        List<ComentarioResponse> result = service.listarPorPost(1L);

        assertFalse(result.isEmpty());
        assertEquals(1, result.size());
    }

    @Test
    void deveLancarExcecaoAoComentarEmPostInexistente() {
        when(postRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(BusinessException.class, () -> service.comentar(1L, 2L, new ComentarioRequest("X")));
    }
}
