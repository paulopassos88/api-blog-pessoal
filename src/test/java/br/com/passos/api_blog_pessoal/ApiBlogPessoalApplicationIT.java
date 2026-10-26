package br.com.passos.api_blog_pessoal;

import br.com.passos.api_blog_pessoal.model.Role;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.springframework.http.HttpStatus;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Smoke tests de infraestrutura (spec: docs/modules/infra-ops.md).
 */
@DisplayName("Infraestrutura da aplicação (IT)")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class ApiBlogPessoalApplicationIT extends AbstractIntegrationTest {

    @Test
    @Tag("BDD-INFRA-01")
    @DisplayName("BDD-INFRA-01: /actuator/health retorna UP com o banco conectado")
    void deveRetornarStatusUp_quandoConsultarHealth() {
        // Act
        var resposta = api.get("/actuator/health");

        // Assert
        assertThat(resposta.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(resposta.getBody().get("status").asText()).isEqualTo("UP");
    }

    @Test
    @Tag("BDD-INFRA-02")
    @DisplayName("BDD-INFRA-02: /actuator/prometheus expõe métricas no formato de scraping")
    void deveExporMetricas_quandoPrometheusConsultar() {
        // Arrange — gera ao menos uma requisição HTTP registrada nas métricas
        api.get("/api/v1/tags");

        // Act
        var resposta = restTemplate.getForEntity("/actuator/prometheus", String.class);

        // Assert
        assertThat(resposta.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(resposta.getBody())
                .contains("# TYPE")
                .contains("http_server_requests_seconds");
    }

    @Test
    @Order(1)
    @Tag("BDD-INFRA-03")
    @DisplayName("BDD-INFRA-03 (1/2): insere registro no banco")
    void deveInserirRegistro_paraVerificarIsolamento() {
        // Act
        api.criarUsuario(Role.USER);

        // Assert
        assertThat(contarLinhas("SELECT COUNT(*) FROM usuarios")).isEqualTo(1);
    }

    @Test
    @Order(2)
    @Tag("BDD-INFRA-03")
    @DisplayName("BDD-INFRA-03 (2/2): próximo teste começa com o banco zerado, inclusive sem os dados do seed V5")
    void deveIniciarComBancoVazio_quandoTesteAnteriorInseriuDados() {
        // Assert
        assertThat(contarLinhas("SELECT COUNT(*) FROM usuarios")).isZero();
        assertThat(contarLinhas("SELECT COUNT(*) FROM posts")).isZero();
        assertThat(contarLinhas("SELECT COUNT(*) FROM flyway_schema_history")).isPositive();
    }
}
