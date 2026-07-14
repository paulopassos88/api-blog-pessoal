package br.com.passos.api_blog_pessoal.controller;

import br.com.passos.api_blog_pessoal.AbstractIntegrationTest;
import br.com.passos.api_blog_pessoal.dto.CategoriaRequest;
import br.com.passos.api_blog_pessoal.dto.ComentarioRequest;
import br.com.passos.api_blog_pessoal.dto.PostRequest;
import br.com.passos.api_blog_pessoal.dto.UsuarioRequest;
import br.com.passos.api_blog_pessoal.model.Role;
import br.com.passos.api_blog_pessoal.repository.CategoriaRepository;
import br.com.passos.api_blog_pessoal.repository.ComentarioRepository;
import br.com.passos.api_blog_pessoal.repository.PostRepository;
import br.com.passos.api_blog_pessoal.repository.UsuarioRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.Collections;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class ComentarioControllerIT extends AbstractIntegrationTest {

    @Autowired
    private ComentarioRepository comentarioRepository;
    @Autowired
    private PostRepository postRepository;
    @Autowired
    private UsuarioRepository usuarioRepository;
    @Autowired
    private CategoriaRepository categoriaRepository;

    private Long autorId;
    private Long postId;

    @BeforeEach
    void setupData() {
        UsuarioRequest userReq = new UsuarioRequest("Autor", "autor@teste.com", "senha123", Role.USER);
        ResponseEntity<Map> userResp = restTemplate.postForEntity("/api/v1/usuarios", userReq, Map.class);
        autorId = ((Number) userResp.getBody().get("id")).longValue();

        CategoriaRequest catReq = new CategoriaRequest("Tecnologia");
        ResponseEntity<Map> catResp = restTemplate.postForEntity("/api/v1/categorias", catReq, Map.class);
        Long categoriaId = ((Number) catResp.getBody().get("id")).longValue();

        PostRequest postReq = new PostRequest("Post Teste", "Conteúdo", categoriaId, Collections.emptyList());
        ResponseEntity<Map> postResp = restTemplate.postForEntity("/api/v1/posts?autorId=" + autorId, postReq, Map.class);
        postId = ((Number) postResp.getBody().get("id")).longValue();
    }

    @Test
    void deveComentarComSucesso() {
        ComentarioRequest request = new ComentarioRequest("Ótimo artigo!");
        
        ResponseEntity<Map> response = restTemplate.postForEntity(
                "/api/v1/posts/" + postId + "/comentarios?autorId=" + autorId, 
                request, 
                Map.class);
                
        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertNotNull(response.getBody().get("_links"));
    }
}
