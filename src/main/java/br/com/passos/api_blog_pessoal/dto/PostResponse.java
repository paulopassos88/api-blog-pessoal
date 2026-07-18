package br.com.passos.api_blog_pessoal.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDateTime;

@Schema(description = "Dados de retorno de um post completo")
public record PostResponse(
    @Schema(description = "Identificador único do post", example = "1")
    Long id,

    @Schema(description = "Título do post", example = "Começando com Spring Boot 3")
    String titulo,

    @Schema(description = "Slug amigável para URL", example = "comecando-com-spring-boot-3")
    String slug,

    @Schema(description = "Conteúdo completo em markdown do post", example = "Neste post vamos falar sobre...")
    String conteudo,

    @Schema(description = "Nome do autor do post", example = "Paulo Passos")
    String nomeAutor,

    @Schema(description = "Categoria do post")
    CategoriaResponse categoria,

    @Schema(description = "Lista de tags vinculadas ao post")
    java.util.List<TagResponse> tags,

    @Schema(description = "Total de curtidas do post", example = "10")
    int totalCurtidas,

    @Schema(description = "Tempo estimado de leitura em minutos", example = "3")
    int tempoLeitura,

    @Schema(description = "Data e hora de criação do post", example = "2026-07-17 10:00:00")
    java.time.LocalDateTime dataCriacao
) {
}
