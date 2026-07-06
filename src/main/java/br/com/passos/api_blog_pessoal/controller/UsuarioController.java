package br.com.passos.api_blog_pessoal.controller;

import br.com.passos.api_blog_pessoal.dto.UsuarioRequest;
import br.com.passos.api_blog_pessoal.dto.UsuarioResponse;
import br.com.passos.api_blog_pessoal.dto.UsuarioUpdateRequest;
import br.com.passos.api_blog_pessoal.service.UsuarioService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import br.com.passos.api_blog_pessoal.assembler.UsuarioAssembler;
import org.springframework.hateoas.CollectionModel;
import org.springframework.hateoas.EntityModel;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/usuarios")
@RequiredArgsConstructor
public class UsuarioController {

    private final UsuarioService service;
    private final UsuarioAssembler assembler;

    @PostMapping
    public ResponseEntity<EntityModel<UsuarioResponse>> cadastrar(@RequestBody @Valid UsuarioRequest request) {
        UsuarioResponse response = service.cadastrar(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(assembler.toModel(response));
    }

    @GetMapping("/busca-por-email")
    public ResponseEntity<EntityModel<UsuarioResponse>> buscarPorEmail(@RequestParam String email) {
        return ResponseEntity.ok(assembler.toModel(service.buscarPorEmail(email)));
    }

    @GetMapping("/busca-por-nome")
    public ResponseEntity<CollectionModel<EntityModel<UsuarioResponse>>> buscarPorNome(@RequestParam String nome) {
        return ResponseEntity.ok(assembler.toCollectionModel(service.buscarPorNome(nome)));
    }

    @PutMapping("/{id}")
    public ResponseEntity<EntityModel<UsuarioResponse>> atualizar(
            @PathVariable Long id,
            @RequestParam Long executorId,
            @RequestBody @Valid UsuarioUpdateRequest request) {
        return ResponseEntity.ok(assembler.toModel(service.atualizar(id, executorId, request)));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> excluir(
            @PathVariable Long id,
            @RequestParam Long executorId) {
        service.excluir(id, executorId);
        return ResponseEntity.noContent().build();
    }
}
