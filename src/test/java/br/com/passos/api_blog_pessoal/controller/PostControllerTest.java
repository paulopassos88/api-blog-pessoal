package br.com.passos.api_blog_pessoal.controller;

import br.com.passos.api_blog_pessoal.assembler.PostAssembler;
import br.com.passos.api_blog_pessoal.assembler.PostFeedAssembler;
import br.com.passos.api_blog_pessoal.dto.PostRequest;
import br.com.passos.api_blog_pessoal.exception.BusinessException;
import br.com.passos.api_blog_pessoal.service.PostService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.SliceImpl;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.stream.Stream;

import static br.com.passos.api_blog_pessoal.support.TestDataFactory.postFeedResponse;
import static br.com.passos.api_blog_pessoal.support.TestDataFactory.postRequest;
import static br.com.passos.api_blog_pessoal.support.TestDataFactory.postRequestValido;
import static br.com.passos.api_blog_pessoal.support.TestDataFactory.postResponse;
import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.hasSize;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.willDoNothing;
import static org.mockito.BDDMockito.willThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(PostController.class)
@Import({PostAssembler.class, PostFeedAssembler.class})
@DisplayName("PostController (unitário)")
class PostControllerTest {

    private static final String URL = "/api/v1/posts";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private PostService service;

    private String json(Object body) throws Exception {
        return objectMapper.writeValueAsString(body);
    }

    @Nested
    @DisplayName("POST /api/v1/posts")
    class Criar {

        @Test
        @Tag("BDD-POSTS-01")
        @DisplayName("BDD-POSTS-01: retorna 201 com o post criado e links HATEOAS")
        void deveRetornar201ComHateoas_quandoPostValido() throws Exception {
            // Arrange
            var request = postRequestValido();
            given(service.criar(1L, request))
                    .willReturn(postResponse(10L, "Primeiro Post", "primeiro-post"));

            // Act + Assert
            mockMvc.perform(post(URL).param("autorId", "1")
                            .contentType(MediaType.APPLICATION_JSON).content(json(request)))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.id").value(10))
                    .andExpect(jsonPath("$.titulo").value("Primeiro Post"))
                    .andExpect(jsonPath("$.slug").value("primeiro-post"))
                    .andExpect(jsonPath("$.tempoLeitura").value(1))
                    .andExpect(jsonPath("$._links.self.href").value("http://localhost/api/v1/posts/10"))
                    .andExpect(jsonPath("$._links.posts.href").value("http://localhost/api/v1/posts"))
                    .andExpect(jsonPath("$._links.curtir.href").exists());

            verify(service).criar(1L, request);
        }

        @Test
        @Tag("BDD-POSTS-02")
        @DisplayName("BDD-POSTS-02: retorna 400 quando o autor informado é ADMIN ou inválido")
        void deveRetornar400_quandoAutorNaoEhUser() throws Exception {
            // Arrange
            var request = postRequestValido();
            given(service.criar(eq(2L), any(PostRequest.class)))
                    .willThrow(new BusinessException("Apenas usuários com perfil USER podem publicar posts"));

            // Act + Assert
            mockMvc.perform(post(URL).param("autorId", "2")
                            .contentType(MediaType.APPLICATION_JSON).content(json(request)))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.status").value(400))
                    .andExpect(jsonPath("$.detail").value("Apenas usuários com perfil USER podem publicar posts"));
        }

        static Stream<Arguments> requestsInvalidos() {
            return Stream.of(
                    Arguments.of("titulo", new PostRequest("", "Conteúdo válido para o post", null, List.of())),
                    Arguments.of("titulo", new PostRequest("abcd", "Conteúdo válido para o post", null, List.of())),
                    Arguments.of("conteudo", new PostRequest("Título Válido", "", null, List.of())),
                    Arguments.of("conteudo", new PostRequest("Título Válido", "   ", null, List.of()))
            );
        }

        @ParameterizedTest(name = "campo inválido: {0}")
        @MethodSource("requestsInvalidos")
        @DisplayName("[TEC] retorna 400 com erro no campo em 'details' quando payload é inválido")
        void deveRetornar400_quandoPayloadInvalido(String campo, PostRequest request) throws Exception {
            // Act + Assert
            mockMvc.perform(post(URL).param("autorId", "1")
                            .contentType(MediaType.APPLICATION_JSON).content(json(request)))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.details." + campo).exists());

            verifyNoInteractions(service);
        }

        @Test
        @DisplayName("[TEC] retorna 400 quando parâmetro 'autorId' está ausente")
        void deveRetornar400_quandoAutorIdAusente() throws Exception {
            // Act + Assert
            mockMvc.perform(post(URL)
                            .contentType(MediaType.APPLICATION_JSON).content(json(postRequestValido())))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.detail", containsString("autorId")));

            verifyNoInteractions(service);
        }
    }

    @Nested
    @DisplayName("GET /api/v1/posts/feed")
    class Feed {

        @Test
        @Tag("BDD-POSTS-10")
        @DisplayName("BDD-POSTS-10: retorna 200 com Slice de PostFeedResponse e links HATEOAS")
        void deveRetornar200ComSlice_quandoListarFeed() throws Exception {
            // Arrange
            var item = postFeedResponse(10L, "Post no Feed");
            var slice = new SliceImpl<>(List.of(item));
            given(service.listarFeed(50L, 10)).willReturn(slice);

            // Act + Assert
            mockMvc.perform(get(URL + "/feed").param("lastId", "50").param("size", "10"))
                    .andDo(org.springframework.test.web.servlet.result.MockMvcResultHandlers.print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.content", hasSize(1)))
                    .andExpect(jsonPath("$.content[0].id").value(10))
                    .andExpect(jsonPath("$.content[0].titulo").value("Post no Feed"));

            verify(service).listarFeed(50L, 10);
        }

        @Test
        @DisplayName("[TEC] utiliza default de size=10 e lastId null quando não informados")
        void deveUsarValoresDefault_quandoParametrosOmitidos() throws Exception {
            // Arrange
            given(service.listarFeed(null, 10)).willReturn(new SliceImpl<>(List.of()));

            // Act + Assert
            mockMvc.perform(get(URL + "/feed"))
                    .andExpect(status().isOk());

            verify(service).listarFeed(null, 10);
        }
    }

    @Nested
    @DisplayName("GET /api/v1/posts")
    class Listar {

        @Test
        @DisplayName("[TEC] retorna 200 com coleção de posts")
        void deveRetornar200ComColecao_quandoListarTodos() throws Exception {
            // Arrange
            given(service.listarTodos()).willReturn(List.of(
                    postResponse(1L, "Post 1", "post-1"),
                    postResponse(2L, "Post 2", "post-2")));

            // Act + Assert
            mockMvc.perform(get(URL))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$._embedded.postResponseList", hasSize(2)))
                    .andExpect(jsonPath("$._embedded.postResponseList[0].titulo").value("Post 1"))
                    .andExpect(jsonPath("$._embedded.postResponseList[1].titulo").value("Post 2"));
        }
    }

    @Nested
    @DisplayName("GET /api/v1/posts/{id}")
    class BuscarPorId {

        @Test
        @DisplayName("[TEC] retorna 200 com o post encontrado por ID")
        void deveRetornar200_quandoPostEncontrado() throws Exception {
            // Arrange
            given(service.buscarPorId(10L)).willReturn(postResponse(10L, "Post 10", "post-10"));

            // Act + Assert
            mockMvc.perform(get(URL + "/10"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.id").value(10))
                    .andExpect(jsonPath("$.titulo").value("Post 10"));
        }

        @Test
        @DisplayName("[TEC] retorna 400 quando post não existe")
        void deveRetornar400_quandoPostInexistente() throws Exception {
            // Arrange
            given(service.buscarPorId(999L)).willThrow(new BusinessException("Postagem não encontrada"));

            // Act + Assert
            mockMvc.perform(get(URL + "/999"))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.detail").value("Postagem não encontrada"));
        }

        @Test
        @DisplayName("[TEC] retorna 400 quando ID não é numérico")
        void deveRetornar400_quandoIdNaoNumerico() throws Exception {
            // Act + Assert
            mockMvc.perform(get(URL + "/abc"))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.detail", containsString("'id'")));

            verifyNoInteractions(service);
        }
    }

    @Nested
    @DisplayName("GET /api/v1/posts/slug/{slug}")
    class BuscarPorSlug {

        @Test
        @Tag("BDD-POSTS-11")
        @DisplayName("BDD-POSTS-11: retorna 200 com o post quando buscado por slug existente")
        void deveRetornar200_quandoSlugExiste() throws Exception {
            // Arrange
            given(service.buscarPorSlug("artigo-spring-boot"))
                    .willReturn(postResponse(10L, "Artigo Spring Boot", "artigo-spring-boot"));

            // Act + Assert
            mockMvc.perform(get(URL + "/slug/artigo-spring-boot"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.id").value(10))
                    .andExpect(jsonPath("$.slug").value("artigo-spring-boot"));
        }

        @Test
        @DisplayName("[TEC] retorna 400 quando slug não é encontrado")
        void deveRetornar400_quandoSlugInexistente() throws Exception {
            // Arrange
            given(service.buscarPorSlug("slug-inexistente"))
                    .willThrow(new BusinessException("Postagem não encontrada"));

            // Act + Assert
            mockMvc.perform(get(URL + "/slug/slug-inexistente"))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.detail").value("Postagem não encontrada"));
        }
    }

    @Nested
    @DisplayName("GET /api/v1/posts/search")
    class Pesquisar {

        @Test
        @DisplayName("[TEC] repassa os parâmetros de filtro ao service")
        void deveRepassarParametrosAoService_quandoPesquisar() throws Exception {
            // Arrange
            given(service.pesquisar("Spring", "Boot", "tecnologia", "Java"))
                    .willReturn(List.of(postResponse(1L, "Spring Boot", "spring-boot")));

            // Act + Assert
            mockMvc.perform(get(URL + "/search")
                            .param("titulo", "Spring")
                            .param("conteudo", "Boot")
                            .param("categoria", "tecnologia")
                            .param("tag", "Java"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$._embedded.postResponseList", hasSize(1)));

            verify(service).pesquisar("Spring", "Boot", "tecnologia", "Java");
        }
    }

    @Nested
    @DisplayName("PUT /api/v1/posts/{id}")
    class Atualizar {

        @Test
        @Tag("BDD-POSTS-04")
        @DisplayName("BDD-POSTS-04: retorna 200 ao atualizar post pelo autor original")
        void deveRetornar200_quandoAtualizadoPeloAutor() throws Exception {
            // Arrange
            var request = postRequest("Novo Título", 1L, List.of("Java"));
            given(service.atualizar(10L, 1L, request))
                    .willReturn(postResponse(10L, "Novo Título", "novo-titulo"));

            // Act + Assert
            mockMvc.perform(put(URL + "/10").param("autorId", "1")
                            .contentType(MediaType.APPLICATION_JSON).content(json(request)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.titulo").value("Novo Título"))
                    .andExpect(jsonPath("$.slug").value("novo-titulo"));

            verify(service).atualizar(10L, 1L, request);
        }

        @Test
        @Tag("BDD-POSTS-05")
        @DisplayName("BDD-POSTS-05: retorna 400 quando tentativa de atualização é feita por outro usuário")
        void deveRetornar400_quandoAtualizadoPorOutroUsuario() throws Exception {
            // Arrange
            var request = postRequestValido();
            given(service.atualizar(eq(10L), eq(2L), any(PostRequest.class)))
                    .willThrow(new BusinessException("Você não tem permissão para realizar esta operação"));

            // Act + Assert
            mockMvc.perform(put(URL + "/10").param("autorId", "2")
                            .contentType(MediaType.APPLICATION_JSON).content(json(request)))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.detail").value("Você não tem permissão para realizar esta operação"));
        }
    }

    @Nested
    @DisplayName("DELETE /api/v1/posts/{id}")
    class Excluir {

        @Test
        @Tag("BDD-POSTS-06")
        @DisplayName("BDD-POSTS-06: retorna 204 ao excluir post pelo autor original")
        void deveRetornar204_quandoExcluidoPeloAutor() throws Exception {
            // Arrange
            willDoNothing().given(service).excluir(10L, 1L);

            // Act + Assert
            mockMvc.perform(delete(URL + "/10").param("autorId", "1"))
                    .andExpect(status().isNoContent())
                    .andExpect(content().string(""));

            verify(service).excluir(10L, 1L);
        }

        @Test
        @Tag("BDD-POSTS-07")
        @DisplayName("BDD-POSTS-07: retorna 400 quando tentativa de exclusão é feita por outro usuário")
        void deveRetornar400_quandoExcluidoPorOutroUsuario() throws Exception {
            // Arrange
            willThrow(new BusinessException("Você não tem permissão para realizar esta operação"))
                    .given(service).excluir(10L, 2L);

            // Act + Assert
            mockMvc.perform(delete(URL + "/10").param("autorId", "2"))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.detail").value("Você não tem permissão para realizar esta operação"));
        }
    }

    @Nested
    @DisplayName("POST /api/v1/posts/{id}/curtir")
    class Curtir {

        @Test
        @Tag("BDD-POSTS-08")
        @DisplayName("BDD-POSTS-08: retorna 200 e delega a curtida ao service")
        void deveRetornar200_quandoCurtir() throws Exception {
            // Arrange
            willDoNothing().given(service).curtir(10L, 5L);

            // Act + Assert
            mockMvc.perform(post(URL + "/10/curtir").param("usuarioId", "5"))
                    .andExpect(status().isOk())
                    .andExpect(content().string(""));

            verify(service).curtir(10L, 5L);
        }

        @Test
        @DisplayName("[TEC] retorna 400 quando usuário ou post não são encontrados")
        void deveRetornar400_quandoUsuarioInexistente() throws Exception {
            // Arrange
            willThrow(new BusinessException("Usuário não encontrado"))
                    .given(service).curtir(10L, 999L);

            // Act + Assert
            mockMvc.perform(post(URL + "/10/curtir").param("usuarioId", "999"))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.detail").value("Usuário não encontrado"));
        }
    }
}
