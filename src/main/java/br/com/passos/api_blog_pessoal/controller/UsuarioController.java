package br.com.passos.api_blog_pessoal.controller;

import br.com.passos.api_blog_pessoal.dto.UsuarioRequest;
import br.com.passos.api_blog_pessoal.dto.UsuarioResponse;
import br.com.passos.api_blog_pessoal.dto.UsuarioUpdateRequest;
import br.com.passos.api_blog_pessoal.service.UsuarioService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/usuarios")
@RequiredArgsConstructor
public class UsuarioController {

    private final UsuarioService service;

    @PostMapping
    public ResponseEntity<UsuarioResponse> cadastrar(@RequestBody @Valid UsuarioRequest request) {
        UsuarioResponse response = service.cadastrar(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/busca-por-email")
    public ResponseEntity<UsuarioResponse> buscarPorEmail(@RequestParam String email) {
        return ResponseEntity.ok(service.buscarPorEmail(email));
    }

    @GetMapping("/busca-por-nome")
    public ResponseEntity<List<UsuarioResponse>> buscarPorNome(@RequestParam String nome) {
        return ResponseEntity.ok(service.buscarPorNome(nome));
    }

    @PutMapping("/{id}")
    public ResponseEntity<UsuarioResponse> atualizar(
            @PathVariable Long id,
            @RequestParam Long executorId,
            @RequestBody @Valid UsuarioUpdateRequest request) {
        return ResponseEntity.ok(service.atualizar(id, executorId, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> excluir(
            @PathVariable Long id,
            @RequestParam Long executorId) {
        service.excluir(id, executorId);
        return ResponseEntity.noContent().build();
    }
}
