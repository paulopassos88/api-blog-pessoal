package br.com.passos.api_blog_pessoal.exception;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDateTime;
import java.util.Map;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
@Schema(description = "Representação de um erro retornado pela API")
public class ErrorResponse {
    @Schema(description = "Timestamp em que o erro ocorreu", example = "2026-07-17T20:30:00")
    private LocalDateTime timestamp;

    @Schema(description = "Código de status HTTP do erro", example = "400")
    private int status;

    @Schema(description = "Título amigável ou categoria do erro", example = "Erro de validação de campos")
    private String title;

    @Schema(description = "Descrição detalhada do erro", example = "Um ou mais campos estão inválidos")
    private String detail;

    @Schema(description = "Caminho (URI) do endpoint que gerou o erro", example = "/api/v1/usuarios")
    private String path;

    @Schema(description = "Dicionário contendo detalhes adicionais do erro (ex: erros por campo de validação)")
    private Map<String, String> details;

    public ErrorResponse(LocalDateTime timestamp, int status, String title, String detail, String path) {
        this.timestamp = timestamp;
        this.status = status;
        this.title = title;
        this.detail = detail;
        this.path = path;
    }
}
