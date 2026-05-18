package br.com.passos.api_blog_pessoal.controller;

import br.com.passos.api_blog_pessoal.dto.PostFeedResponse;
import br.com.passos.api_blog_pessoal.dto.PostRequest;
import br.com.passos.api_blog_pessoal.dto.PostResponse;
import br.com.passos.api_blog_pessoal.service.PostService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Slice;
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

    @GetMapping("/feed")
    public ResponseEntity<Slice<PostFeedResponse>> listarFeed(
            @RequestParam(required = false) Long lastId,
            @RequestParam(defaultValue = "10") int size) {
        return ResponseEntity.ok(service.listarFeed(lastId, size));
    }

    @GetMapping
    public ResponseEntity<List<PostResponse>> listar() {
        return ResponseEntity.ok(service.listarTodos());
    }

    @GetMapping("/{id}")
    public ResponseEntity<PostResponse> buscar(@PathVariable Long id) {
        return ResponseEntity.ok(service.buscarPorId(id));
    }

    @GetMapping("/slug/{slug}")
    public ResponseEntity<PostResponse> buscarPorSlug(@PathVariable String slug) {
        return ResponseEntity.ok(service.buscarPorSlug(slug));
    }

    @GetMapping("/search")
    public ResponseEntity<List<PostResponse>> pesquisar(
            @RequestParam(required = false) String titulo,
            @RequestParam(required = false) String conteudo,
            @RequestParam(required = false) String categoria,
            @RequestParam(required = false) String tag) {
        return ResponseEntity.ok(service.pesquisar(titulo, conteudo, categoria, tag));
    }

    @PutMapping("/{id}")
    public ResponseEntity<PostResponse> atualizar(@PathVariable Long id, @RequestParam Long autorId, @RequestBody @Valid PostRequest request) {
        return ResponseEntity.ok(service.atualizar(id, autorId, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> excluir(@PathVariable Long id, @RequestParam Long autorId) {
        service.excluir(id, autorId);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{id}/curtir")
    public ResponseEntity<Void> curtir(@PathVariable Long id, @RequestParam Long usuarioId) {
        service.curtir(id, usuarioId);
        return ResponseEntity.ok().build();
    }
}
