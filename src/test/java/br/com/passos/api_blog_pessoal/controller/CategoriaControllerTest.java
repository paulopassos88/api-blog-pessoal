package br.com.passos.api_blog_pessoal.controller;

import br.com.passos.api_blog_pessoal.assembler.CategoriaAssembler;
import br.com.passos.api_blog_pessoal.dto.CategoriaRequest;
import br.com.passos.api_blog_pessoal.exception.BusinessException;
import br.com.passos.api_blog_pessoal.service.CategoriaService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static br.com.passos.api_blog_pessoal.support.TestDataFactory.categoriaRequest;
import static br.com.passos.api_blog_pessoal.support.TestDataFactory.categoriaResponse;
import static org.hamcrest.Matchers.hasSize;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(CategoriaController.class)
@Import(CategoriaAssembler.class)
@DisplayName("CategoriaController (unitário)")
class CategoriaControllerTest {

    private static final String URL = "/api/v1/categorias";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private CategoriaService service;

    @Nested
    @DisplayName("POST /api/v1/categorias")
    class Criar {

        @Test
        @Tag("BDD-CATEGORIAS-01")
        @DisplayName("BDD-CATEGORIAS-01: retorna 201 com slug e link HATEOAS")
        void deveRetornar201_quandoCategoriaValida() throws Exception {
            // Arrange
            var request = categoriaRequest("Tecnologia");
            given(service.criar(request)).willReturn(categoriaResponse(1L, "Tecnologia", "tecnologia"));

            // Act + Assert
            mockMvc.perform(post(URL).contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.id").value(1))
                    .andExpect(jsonPath("$.nome").value("Tecnologia"))
                    .andExpect(jsonPath("$.slug").value("tecnologia"))
                    .andExpect(jsonPath("$._links.categorias.href").value("http://localhost/api/v1/categorias"));
        }

        @Test
        @Tag("BDD-CATEGORIAS-02")
        @DisplayName("BDD-CATEGORIAS-02: retorna 400 quando o service sinaliza categoria duplicada")
        void deveRetornar400_quandoCategoriaDuplicada() throws Exception {
            // Arrange
            given(service.criar(any(CategoriaRequest.class)))
                    .willThrow(new BusinessException("Categoria duplicada"));

            // Act + Assert
            mockMvc.perform(post(URL).contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(categoriaRequest("Tecnologia"))))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.status").value(400))
                    .andExpect(jsonPath("$.detail").value("Categoria duplicada"))
                    .andExpect(jsonPath("$.path").value(URL));
        }

        @ParameterizedTest(name = "nome = \"{0}\"")
        @NullAndEmptySource
        @ValueSource(strings = {"   ", "ab", "123456789012345678901234567890123456789012345678901"})
        @DisplayName("[TEC] retorna 400 com detalhe do campo quando o nome é inválido (vazio, < 3 ou > 50)")
        void deveRetornar400_quandoNomeInvalido(String nome) throws Exception {
            // Act + Assert
            mockMvc.perform(post(URL).contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(categoriaRequest(nome))))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.details.nome").exists());

            verifyNoInteractions(service);
        }

        @Test
        @DisplayName("[TEC] retorna 400 quando o corpo da requisição é malformado")
        void deveRetornar400_quandoCorpoMalformado() throws Exception {
            // Act + Assert
            mockMvc.perform(post(URL).contentType(MediaType.APPLICATION_JSON).content("{nome:"))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.title").value("Bad Request"));

            verifyNoInteractions(service);
        }
    }

    @Nested
    @DisplayName("GET /api/v1/categorias")
    class Listar {

        @Test
        @Tag("BDD-CATEGORIAS-03")
        @DisplayName("BDD-CATEGORIAS-03: retorna 200 com coleção HATEOAS")
        void deveRetornar200ComColecao_quandoExistiremCategorias() throws Exception {
            // Arrange
            given(service.listarTodas()).willReturn(List.of(
                    categoriaResponse(1L, "Backend", "backend"),
                    categoriaResponse(2L, "Tecnologia", "tecnologia")));

            // Act + Assert
            mockMvc.perform(get(URL))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$._embedded.categoriaResponseList", hasSize(2)))
                    .andExpect(jsonPath("$._embedded.categoriaResponseList[0].nome").value("Backend"))
                    .andExpect(jsonPath("$._embedded.categoriaResponseList[0]._links.categorias.href").exists());
        }

        @Test
        @DisplayName("[TEC] retorna 200 sem _embedded quando não há categorias")
        void deveRetornar200Vazio_quandoNaoExistiremCategorias() throws Exception {
            // Arrange
            given(service.listarTodas()).willReturn(List.of());

            // Act + Assert
            mockMvc.perform(get(URL))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$._embedded").doesNotExist());
        }
    }
}
