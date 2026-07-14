package br.com.passos.api_blog_pessoal.service;

import br.com.passos.api_blog_pessoal.dto.CategoriaRequest;
import br.com.passos.api_blog_pessoal.dto.CategoriaResponse;
import br.com.passos.api_blog_pessoal.exception.BusinessException;
import br.com.passos.api_blog_pessoal.mapper.CategoriaMapper;
import br.com.passos.api_blog_pessoal.model.Categoria;
import br.com.passos.api_blog_pessoal.repository.CategoriaRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CategoriaServiceTest {

    @Mock
    private CategoriaRepository repository;

    @Mock
    private CategoriaMapper mapper;

    @InjectMocks
    private CategoriaService service;

    @Test
    void deveCriarCategoriaComSucesso() {
        CategoriaRequest request = new CategoriaRequest("Tecnologia");
        Categoria categoria = new Categoria();
        categoria.setNome("Tecnologia");
        CategoriaResponse response = new CategoriaResponse(1L, "Tecnologia", "tecnologia");

        when(repository.existsByNome("Tecnologia")).thenReturn(false);
        when(mapper.toEntity(request)).thenReturn(categoria);
        when(repository.save(any(Categoria.class))).thenReturn(categoria);
        when(mapper.toResponse(categoria)).thenReturn(response);

        CategoriaResponse result = service.criar(request);

        assertNotNull(result);
        assertEquals("tecnologia", result.slug());
        verify(repository).save(any(Categoria.class));
    }

    @Test
    void deveLancarExcecaoCategoriaJaExistente() {
        CategoriaRequest request = new CategoriaRequest("Tecnologia");
        when(repository.existsByNome("Tecnologia")).thenReturn(true);

        BusinessException exception = assertThrows(BusinessException.class, () -> service.criar(request));
        assertEquals("Categoria já cadastrada", exception.getMessage());
        verify(repository, never()).save(any());
    }

    @Test
    void deveListarTodas() {
        Categoria categoria = new Categoria();
        CategoriaResponse response = new CategoriaResponse(1L, "Tecnologia", "tecnologia");

        when(repository.findAll()).thenReturn(List.of(categoria));
        when(mapper.toResponse(categoria)).thenReturn(response);

        List<CategoriaResponse> result = service.listarTodas();

        assertFalse(result.isEmpty());
        assertEquals(1, result.size());
    }

    @Test
    void deveBuscarPorIdComSucesso() {
        Categoria categoria = new Categoria();
        categoria.setId(1L);

        when(repository.findById(1L)).thenReturn(Optional.of(categoria));

        Categoria result = service.buscarPorId(1L);

        assertNotNull(result);
        assertEquals(1L, result.getId());
    }

    @Test
    void deveLancarExcecaoAoBuscarIdInexistente() {
        when(repository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(BusinessException.class, () -> service.buscarPorId(99L));
    }
}
