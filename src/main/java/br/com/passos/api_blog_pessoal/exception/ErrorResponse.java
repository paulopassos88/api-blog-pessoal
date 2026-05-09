package br.com.passos.api_blog_pessoal.exception;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.Map;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ErrorResponse {
    private LocalDateTime timestamp;
    private int status;
    private String title;
    private String detail;
    private String path;
    private Map<String, String> details;

    public ErrorResponse(LocalDateTime timestamp, int status, String title, String detail, String path) {
        this.timestamp = timestamp;
        this.status = status;
        this.title = title;
        this.detail = detail;
        this.path = path;
    }
}
