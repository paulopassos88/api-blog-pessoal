package br.com.passos.api_blog_pessoal.controller;

import br.com.passos.api_blog_pessoal.assembler.TagAssembler;
import br.com.passos.api_blog_pessoal.service.TagService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static br.com.passos.api_blog_pessoal.support.TestDataFactory.tagResponse;
import static org.hamcrest.Matchers.hasSize;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(TagController.class)
@Import(TagAssembler.class)
@DisplayName("TagController (unitário)")
class TagControllerTest {

    private static final String URL = "/api/v1/tags";

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private TagService service;

    @Test
    @Tag("BDD-TAGS-06")
    @DisplayName("BDD-TAGS-06: GET /tags retorna 200 com a lista completa e links HATEOAS")
    void deveRetornar200ComTags_quandoExistiremTags() throws Exception {
        // Arrange
        given(service.listarTodas()).willReturn(List.of(tagResponse(1L, "Java"), tagResponse(2L, "Spring")));

        // Act + Assert
        mockMvc.perform(get(URL))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$._embedded.tagResponseList", hasSize(2)))
                .andExpect(jsonPath("$._embedded.tagResponseList[0].id").value(1))
                .andExpect(jsonPath("$._embedded.tagResponseList[0].nome").value("Java"))
                .andExpect(jsonPath("$._embedded.tagResponseList[0]._links.tags.href")
                        .value("http://localhost/api/v1/tags"));
    }

    @Test
    @DisplayName("[TEC] GET /tags retorna 200 sem _embedded quando não há tags")
    void deveRetornar200Vazio_quandoNaoExistiremTags() throws Exception {
        // Arrange
        given(service.listarTodas()).willReturn(List.of());

        // Act + Assert
        mockMvc.perform(get(URL))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$._embedded").doesNotExist());
    }
}
