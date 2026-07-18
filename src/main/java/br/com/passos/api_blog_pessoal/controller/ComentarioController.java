package br.com.passos.api_blog_pessoal.controller;

import br.com.passos.api_blog_pessoal.dto.ComentarioRequest;
import br.com.passos.api_blog_pessoal.dto.ComentarioResponse;
import br.com.passos.api_blog_pessoal.service.ComentarioService;
import br.com.passos.api_blog_pessoal.exception.ErrorResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import br.com.passos.api_blog_pessoal.assembler.ComentarioAssembler;
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
@RequestMapping("/api/v1")
@RequiredArgsConstructor
@Tag(name = "Comentários", description = "Endpoints para criação, resposta e gerenciamento de comentários nos posts")
public class ComentarioController {

    private final ComentarioService service;
    private final ComentarioAssembler assembler;

    @PostMapping("/posts/{postId}/comentarios")
    @Operation(summary = "Adiciona um comentário a um post", description = "Cria um novo comentário associado ao post informado. O autor não pode ser do tipo ADMIN.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "201", description = "Comentário adicionado com sucesso",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = ComentarioResponse.class))),
        @ApiResponse(responseCode = "400", description = "Dados da requisição inválidos, post não encontrado, autor não encontrado ou autor possui perfil ADMIN",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<EntityModel<ComentarioResponse>> comentar(
            @PathVariable @Parameter(description = "ID do post a ser comentado", example = "1") Long postId,
            @RequestParam @Parameter(description = "ID do usuário autor do comentário", example = "2") Long autorId,
            @RequestBody @Valid ComentarioRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(assembler.toModel(service.comentar(postId, autorId, request)));
    }

    @GetMapping("/posts/{postId}/comentarios")
    @Operation(summary = "Lista comentários de um post", description = "Retorna todos os comentários e suas respectivas respostas vinculados a um post.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Lista de comentários retornada com sucesso"),
        @ApiResponse(responseCode = "400", description = "ID do post não encontrado",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<CollectionModel<EntityModel<ComentarioResponse>>> listar(
            @PathVariable @Parameter(description = "ID do post", example = "1") Long postId) {
        return ResponseEntity.ok(assembler.toCollectionModel(service.listarPorPost(postId)));
    }

    @PostMapping("/comentarios/{id}/respostas")
    @Operation(summary = "Responde a um comentário existente", description = "Adiciona uma resposta vinculada a um comentário pai específico.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "201", description = "Resposta adicionada com sucesso",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = ComentarioResponse.class))),
        @ApiResponse(responseCode = "400", description = "Dados inválidos, comentário pai não encontrado, autor não encontrado ou autor é ADMIN",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<EntityModel<ComentarioResponse>> responder(
            @PathVariable @Parameter(description = "ID do comentário que receberá a resposta", example = "1") Long id,
            @RequestParam @Parameter(description = "ID do autor da resposta", example = "2") Long autorId,
            @RequestBody @Valid ComentarioRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(assembler.toModel(service.responder(id, autorId, request)));
    }

    @PostMapping("/comentarios/{id}/curtir")
    @Operation(summary = "Curte um comentário", description = "Registra a curtida de um usuário em um comentário. Um usuário só pode curtir cada comentário uma vez.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Comentário curtido com sucesso"),
        @ApiResponse(responseCode = "400", description = "Comentário ou usuário não encontrado, ou curtida duplicada/inválida",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<Void> curtir(
            @PathVariable @Parameter(description = "ID do comentário a ser curtido", example = "1") Long id,
            @RequestParam @Parameter(description = "ID do usuário que está curtindo", example = "2") Long usuarioId) {
        service.curtir(id, usuarioId);
        return ResponseEntity.ok().build();
    }
}
