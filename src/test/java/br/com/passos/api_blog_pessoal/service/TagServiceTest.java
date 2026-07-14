package br.com.passos.api_blog_pessoal.service;

import br.com.passos.api_blog_pessoal.dto.TagRequest;
import br.com.passos.api_blog_pessoal.dto.TagResponse;
import br.com.passos.api_blog_pessoal.mapper.TagMapper;
import br.com.passos.api_blog_pessoal.model.Tag;
import br.com.passos.api_blog_pessoal.repository.TagRepository;
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
class TagServiceTest {

    @Mock
    private TagRepository repository;

    @Mock
    private TagMapper mapper;

    @InjectMocks
    private TagService service;

    @Test
    void deveCriarTagInexistente() {
        TagRequest request = new TagRequest("Java");
        Tag tag = Tag.builder().nome("Java").build();
        TagResponse response = new TagResponse(1L, "Java");

        when(repository.findByNome("Java")).thenReturn(Optional.empty());
        when(mapper.toEntity(request)).thenReturn(tag);
        when(repository.save(any(Tag.class))).thenReturn(tag);
        when(mapper.toResponse(tag)).thenReturn(response);

        TagResponse result = service.criar(request);

        assertNotNull(result);
        assertEquals("Java", result.nome());
        verify(repository).save(any(Tag.class));
    }

    @Test
    void deveRetornarTagExistenteAoTentarCriar() {
        TagRequest request = new TagRequest("Java");
        Tag tag = Tag.builder().id(1L).nome("Java").build();
        TagResponse response = new TagResponse(1L, "Java");

        when(repository.findByNome("Java")).thenReturn(Optional.of(tag));
        when(mapper.toResponse(tag)).thenReturn(response);

        TagResponse result = service.criar(request);

        assertNotNull(result);
        assertEquals(1L, result.id());
        verify(repository, never()).save(any(Tag.class));
    }

    @Test
    void deveListarTodas() {
        Tag tag = Tag.builder().id(1L).nome("Java").build();
        TagResponse response = new TagResponse(1L, "Java");

        when(repository.findAll()).thenReturn(List.of(tag));
        when(mapper.toResponse(tag)).thenReturn(response);

        List<TagResponse> result = service.listarTodas();

        assertFalse(result.isEmpty());
        assertEquals(1, result.size());
    }

    @Test
    void deveBuscarOuCriar_RetornaExistente() {
        Tag tag = Tag.builder().id(1L).nome("Java").build();
        when(repository.findByNome("Java")).thenReturn(Optional.of(tag));

        Tag result = service.buscarOuCriar("Java");

        assertEquals(1L, result.getId());
        verify(repository, never()).save(any());
    }

    @Test
    void deveBuscarOuCriar_CriaNova() {
        when(repository.findByNome("Java")).thenReturn(Optional.empty());
        when(repository.save(any(Tag.class))).thenAnswer(i -> {
            Tag t = i.getArgument(0);
            t.setId(1L);
            return t;
        });

        Tag result = service.buscarOuCriar("Java");

        assertEquals(1L, result.getId());
        verify(repository).save(any(Tag.class));
    }
}
