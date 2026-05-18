package br.com.passos.api_blog_pessoal.controller;

import br.com.passos.api_blog_pessoal.dto.ComentarioRequest;
import br.com.passos.api_blog_pessoal.dto.ComentarioResponse;
import br.com.passos.api_blog_pessoal.service.ComentarioService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
public class ComentarioController {

    private final ComentarioService service;

    @PostMapping("/posts/{postId}/comentarios")
    public ResponseEntity<ComentarioResponse> comentar(
            @PathVariable Long postId,
            @RequestParam Long autorId,
            @RequestBody @Valid ComentarioRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.comentar(postId, autorId, request));
    }

    @GetMapping("/posts/{postId}/comentarios")
    public ResponseEntity<List<ComentarioResponse>> listar(@PathVariable Long postId) {
        return ResponseEntity.ok(service.listarPorPost(postId));
    }

    @PostMapping("/comentarios/{id}/respostas")
    public ResponseEntity<ComentarioResponse> responder(
            @PathVariable Long id,
            @RequestParam Long autorId,
            @RequestBody @Valid ComentarioRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.responder(id, autorId, request));
    }

    @PostMapping("/comentarios/{id}/curtir")
    public ResponseEntity<Void> curtir(@PathVariable Long id, @RequestParam Long usuarioId) {
        service.curtir(id, usuarioId);
        return ResponseEntity.ok().build();
    }
}
