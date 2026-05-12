package br.com.passos.api_blog_pessoal.controller;

import br.com.passos.api_blog_pessoal.dto.PostRequest;
import br.com.passos.api_blog_pessoal.dto.PostResponse;
import br.com.passos.api_blog_pessoal.service.PostService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/posts")
@RequiredArgsConstructor
public class PostController {

    private final PostService service;

    @PostMapping
    public ResponseEntity<PostResponse> criar(@RequestParam Long autorId, @RequestBody @Valid PostRequest request) {
        // Nota: O autorId virá do contexto de segurança no futuro
        return ResponseEntity.status(HttpStatus.CREATED).body(service.criar(autorId, request));
    }

    @GetMapping
    public ResponseEntity<List<PostResponse>> listar() {
        return ResponseEntity.ok(service.listarTodos());
    }

    @GetMapping("/{id}")
    public ResponseEntity<PostResponse> buscar(@PathVariable Long id) {
        return ResponseEntity.ok(service.buscarPorId(id));
    }
}
