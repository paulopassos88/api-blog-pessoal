package br.com.passos.api_blog_pessoal.controller;

import br.com.passos.api_blog_pessoal.dto.TagResponse;
import br.com.passos.api_blog_pessoal.service.TagService;
import lombok.RequiredArgsConstructor;
import br.com.passos.api_blog_pessoal.assembler.TagAssembler;
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
public class TagController {

    private final TagService service;
    private final TagAssembler assembler;

    @GetMapping
    public ResponseEntity<CollectionModel<EntityModel<TagResponse>>> listar() {
        return ResponseEntity.ok(assembler.toCollectionModel(service.listarTodas()));
    }
}
