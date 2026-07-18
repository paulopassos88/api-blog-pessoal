package br.com.passos.api_blog_pessoal.controller;

import br.com.passos.api_blog_pessoal.dto.TagResponse;
import br.com.passos.api_blog_pessoal.service.TagService;
import lombok.RequiredArgsConstructor;
import br.com.passos.api_blog_pessoal.assembler.TagAssembler;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.hateoas.CollectionModel;
import org.springframework.hateoas.EntityModel;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/tags")
@RequiredArgsConstructor
@Tag(name = "Tags", description = "Endpoints para gerenciamento de tags dos posts")
public class TagController {

    private final TagService service;
    private final TagAssembler assembler;

    @GetMapping
    @Operation(summary = "Lista todas as tags", description = "Retorna todas as tags cadastradas ordenadas alfabeticamente.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Tags retornadas com sucesso")
    })
    public ResponseEntity<CollectionModel<EntityModel<TagResponse>>> listar() {
        return ResponseEntity.ok(assembler.toCollectionModel(service.listarTodas()));
    }
}
