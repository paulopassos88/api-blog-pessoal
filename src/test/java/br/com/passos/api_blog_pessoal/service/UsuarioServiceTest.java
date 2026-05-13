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
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UsuarioServiceTest {

    @Mock
    private UsuarioRepository repository;

    @Mock
    private UsuarioMapper mapper;

    @InjectMocks
    private UsuarioService service;

    private Usuario usuario;
    private Usuario admin;
    private UsuarioRequest usuarioRequest;
    private UsuarioResponse usuarioResponse;
    private UsuarioUpdateRequest updateRequest;

    @BeforeEach
    void setUp() {
        usuario = Usuario.builder()
                .id(1L)
                .nome("Usuario Teste")
                .email("usuario@teste.com")
                .role(Role.USER)
                .build();

        admin = Usuario.builder()
                .id(2L)
                .nome("Admin Teste")
                .email("admin@teste.com")
                .role(Role.ADMIN)
                .build();

        usuarioRequest = new UsuarioRequest("Usuario Teste", "usuario@teste.com", "senha123", Role.USER);
        usuarioResponse = new UsuarioResponse(1L, "Usuario Teste", "usuario@teste.com", Role.USER, LocalDateTime.now());
        updateRequest = new UsuarioUpdateRequest("Nome Atualizado", "novo@email.com", Role.USER);
    }

    @Test
    @DisplayName("Deve cadastrar usuário com sucesso")
    void deveCadastrarUsuarioComSucesso() {
        when(repository.findByEmail(anyString())).thenReturn(Optional.empty());
        when(mapper.toEntity(usuarioRequest)).thenReturn(usuario);
        when(repository.save(any(Usuario.class))).thenReturn(usuario);
        when(mapper.toResponse(usuario)).thenReturn(usuarioResponse);

        UsuarioResponse response = service.cadastrar(usuarioRequest);

        assertNotNull(response);
        assertEquals(usuarioResponse.email(), response.email());
        verify(repository, times(1)).save(any(Usuario.class));
    }

    @Test
    @DisplayName("Deve lançar exceção ao cadastrar email duplicado")
    void deveLancarExcecaoEmailDuplicadoNoCadastro() {
        when(repository.findByEmail(anyString())).thenReturn(Optional.of(usuario));

        assertThrows(EmailJaCadastradoException.class, () -> service.cadastrar(usuarioRequest));
        verify(repository, never()).save(any(Usuario.class));
    }

    @Test
    @DisplayName("Deve atualizar usuário quando o executor for ADMIN")
    void deveAtualizarUsuarioQuandoExecutorForAdmin() {
        when(repository.findById(2L)).thenReturn(Optional.of(admin)); // Executor
        when(repository.findById(1L)).thenReturn(Optional.of(usuario)); // Alvo
        when(repository.findByEmail(updateRequest.email())).thenReturn(Optional.empty());
        when(repository.save(any(Usuario.class))).thenReturn(usuario);
        when(mapper.toResponse(usuario)).thenReturn(usuarioResponse);

        UsuarioResponse response = service.atualizar(1L, 2L, updateRequest);

        assertNotNull(response);
        verify(mapper).updateEntityFromDto(updateRequest, usuario);
        verify(repository).save(usuario);
    }

    @Test
    @DisplayName("Deve lançar exceção ao atualizar se executor não for ADMIN")
    void deveLancarExcecaoAoAtualizarSeExecutorNaoForAdmin() {
        when(repository.findById(1L)).thenReturn(Optional.of(usuario)); // Executor USER

        assertThrows(BusinessException.class, () -> service.atualizar(3L, 1L, updateRequest));
        verify(repository, never()).save(any(Usuario.class));
    }

    @Test
    @DisplayName("Deve excluir usuário quando o executor for ADMIN")
    void deveExcluirUsuarioQuandoExecutorForAdmin() {
        when(repository.findById(2L)).thenReturn(Optional.of(admin)); // Executor
        when(repository.findById(1L)).thenReturn(Optional.of(usuario)); // Alvo

        service.excluir(1L, 2L);

        verify(repository).delete(usuario);
    }

    @Test
    @DisplayName("Deve buscar usuário por email com sucesso")
    void deveBuscarPorEmail() {
        when(repository.findByEmail("usuario@teste.com")).thenReturn(Optional.of(usuario));
        when(mapper.toResponse(usuario)).thenReturn(usuarioResponse);

        UsuarioResponse response = service.buscarPorEmail("usuario@teste.com");

        assertNotNull(response);
        assertEquals("usuario@teste.com", response.email());
    }

    @Test
    @DisplayName("Deve buscar usuários por nome")
    void deveBuscarPorNome() {
        when(repository.findByNomeContainingIgnoreCaseOrderByDataCriacaoDesc("Teste"))
                .thenReturn(List.of(usuario));
        when(mapper.toResponse(usuario)).thenReturn(usuarioResponse);

        List<UsuarioResponse> response = service.buscarPorNome("Teste");

        assertFalse(response.isEmpty());
        assertEquals(1, response.size());
    }
}
