package br.com.passos.api_blog_pessoal.controller;

import br.com.passos.api_blog_pessoal.assembler.ComentarioAssembler;
import br.com.passos.api_blog_pessoal.dto.ComentarioRequest;
import br.com.passos.api_blog_pessoal.exception.BusinessException;
import br.com.passos.api_blog_pessoal.service.ComentarioService;
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

import static br.com.passos.api_blog_pessoal.support.TestDataFactory.comentarioRequest;
import static br.com.passos.api_blog_pessoal.support.TestDataFactory.comentarioResponse;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.hasSize;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.willDoNothing;
import static org.mockito.BDDMockito.willThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ComentarioController.class)
@Import(ComentarioAssembler.class)
@DisplayName("ComentarioController (unitário)")
class ComentarioControllerTest {

    private static final String BASE_URL = "/api/v1";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private ComentarioService service;

    private String json(Object body) throws Exception {
        return objectMapper.writeValueAsString(body);
    }

    @Nested
    @DisplayName("POST /api/v1/posts/{postId}/comentarios")
    class Comentar {

        @Test
        @Tag("BDD-COMENTARIOS-01")
        @DisplayName("BDD-COMENTARIOS-01: retorna 201 com links HATEOAS ao adicionar comentário raiz")
        void deveRetornar201ComLinks_quandoComentar() throws Exception {
            // Arrange
            var request = comentarioRequest("Excelente artigo!");
            given(service.comentar(10L, 1L, request))
                    .willReturn(comentarioResponse(100L, "Excelente artigo!", List.of()));

            // Act + Assert
            mockMvc.perform(post(BASE_URL + "/posts/10/comentarios").param("autorId", "1")
                            .contentType(MediaType.APPLICATION_JSON).content(json(request)))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.id").value(100))
                    .andExpect(jsonPath("$.texto").value("Excelente artigo!"))
                    .andExpect(jsonPath("$._links.responder.href").exists())
                    .andExpect(jsonPath("$._links.curtir.href").exists());

            verify(service).comentar(10L, 1L, request);
        }

        @Test
        @Tag("BDD-COMENTARIOS-02")
        @DisplayName("BDD-COMENTARIOS-02: retorna 400 quando o autor do comentário é ADMIN")
        void deveRetornar400_quandoAutorAdmin() throws Exception {
            // Arrange
            var request = comentarioRequest("Comentário do Admin");
            given(service.comentar(eq(10L), eq(2L), any(ComentarioRequest.class)))
                    .willThrow(new BusinessException("Usuários administradores não podem publicar comentários"));

            // Act + Assert
            mockMvc.perform(post(BASE_URL + "/posts/10/comentarios").param("autorId", "2")
                            .contentType(MediaType.APPLICATION_JSON).content(json(request)))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.status").value(400))
                    .andExpect(jsonPath("$.detail").value("Usuários administradores não podem publicar comentários"));
        }

        @ParameterizedTest(name = "texto inválido: \"{0}\"")
        @NullAndEmptySource
        @ValueSource(strings = {"   "})
        @DisplayName("[TEC] retorna 400 quando o texto do comentário é em branco ou nulo")
        void deveRetornar400_quandoTextoInvalido(String texto) throws Exception {
            // Act + Assert
            mockMvc.perform(post(BASE_URL + "/posts/10/comentarios").param("autorId", "1")
                            .contentType(MediaType.APPLICATION_JSON).content(json(new ComentarioRequest(texto))))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.details.texto").exists());

            verifyNoInteractions(service);
        }

        @Test
        @DisplayName("[TEC] retorna 400 quando autorId está ausente")
        void deveRetornar400_quandoAutorIdAusente() throws Exception {
            // Act + Assert
            mockMvc.perform(post(BASE_URL + "/posts/10/comentarios")
                            .contentType(MediaType.APPLICATION_JSON).content(json(comentarioRequest("Artigo top"))))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.detail", containsString("autorId")));

            verifyNoInteractions(service);
        }
    }

    @Nested
    @DisplayName("GET /api/v1/posts/{postId}/comentarios")
    class ListarPorPost {

        @Test
        @Tag("BDD-COMENTARIOS-04")
        @DisplayName("BDD-COMENTARIOS-04: retorna 200 com a coleção hierárquica de comentários")
        void deveRetornar200ComHierarquia_quandoListar() throws Exception {
            // Arrange
            var resposta = comentarioResponse(101L, "Resposta", List.of());
            var raiz = comentarioResponse(100L, "Raiz", List.of(resposta));
            given(service.listarPorPost(10L)).willReturn(List.of(raiz));

            // Act + Assert
            mockMvc.perform(get(BASE_URL + "/posts/10/comentarios"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$._embedded.comentarioResponseList", hasSize(1)))
                    .andExpect(jsonPath("$._embedded.comentarioResponseList[0].id").value(100))
                    .andExpect(jsonPath("$._embedded.comentarioResponseList[0].respostas", hasSize(1)))
                    .andExpect(jsonPath("$._embedded.comentarioResponseList[0].respostas[0].id").value(101));

            verify(service).listarPorPost(10L);
        }

        @Test
        @DisplayName("[TEC] retorna 400 quando postId não é encontrado")
        void deveRetornar400_quandoPostNaoExiste() throws Exception {
            // Arrange
            given(service.listarPorPost(999L)).willThrow(new BusinessException("Postagem não encontrada"));

            // Act + Assert
            mockMvc.perform(get(BASE_URL + "/posts/999/comentarios"))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.detail").value("Postagem não encontrada"));
        }
    }

    @Nested
    @DisplayName("POST /api/v1/comentarios/{id}/respostas")
    class Responder {

        @Test
        @Tag("BDD-COMENTARIOS-03")
        @DisplayName("BDD-COMENTARIOS-03: retorna 201 ao responder a um comentário existente")
        void deveRetornar201_quandoResponderComSucesso() throws Exception {
            // Arrange
            var request = comentarioRequest("Obrigado pela resposta!");
            given(service.responder(100L, 1L, request))
                    .willReturn(comentarioResponse(102L, "Obrigado pela resposta!", List.of()));

            // Act + Assert
            mockMvc.perform(post(BASE_URL + "/comentarios/100/respostas").param("autorId", "1")
                            .contentType(MediaType.APPLICATION_JSON).content(json(request)))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.id").value(102))
                    .andExpect(jsonPath("$.texto").value("Obrigado pela resposta!"))
                    .andExpect(jsonPath("$._links.responder.href").exists());

            verify(service).responder(100L, 1L, request);
        }

        @Test
        @DisplayName("[TEC] retorna 400 quando o comentário pai não existe")
        void deveRetornar400_quandoComentarioPaiInexistente() throws Exception {
            // Arrange
            var request = comentarioRequest("Resposta");
            given(service.responder(eq(999L), eq(1L), any(ComentarioRequest.class)))
                    .willThrow(new BusinessException("Comentário pai não encontrado"));

            // Act + Assert
            mockMvc.perform(post(BASE_URL + "/comentarios/999/respostas").param("autorId", "1")
                            .contentType(MediaType.APPLICATION_JSON).content(json(request)))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.detail").value("Comentário pai não encontrado"));
        }
    }

    @Nested
    @DisplayName("POST /api/v1/comentarios/{id}/curtir")
    class Curtir {

        @Test
        @Tag("BDD-COMENTARIOS-05")
        @DisplayName("BDD-COMENTARIOS-05: retorna 200 ao curtir comentário")
        void deveRetornar200_quandoCurtirComentario() throws Exception {
            // Arrange
            willDoNothing().given(service).curtir(100L, 5L);

            // Act + Assert
            mockMvc.perform(post(BASE_URL + "/comentarios/100/curtir").param("usuarioId", "5"))
                    .andExpect(status().isOk())
                    .andExpect(content().string(""));

            verify(service).curtir(100L, 5L);
        }

        @Test
        @DisplayName("[TEC] retorna 400 quando comentário ou usuário não são encontrados")
        void deveRetornar400_quandoComentarioInexistente() throws Exception {
            // Arrange
            willThrow(new BusinessException("Comentário não encontrado"))
                    .given(service).curtir(999L, 5L);

            // Act + Assert
            mockMvc.perform(post(BASE_URL + "/comentarios/999/curtir").param("usuarioId", "5"))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.detail").value("Comentário não encontrado"));
        }
    }
}
