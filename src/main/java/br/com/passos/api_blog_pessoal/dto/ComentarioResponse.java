package br.com.passos.api_blog_pessoal.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDateTime;

@Schema(description = "Dados de retorno de um comentário")
public record ComentarioResponse(
    @Schema(description = "Identificador único do comentário", example = "1")
    Long id,

    @Schema(description = "Texto do comentário", example = "Excelente artigo, muito explicativo!")
    String texto,

    @Schema(description = "Nome do autor do comentário", example = "Paulo Passos")
    String nomeAutor,

    @Schema(description = "Total de curtidas recebidas pelo comentário", example = "5")
    int totalCurtidas,

    @Schema(description = "Lista de respostas enviadas a este comentário")
    java.util.List<ComentarioResponse> respostas,

    @Schema(description = "Data de criação do comentário", example = "2026-07-17 20:30:00")
    java.time.LocalDateTime dataCriacao
) {
}
