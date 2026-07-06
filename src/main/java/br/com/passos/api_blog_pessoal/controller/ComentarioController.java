package br.com.passos.api_blog_pessoal.controller;

import br.com.passos.api_blog_pessoal.dto.ComentarioRequest;
import br.com.passos.api_blog_pessoal.dto.ComentarioResponse;
import br.com.passos.api_blog_pessoal.service.ComentarioService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import br.com.passos.api_blog_pessoal.assembler.ComentarioAssembler;
import org.springframework.hateoas.CollectionModel;
import org.springframework.hateoas.EntityModel;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
public class ComentarioController {

    private final ComentarioService service;
    private final ComentarioAssembler assembler;

    @PostMapping("/posts/{postId}/comentarios")
    public ResponseEntity<EntityModel<ComentarioResponse>> comentar(
            @PathVariable Long postId,
            @RequestParam Long autorId,
            @RequestBody @Valid ComentarioRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(assembler.toModel(service.comentar(postId, autorId, request)));
    }

    @GetMapping("/posts/{postId}/comentarios")
    public ResponseEntity<CollectionModel<EntityModel<ComentarioResponse>>> listar(@PathVariable Long postId) {
        return ResponseEntity.ok(assembler.toCollectionModel(service.listarPorPost(postId)));
    }

    @PostMapping("/comentarios/{id}/respostas")
    public ResponseEntity<EntityModel<ComentarioResponse>> responder(
            @PathVariable Long id,
            @RequestParam Long autorId,
            @RequestBody @Valid ComentarioRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(assembler.toModel(service.responder(id, autorId, request)));
    }

    @PostMapping("/comentarios/{id}/curtir")
    public ResponseEntity<Void> curtir(@PathVariable Long id, @RequestParam Long usuarioId) {
        service.curtir(id, usuarioId);
        return ResponseEntity.ok().build();
    }
}
