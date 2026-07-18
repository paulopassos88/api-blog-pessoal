package br.com.passos.api_blog_pessoal.controller;

import br.com.passos.api_blog_pessoal.dto.CategoriaRequest;
import br.com.passos.api_blog_pessoal.dto.CategoriaResponse;
import br.com.passos.api_blog_pessoal.service.CategoriaService;
import br.com.passos.api_blog_pessoal.exception.ErrorResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import br.com.passos.api_blog_pessoal.assembler.CategoriaAssembler;
import io.swagger.v3.oas.annotations.Operation;
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
@RequestMapping("/api/v1/categorias")
@RequiredArgsConstructor
@Tag(name = "Categorias", description = "Endpoints para gerenciamento de categorias dos posts")
public class CategoriaController {

    private final CategoriaService service;
    private final CategoriaAssembler assembler;

    @PostMapping
    @Operation(summary = "Cria uma nova categoria", description = "Cria uma categoria com nome único. O slug será gerado automaticamente.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "201", description = "Categoria criada com sucesso",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = CategoriaResponse.class))),
        @ApiResponse(responseCode = "400", description = "Dados da requisição inválidos, nome nulo ou duplicado",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<EntityModel<CategoriaResponse>> criar(@RequestBody @Valid CategoriaRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(assembler.toModel(service.criar(request)));
    }

    @GetMapping
    @Operation(summary = "Lista todas as categorias", description = "Retorna todas as categorias cadastradas ordenadas alfabeticamente.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Categorias retornadas com sucesso")
    })
    public ResponseEntity<CollectionModel<EntityModel<CategoriaResponse>>> listar() {
        return ResponseEntity.ok(assembler.toCollectionModel(service.listarTodas()));
    }
}
