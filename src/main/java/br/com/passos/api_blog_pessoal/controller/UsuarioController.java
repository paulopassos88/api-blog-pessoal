package br.com.passos.api_blog_pessoal.controller;

import br.com.passos.api_blog_pessoal.dto.UsuarioRequest;
import br.com.passos.api_blog_pessoal.dto.UsuarioResponse;
import br.com.passos.api_blog_pessoal.dto.UsuarioUpdateRequest;
import br.com.passos.api_blog_pessoal.service.UsuarioService;
import br.com.passos.api_blog_pessoal.exception.ErrorResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import br.com.passos.api_blog_pessoal.assembler.UsuarioAssembler;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.hateoas.CollectionModel;
import org.springframework.hateoas.EntityModel;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/usuarios")
@RequiredArgsConstructor
@Tag(name = "Usuários", description = "Endpoints para cadastro e gerenciamento de usuários da plataforma")
public class UsuarioController {

    private final UsuarioService service;
    private final UsuarioAssembler assembler;

    @PostMapping
    @Operation(summary = "Cadastra um novo usuário", description = "Cria um novo usuário com perfil ADMIN ou USER. O email deve ser único.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "201", description = "Usuário cadastrado com sucesso",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = UsuarioResponse.class))),
        @ApiResponse(responseCode = "400", description = "Dados da requisição inválidos ou falha de validação",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))),
        @ApiResponse(responseCode = "409", description = "Email já cadastrado no sistema",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<EntityModel<UsuarioResponse>> cadastrar(@RequestBody @Valid UsuarioRequest request) {
        UsuarioResponse response = service.cadastrar(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(assembler.toModel(response));
    }

    @GetMapping("/busca-por-email")
    @Operation(summary = "Busca usuário por email", description = "Retorna os dados de um usuário buscando por seu endereço de e-mail exato.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Usuário encontrado com sucesso",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = UsuarioResponse.class))),
        @ApiResponse(responseCode = "400", description = "Usuário não encontrado ou parâmetro de e-mail inválido",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<EntityModel<UsuarioResponse>> buscarPorEmail(
            @RequestParam @Parameter(description = "E-mail do usuário para busca", example = "paulopassos88@gmail.com") String email) {
        return ResponseEntity.ok(assembler.toModel(service.buscarPorEmail(email)));
    }

    @GetMapping("/busca-por-nome")
    @Operation(summary = "Busca usuários por nome", description = "Retorna uma lista de usuários cujos nomes contenham o termo pesquisado (busca case-insensitive).")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Lista de usuários retornada com sucesso"),
        @ApiResponse(responseCode = "400", description = "Parâmetro de nome inválido",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<CollectionModel<EntityModel<UsuarioResponse>>> buscarPorNome(
            @RequestParam @Parameter(description = "Parte do nome do usuário", example = "Paulo") String nome) {
        return ResponseEntity.ok(assembler.toCollectionModel(service.buscarPorNome(nome)));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Atualiza dados de um usuário", description = "Atualiza as informações de um usuário existente. Apenas administradores podem atualizar usuários.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Usuário atualizado com sucesso",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = UsuarioResponse.class))),
        @ApiResponse(responseCode = "400", description = "Dados inválidos, ID de usuário não encontrado ou executor não é administrador",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))),
        @ApiResponse(responseCode = "409", description = "Novo e-mail informado já está em uso por outro usuário",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<EntityModel<UsuarioResponse>> atualizar(
            @PathVariable @Parameter(description = "ID do usuário a ser atualizado", example = "1") Long id,
            @RequestParam @Parameter(description = "ID do usuário que está executando a ação (deve ser ADMIN)", example = "2") Long executorId,
            @RequestBody @Valid UsuarioUpdateRequest request) {
        return ResponseEntity.ok(assembler.toModel(service.atualizar(id, executorId, request)));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Exclui um usuário", description = "Exclui um usuário da base de dados. Apenas administradores podem excluir usuários.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "204", description = "Usuário excluído com sucesso"),
        @ApiResponse(responseCode = "400", description = "ID não encontrado ou executor não é administrador",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<Void> excluir(
            @PathVariable @Parameter(description = "ID do usuário a ser excluído", example = "1") Long id,
            @RequestParam @Parameter(description = "ID do usuário que está executando a ação (deve ser ADMIN)", example = "2") Long executorId) {
        service.excluir(id, executorId);
        return ResponseEntity.noContent().build();
    }
}
