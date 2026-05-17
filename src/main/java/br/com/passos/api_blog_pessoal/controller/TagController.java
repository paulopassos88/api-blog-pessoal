package br.com.passos.api_blog_pessoal.controller;

import br.com.passos.api_blog_pessoal.dto.TagResponse;
import br.com.passos.api_blog_pessoal.service.TagService;
import lombok.RequiredArgsConstructor;
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

    @GetMapping
    public ResponseEntity<List<TagResponse>> listar() {
        return ResponseEntity.ok(service.listarTodas());
    }
}
