package br.com.passos.api_blog_pessoal.controller;

import br.com.passos.api_blog_pessoal.dto.CategoriaRequest;
import br.com.passos.api_blog_pessoal.dto.CategoriaResponse;
import br.com.passos.api_blog_pessoal.service.CategoriaService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import br.com.passos.api_blog_pessoal.assembler.CategoriaAssembler;
import org.springframework.hateoas.CollectionModel;
import org.springframework.hateoas.EntityModel;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/categorias")
@RequiredArgsConstructor
public class CategoriaController {

    private final CategoriaService service;
    private final CategoriaAssembler assembler;

    @PostMapping
    public ResponseEntity<EntityModel<CategoriaResponse>> criar(@RequestBody @Valid CategoriaRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(assembler.toModel(service.criar(request)));
    }

    @GetMapping
    public ResponseEntity<CollectionModel<EntityModel<CategoriaResponse>>> listar() {
        return ResponseEntity.ok(assembler.toCollectionModel(service.listarTodas()));
    }
}
