package br.com.passos.api_blog_pessoal.controller;

import br.com.passos.api_blog_pessoal.assembler.UsuarioAssembler;
import br.com.passos.api_blog_pessoal.dto.UsuarioRequest;
import br.com.passos.api_blog_pessoal.dto.UsuarioUpdateRequest;
import br.com.passos.api_blog_pessoal.exception.BusinessException;
import br.com.passos.api_blog_pessoal.exception.EmailJaCadastradoException;
import br.com.passos.api_blog_pessoal.model.Role;
import br.com.passos.api_blog_pessoal.service.UsuarioService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.stream.Stream;

import static br.com.passos.api_blog_pessoal.support.TestDataFactory.*;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.hasSize;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.willThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(UsuarioController.class)
@Import(UsuarioAssembler.class)
@DisplayName("UsuarioController (unitário)")
class UsuarioControllerTest {

    private static final String URL = "/api/v1/usuarios";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private UsuarioService service;

    private String json(Object body) throws Exception {
        return objectMapper.writeValueAsString(body);
    }

    @Nested
    @DisplayName("POST /api/v1/usuarios")
    class Cadastrar {

        @Test
        @Tag("BDD-USUARIOS-01")
        @DisplayName("BDD-USUARIOS-01: retorna 201 com dados do usuário, sem senha, e links HATEOAS")
        void deveRetornar201SemSenha_quandoDadosValidos() throws Exception {
            // Arrange
            var request = usuarioRequestValido();
            given(service.cadastrar(request))
                    .willReturn(usuarioResponse(1L, "Autor Teste", "autor@teste.com", Role.USER));

            // Act + Assert
            mockMvc.perform(post(URL).contentType(MediaType.APPLICATION_JSON).content(json(request)))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.id").value(1))
                    .andExpect(jsonPath("$.email").value("autor@teste.com"))
                    .andExpect(jsonPath("$.role").value("USER"))
                    .andExpect(jsonPath("$.senha").doesNotExist())
                    .andExpect(jsonPath("$._links.self.href")
                            .value("http://localhost/api/v1/usuarios/busca-por-email?email=autor%40teste.com"))
                    .andExpect(jsonPath("$._links.busca-por-nome.href").exists());
        }

        @Test
        @Tag("BDD-USUARIOS-02")
        @DisplayName("BDD-USUARIOS-02: retorna 409 quando o e-mail já está cadastrado")
        void deveRetornar409_quandoEmailDuplicado() throws Exception {
            // Arrange
            given(service.cadastrar(any(UsuarioRequest.class)))
                    .willThrow(new EmailJaCadastradoException("Email duplicado"));

            // Act + Assert
            mockMvc.perform(post(URL).contentType(MediaType.APPLICATION_JSON).content(json(usuarioRequestValido())))
                    .andExpect(status().isConflict())
                    .andExpect(jsonPath("$.status").value(409))
                    .andExpect(jsonPath("$.detail").value("Email duplicado"));
        }

        static Stream<Arguments> requestsInvalidos() {
            return Stream.of(
                    Arguments.of("nome", new UsuarioRequest("", "a@teste.com", SENHA_VALIDA, Role.USER)),
                    Arguments.of("nome", new UsuarioRequest("ab", "a@teste.com", SENHA_VALIDA, Role.USER)),
                    Arguments.of("email", new UsuarioRequest("Fulano", "email-invalido", SENHA_VALIDA, Role.USER)),
                    Arguments.of("email", new UsuarioRequest("Fulano", "", SENHA_VALIDA, Role.USER)),
                    Arguments.of("senha", new UsuarioRequest("Fulano", "a@teste.com", "1234567", Role.USER)),
                    Arguments.of("role", new UsuarioRequest("Fulano", "a@teste.com", SENHA_VALIDA, null)));
        }

        @ParameterizedTest(name = "campo inválido: {0}")
        @MethodSource("requestsInvalidos")
        @DisplayName("[TEC] retorna 400 com o campo inválido em 'details' e não chama o service")
        void deveRetornar400_quandoCampoInvalido(String campo, UsuarioRequest request) throws Exception {
            // Act + Assert
            mockMvc.perform(post(URL).contentType(MediaType.APPLICATION_JSON).content(json(request)))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.details." + campo).exists());

            verifyNoInteractions(service);
        }

        @Test
        @DisplayName("[TEC] retorna 400 quando o corpo está ausente")
        void deveRetornar400_quandoCorpoAusente() throws Exception {
            // Act + Assert
            mockMvc.perform(post(URL).contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.title").value("Bad Request"));
        }
    }

    @Nested
    @DisplayName("GET /api/v1/usuarios/busca-por-email")
    class BuscarPorEmail {

        @Test
        @Tag("BDD-USUARIOS-08")
        @DisplayName("BDD-USUARIOS-08: retorna 200 com o usuário encontrado")
        void deveRetornar200_quandoEmailExiste() throws Exception {
            // Arrange
            given(service.buscarPorEmail("busca@teste.com"))
                    .willReturn(usuarioResponse(5L, "Busca", "busca@teste.com", Role.USER));

            // Act + Assert
            mockMvc.perform(get(URL + "/busca-por-email").param("email", "busca@teste.com"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.id").value(5))
                    .andExpect(jsonPath("$.email").value("busca@teste.com"));
        }

        @Test
        @Tag("BDD-USUARIOS-09")
        @DisplayName("BDD-USUARIOS-09: retorna 400 quando o e-mail não está cadastrado")
        void deveRetornar400_quandoEmailInexistente() throws Exception {
            // Arrange
            given(service.buscarPorEmail("inexistente@teste.com"))
                    .willThrow(new BusinessException("Usuario nao encontrado"));

            // Act + Assert
            mockMvc.perform(get(URL + "/busca-por-email").param("email", "inexistente@teste.com"))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.detail").value("Usuario nao encontrado"));
        }

        @Test
        @DisplayName("[TEC] retorna 400 quando o parâmetro 'email' está ausente")
        void deveRetornar400_quandoParametroAusente() throws Exception {
            // Act + Assert
            mockMvc.perform(get(URL + "/busca-por-email"))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.detail", containsString("email")));

            verifyNoInteractions(service);
        }
    }

    @Nested
    @DisplayName("GET /api/v1/usuarios/busca-por-nome")
    class BuscarPorNome {

        @Test
        @Tag("BDD-USUARIOS-10")
        @DisplayName("BDD-USUARIOS-10: retorna 200 com a coleção de usuários encontrados")
        void deveRetornar200ComColecao_quandoNomeEncontrado() throws Exception {
            // Arrange
            given(service.buscarPorNome("silva")).willReturn(List.of(
                    usuarioResponse(1L, "João Silva", "joao@teste.com", Role.USER),
                    usuarioResponse(2L, "Maria Silva", "maria@teste.com", Role.USER)));

            // Act + Assert
            mockMvc.perform(get(URL + "/busca-por-nome").param("nome", "silva"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$._embedded.usuarioResponseList", hasSize(2)))
                    .andExpect(jsonPath("$._embedded.usuarioResponseList[1].email").value("maria@teste.com"));
        }

        @Test
        @DisplayName("[TEC] retorna 200 sem _embedded quando nenhum usuário corresponde")
        void deveRetornar200Vazio_quandoNenhumUsuarioEncontrado() throws Exception {
            // Arrange
            given(service.buscarPorNome("xyz")).willReturn(List.of());

            // Act + Assert
            mockMvc.perform(get(URL + "/busca-por-nome").param("nome", "xyz"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$._embedded").doesNotExist());
        }
    }

    @Nested
    @DisplayName("PUT /api/v1/usuarios/{id}")
    class Atualizar {

        private final UsuarioUpdateRequest request =
                usuarioUpdateRequest("Nome Novo", "novo@teste.com", Role.USER);

        @Test
        @Tag("BDD-USUARIOS-03")
        @DisplayName("BDD-USUARIOS-03: retorna 200 e repassa id, executorId e corpo ao service")
        void deveRetornar200_quandoExecutorAdmin() throws Exception {
            // Arrange
            given(service.atualizar(2L, 1L, request))
                    .willReturn(usuarioResponse(2L, "Nome Novo", "novo@teste.com", Role.USER));

            // Act + Assert
            mockMvc.perform(put(URL + "/2").param("executorId", "1")
                            .contentType(MediaType.APPLICATION_JSON).content(json(request)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.nome").value("Nome Novo"))
                    .andExpect(jsonPath("$.email").value("novo@teste.com"));

            verify(service).atualizar(2L, 1L, request);
        }

        @Test
        @Tag("BDD-USUARIOS-04")
        @DisplayName("BDD-USUARIOS-04: retorna 400 quando o executor não é ADMIN")
        void deveRetornar400_quandoExecutorNaoAdmin() throws Exception {
            // Arrange
            given(service.atualizar(eq(2L), eq(3L), any()))
                    .willThrow(new BusinessException("Somente administradores"));

            // Act + Assert
            mockMvc.perform(put(URL + "/2").param("executorId", "3")
                            .contentType(MediaType.APPLICATION_JSON).content(json(request)))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.detail").value("Somente administradores"));
        }

        @Test
        @Tag("BDD-USUARIOS-05")
        @DisplayName("BDD-USUARIOS-05: retorna 409 quando o novo e-mail pertence a outro usuário")
        void deveRetornar409_quandoEmailPertenceAOutroUsuario() throws Exception {
            // Arrange
            given(service.atualizar(anyLong(), anyLong(), any()))
                    .willThrow(new EmailJaCadastradoException("Email em uso"));

            // Act + Assert
            mockMvc.perform(put(URL + "/2").param("executorId", "1")
                            .contentType(MediaType.APPLICATION_JSON).content(json(request)))
                    .andExpect(status().isConflict())
                    .andExpect(jsonPath("$.detail").value("Email em uso"));
        }

        @Test
        @DisplayName("[TEC] retorna 400 quando 'executorId' está ausente")
        void deveRetornar400_quandoExecutorIdAusente() throws Exception {
            // Act + Assert
            mockMvc.perform(put(URL + "/2").contentType(MediaType.APPLICATION_JSON).content(json(request)))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.detail", containsString("executorId")));

            verifyNoInteractions(service);
        }

        @Test
        @DisplayName("[TEC] retorna 400 quando o id do path não é numérico")
        void deveRetornar400_quandoIdNaoNumerico() throws Exception {
            // Act + Assert
            mockMvc.perform(put(URL + "/abc").param("executorId", "1")
                            .contentType(MediaType.APPLICATION_JSON).content(json(request)))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.detail", containsString("'id'")));

            verifyNoInteractions(service);
        }

        @Test
        @DisplayName("[TEC] retorna 400 quando o corpo de atualização é inválido")
        void deveRetornar400_quandoCorpoInvalido() throws Exception {
            // Arrange
            var invalido = usuarioUpdateRequest("", "email-invalido", null);

            // Act + Assert
            mockMvc.perform(put(URL + "/2").param("executorId", "1")
                            .contentType(MediaType.APPLICATION_JSON).content(json(invalido)))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.details.nome").exists())
                    .andExpect(jsonPath("$.details.email").exists())
                    .andExpect(jsonPath("$.details.role").exists());
        }
    }

    @Nested
    @DisplayName("DELETE /api/v1/usuarios/{id}")
    class Excluir {

        @Test
        @Tag("BDD-USUARIOS-06")
        @DisplayName("BDD-USUARIOS-06: retorna 204 sem corpo e delega a exclusão ao service")
        void deveRetornar204_quandoExecutorAdmin() throws Exception {
            // Act + Assert
            mockMvc.perform(delete(URL + "/2").param("executorId", "1"))
                    .andExpect(status().isNoContent())
                    .andExpect(content().string(""));

            verify(service).excluir(2L, 1L);
        }

        @Test
        @Tag("BDD-USUARIOS-07")
        @DisplayName("BDD-USUARIOS-07: retorna 400 quando o executor não é ADMIN")
        void deveRetornar400_quandoExecutorNaoAdmin() throws Exception {
            // Arrange
            willThrow(new BusinessException("Somente administradores")).given(service).excluir(2L, 3L);

            // Act + Assert
            mockMvc.perform(delete(URL + "/2").param("executorId", "3"))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.detail").value("Somente administradores"));
        }
    }
}
