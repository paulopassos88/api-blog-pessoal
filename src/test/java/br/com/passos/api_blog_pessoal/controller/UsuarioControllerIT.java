package br.com.passos.api_blog_pessoal.controller;

import br.com.passos.api_blog_pessoal.AbstractIntegrationTest;
import br.com.passos.api_blog_pessoal.dto.UsuarioRequest;
import br.com.passos.api_blog_pessoal.model.Role;
import br.com.passos.api_blog_pessoal.repository.UsuarioRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class UsuarioControllerIT extends AbstractIntegrationTest {

    @Autowired
    private UsuarioRepository repository;

    @Test
    void deveCadastrarUsuarioComSucesso() {
        UsuarioRequest request = new UsuarioRequest("Admin", "admin@teste.com", "senha123", Role.ADMIN);
        
        ResponseEntity<Map> response = restTemplate.postForEntity("/api/v1/usuarios", request, Map.class);
        
        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        Map body = response.getBody();
        assertNotNull(body);
        assertEquals("Admin", body.get("nome"));
        assertNotNull(body.get("_links"));
    }
}
