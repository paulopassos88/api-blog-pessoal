package br.com.passos.api_blog_pessoal.controller;

import br.com.passos.api_blog_pessoal.AbstractIntegrationTest;
import br.com.passos.api_blog_pessoal.model.Tag;
import br.com.passos.api_blog_pessoal.repository.TagRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class TagControllerIT extends AbstractIntegrationTest {

    @Autowired
    private TagRepository repository;

    @Test
    void deveListarTagsComHateoas() {
        repository.save(Tag.builder().nome("Java").build());

        ResponseEntity<Map> response = restTemplate.getForEntity("/api/v1/tags", Map.class);
        
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody().get("_embedded"));
    }
}
