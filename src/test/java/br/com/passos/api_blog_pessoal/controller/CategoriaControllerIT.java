package br.com.passos.api_blog_pessoal.controller;

import br.com.passos.api_blog_pessoal.AbstractIntegrationTest;
import br.com.passos.api_blog_pessoal.dto.CategoriaRequest;
import br.com.passos.api_blog_pessoal.repository.CategoriaRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class CategoriaControllerIT extends AbstractIntegrationTest {

    @Autowired
    private CategoriaRepository repository;

    @Test
    void deveCriarCategoriaEListarComHateoas() {
        // Criar
        CategoriaRequest request = new CategoriaRequest("Programação");
        ResponseEntity<Map> responseCreate = restTemplate.postForEntity("/api/v1/categorias", request, Map.class);
        
        assertEquals(HttpStatus.CREATED, responseCreate.getStatusCode());
        assertNotNull(responseCreate.getBody());
        assertEquals("Programação", responseCreate.getBody().get("nome"));
        
        // Listar
        ResponseEntity<Map> responseList = restTemplate.getForEntity("/api/v1/categorias", Map.class);
        assertEquals(HttpStatus.OK, responseList.getStatusCode());
        
        // Verifica se veio no formato HAL (HATEOAS)
        Map bodyList = responseList.getBody();
        assertNotNull(bodyList.get("_embedded"));
    }
}
