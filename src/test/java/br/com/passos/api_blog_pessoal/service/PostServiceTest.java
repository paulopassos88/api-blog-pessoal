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
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PostServiceTest {

    @Mock
    private PostRepository repository;

    @Mock
    private UsuarioRepository usuarioRepository;

    @Mock
    private CategoriaService categoriaService;

    @Mock
    private TagService tagService;

    @Mock
    private PostMapper mapper;

    @Mock
    private ValidadorCriacaoPost validador;

    private List<ValidadorCriacaoPost> validadores;

    @InjectMocks
    private PostService service;

    private Usuario autor;
    private PostRequest postRequest;
    private Post post;
    private PostResponse postResponse;

    @BeforeEach
    void setUp() {
        // Inicializa a lista de validadores manualmente para o Mockito InjectMocks
        validadores = Collections.singletonList(validador);
        service = new PostService(repository, usuarioRepository, categoriaService, tagService, mapper, validadores);

        autor = Usuario.builder()
                .id(1L)
                .nome("Autor Teste")
                .email("autor@teste.com")
                .role(Role.USER)
                .build();

        postRequest = new PostRequest("Título Teste", "Conteúdo Teste", null, null);

        post = Post.builder()
                .id(1L)
                .titulo("Título Teste")
                .slug("titulo-teste")
                .conteudo("Conteúdo Teste")
                .autor(autor)
                .build();

        postResponse = new PostResponse(1L, "Título Teste", "titulo-teste", "Conteúdo Teste", "Autor Teste", null, null, 0, 1, LocalDateTime.now());
    }

    @Test
    @DisplayName("Deve criar um post com sucesso")
    void deveCriarPostComSucesso() {
        // Arrange
        when(usuarioRepository.findById(1L)).thenReturn(Optional.of(autor));
        when(mapper.toEntity(postRequest)).thenReturn(post);
        when(repository.save(any(Post.class))).thenReturn(post);
        when(mapper.toResponse(post)).thenReturn(postResponse);

        // Act
        PostResponse response = service.criar(1L, postRequest);

        // Assert
        assertNotNull(response);
        assertEquals(postResponse.titulo(), response.titulo());
        verify(validador, times(1)).validar(autor, postRequest);
        verify(repository, times(1)).save(any(Post.class));
    }

    @Test
    @DisplayName("Deve lançar exceção ao tentar criar post com usuário inexistente")
    void deveLancarExcecaoUsuarioInexistente() {
        // Arrange
        when(usuarioRepository.findById(1L)).thenReturn(Optional.empty());

        // Act & Assert
        BusinessException exception = assertThrows(BusinessException.class, () -> {
            service.criar(1L, postRequest);
        });

        assertEquals("Usuário não encontrado", exception.getMessage());
        verify(repository, never()).save(any(Post.class));
    }

    @Test
    @DisplayName("Deve lançar exceção se um validador falhar")
    void deveLancarExcecaoSeValidadorFalhar() {
        // Arrange
        when(usuarioRepository.findById(1L)).thenReturn(Optional.of(autor));
        doThrow(new BusinessException("Erro de validação")).when(validador).validar(autor, postRequest);

        // Act & Assert
        assertThrows(BusinessException.class, () -> {
            service.criar(1L, postRequest);
        });

        verify(repository, never()).save(any(Post.class));
    }

    @Test
    @DisplayName("Deve listar todos os posts")
    void deveListarTodosOsPosts() {
        // Arrange
        when(repository.findAll()).thenReturn(List.of(post));
        when(mapper.toResponse(post)).thenReturn(postResponse);

        // Act
        List<PostResponse> response = service.listarTodos();

        // Assert
        assertFalse(response.isEmpty());
        assertEquals(1, response.size());
        verify(repository, times(1)).findAll();
    }

    @Test
    @DisplayName("Deve buscar post por ID com sucesso")
    void deveBuscarPostPorId() {
        // Arrange
        when(repository.findById(1L)).thenReturn(Optional.of(post));
        when(mapper.toResponse(post)).thenReturn(postResponse);

        // Act
        PostResponse response = service.buscarPorId(1L);

        // Assert
        assertNotNull(response);
        assertEquals(1L, response.id());
    }

    @Test
    @DisplayName("Deve lançar exceção ao buscar post inexistente")
    void deveLancarExcecaoPostInexistente() {
        // Arrange
        when(repository.findById(1L)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(BusinessException.class, () -> {
            service.buscarPorId(1L);
        });
    }

    @Test
    @DisplayName("Deve atualizar um post com sucesso")
    void deveAtualizarPostComSucesso() {
        // Arrange
        when(repository.findById(1L)).thenReturn(Optional.of(post));
        when(repository.save(any(Post.class))).thenReturn(post);
        when(mapper.toResponse(post)).thenReturn(postResponse);

        // Act
        PostResponse response = service.atualizar(1L, 1L, postRequest);

        // Assert
        assertNotNull(response);
        verify(repository, times(1)).save(any(Post.class));
    }

    @Test
    @DisplayName("Deve lançar exceção ao atualizar post de outro autor")
    void deveLancarExcecaoAoAtualizarPostDeOutroAutor() {
        // Arrange
        when(repository.findById(1L)).thenReturn(Optional.of(post));

        // Act & Assert
        BusinessException exception = assertThrows(BusinessException.class, () -> {
            service.atualizar(1L, 2L, postRequest);
        });

        assertEquals("Você não tem permissão para realizar esta operação", exception.getMessage());
        verify(repository, never()).save(any(Post.class));
    }

    @Test
    @DisplayName("Deve curtir um post com sucesso")
    void deveCurtirPostComSucesso() {
        // Arrange
        when(repository.findById(1L)).thenReturn(Optional.of(post));
        when(usuarioRepository.findById(1L)).thenReturn(Optional.of(autor));

        // Act
        service.curtir(1L, 1L);

        // Assert
        assertTrue(post.getCurtidas().contains(autor));
        verify(repository, times(1)).save(post);
    }

    @Test
    @DisplayName("Deve descurtir um post se já estiver curtido")
    void deveDescurtirPostSeJaCurtido() {
        // Arrange
        post.getCurtidas().add(autor);
        when(repository.findById(1L)).thenReturn(Optional.of(post));
        when(usuarioRepository.findById(1L)).thenReturn(Optional.of(autor));

        // Act
        service.curtir(1L, 1L);

        // Assert
        assertFalse(post.getCurtidas().contains(autor));
        verify(repository, times(1)).save(post);
    }

    @Test
    @DisplayName("Deve excluir um post com sucesso")
    void deveExcluirPostComSucesso() {
        // Arrange
        when(repository.findById(1L)).thenReturn(Optional.of(post));

        // Act
        service.excluir(1L, 1L);

        // Assert
        verify(repository, times(1)).delete(post);
    }

    @Test
    @DisplayName("Deve lançar exceção ao excluir post de outro autor")
    void deveLancarExcecaoAoExcluirPostDeOutroAutor() {
        // Arrange
        when(repository.findById(1L)).thenReturn(Optional.of(post));

        // Act & Assert
        BusinessException exception = assertThrows(BusinessException.class, () -> {
            service.excluir(1L, 2L);
        });

        assertEquals("Você não tem permissão para realizar esta operação", exception.getMessage());
        verify(repository, never()).delete(any(Post.class));
    }
}
