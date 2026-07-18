package br.com.passos.api_blog_pessoal.controller;

import br.com.passos.api_blog_pessoal.dto.PostFeedResponse;
import br.com.passos.api_blog_pessoal.dto.PostRequest;
import br.com.passos.api_blog_pessoal.dto.PostResponse;
import br.com.passos.api_blog_pessoal.service.PostService;
import br.com.passos.api_blog_pessoal.exception.ErrorResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import br.com.passos.api_blog_pessoal.assembler.PostAssembler;
import br.com.passos.api_blog_pessoal.assembler.PostFeedAssembler;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.hateoas.CollectionModel;
import org.springframework.hateoas.EntityModel;
import org.springframework.data.domain.Slice;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/posts")
@RequiredArgsConstructor
@Tag(name = "Posts", description = "Endpoints para gerenciamento de publicações (posts) e feed")
public class PostController {

    private final PostService service;
    private final PostAssembler assembler;
    private final PostFeedAssembler feedAssembler;

    @PostMapping
    @Operation(summary = "Cria um novo post", description = "Publica um novo post associado a um autor com papel USER. O tempo de leitura é calculado automaticamente.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "201", description = "Post criado com sucesso",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = PostResponse.class))),
        @ApiResponse(responseCode = "400", description = "Dados da requisição inválidos, autor não encontrado ou não possui papel USER",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<EntityModel<PostResponse>> criar(
            @RequestParam @Parameter(description = "ID do usuário autor do post (deve ser USER)", example = "2") Long autorId,
            @RequestBody @Valid PostRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(assembler.toModel(service.criar(autorId, request)));
    }

    @GetMapping("/feed")
    @Operation(summary = "Lista o feed de posts de forma paginada", description = "Retorna um Slice contendo os posts ordenados por data de criação decrescente, de forma otimizada usando ID baseado em cursor.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Feed retornado com sucesso")
    })
    public ResponseEntity<Slice<EntityModel<PostFeedResponse>>> listarFeed(
            @RequestParam(required = false) @Parameter(description = "Cursor: ID do último post da página anterior para paginação infinita", example = "10") Long lastId,
            @RequestParam(defaultValue = "10") @Parameter(description = "Tamanho da página", example = "10") int size) {
        return ResponseEntity.ok(service.listarFeed(lastId, size).map(feedAssembler::toModel));
    }

    @GetMapping
    @Operation(summary = "Lista todos os posts", description = "Retorna todos os posts cadastrados na base de dados de forma simples.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Posts retornados com sucesso")
    })
    public ResponseEntity<CollectionModel<EntityModel<PostResponse>>> listar() {
        return ResponseEntity.ok(assembler.toCollectionModel(service.listarTodos()));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Busca post por ID", description = "Retorna um post detalhado com base no seu identificador único.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Post encontrado com sucesso",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = PostResponse.class))),
        @ApiResponse(responseCode = "400", description = "Post não encontrado",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<EntityModel<PostResponse>> buscar(
            @PathVariable @Parameter(description = "ID do post", example = "1") Long id) {
        return ResponseEntity.ok(assembler.toModel(service.buscarPorId(id)));
    }

    @GetMapping("/slug/{slug}")
    @Operation(summary = "Busca post por slug", description = "Retorna um post detalhado com base no seu slug textual amigável.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Post encontrado com sucesso",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = PostResponse.class))),
        @ApiResponse(responseCode = "400", description = "Post com slug informado não foi encontrado",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<EntityModel<PostResponse>> buscarPorSlug(
            @PathVariable @Parameter(description = "Slug textual do post", example = "comecando-com-spring-boot-3") String slug) {
        return ResponseEntity.ok(assembler.toModel(service.buscarPorSlug(slug)));
    }

    @GetMapping("/search")
    @Operation(summary = "Filtra e pesquisa posts", description = "Busca posts dinamicamente filtrando por partes do título, conteúdo, categoria e/ou tag.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Resultados da pesquisa retornados com sucesso")
    })
    public ResponseEntity<CollectionModel<EntityModel<PostResponse>>> pesquisar(
            @RequestParam(required = false) @Parameter(description = "Parte do título do post", example = "Spring") String titulo,
            @RequestParam(required = false) @Parameter(description = "Parte do conteúdo do post", example = "webmvc") String conteudo,
            @RequestParam(required = false) @Parameter(description = "Nome exato da categoria", example = "Tecnologia") String categoria,
            @RequestParam(required = false) @Parameter(description = "Nome exato da tag", example = "Java") String tag) {
        return ResponseEntity.ok(assembler.toCollectionModel(service.pesquisar(titulo, conteudo, categoria, tag)));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Atualiza um post existente", description = "Atualiza o título, conteúdo, categoria e tags de um post. Somente o autor do post original pode alterá-lo.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Post atualizado com sucesso",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = PostResponse.class))),
        @ApiResponse(responseCode = "400", description = "Dados da requisição inválidos, post não encontrado, autor não encontrado ou usuário não possui permissão para editar",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<EntityModel<PostResponse>> atualizar(
            @PathVariable @Parameter(description = "ID do post a ser atualizado", example = "1") Long id,
            @RequestParam @Parameter(description = "ID do usuário solicitante (deve ser o autor original)", example = "2") Long autorId,
            @RequestBody @Valid PostRequest request) {
        return ResponseEntity.ok(assembler.toModel(service.atualizar(id, autorId, request)));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Exclui um post", description = "Exclui o post e todos os seus comentários. Somente o autor do post original pode excluí-lo.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "204", description = "Post excluído com sucesso"),
        @ApiResponse(responseCode = "400", description = "Post não encontrado, autor não encontrado ou usuário não possui permissão para excluir",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<Void> excluir(
            @PathVariable @Parameter(description = "ID do post a ser excluído", example = "1") Long id,
            @RequestParam @Parameter(description = "ID do usuário solicitante (deve ser o autor original)", example = "2") Long autorId) {
        service.excluir(id, autorId);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{id}/curtir")
    @Operation(summary = "Curte um post", description = "Registra a curtida de um usuário em um post específico. Cada usuário só pode curtir cada post uma vez.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Post curtido com sucesso"),
        @ApiResponse(responseCode = "400", description = "Post não encontrado, usuário não encontrado ou curtida duplicada/inválida",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<Void> curtir(
            @PathVariable @Parameter(description = "ID do post a ser curtido", example = "1") Long id,
            @RequestParam @Parameter(description = "ID do usuário que está curtindo", example = "2") Long usuarioId) {
        service.curtir(id, usuarioId);
        return ResponseEntity.ok().build();
    }
}
