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
@RequestMapping("/api/v1/posts/{postId}/comentarios")
@RequiredArgsConstructor
public class ComentarioController {

    private final ComentarioService service;

    @PostMapping
    public ResponseEntity<ComentarioResponse> comentar(
            @PathVariable Long postId,
            @RequestParam Long autorId,
            @RequestBody @Valid ComentarioRequest request) {
        // Nota: O autorId virá do contexto de segurança no futuro
        return ResponseEntity.status(HttpStatus.CREATED).body(service.comentar(postId, autorId, request));
    }

    @GetMapping
    public ResponseEntity<List<ComentarioResponse>> listar(@PathVariable Long postId) {
        return ResponseEntity.ok(service.listarPorPost(postId));
    }
}
