package br.com.passos.api_blog_pessoal;

import br.com.passos.api_blog_pessoal.support.ApiClient;
import br.com.passos.api_blog_pessoal.support.DatabaseCleaner;
import br.com.passos.api_blog_pessoal.support.TestcontainersConfiguration;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.actuate.observability.AutoConfigureObservability;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.context.annotation.Import;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;

/**
 * Base de todos os testes de integração ({@code *IT}).
 * <ul>
 *   <li>Sobe a aplicação completa em porta aleatória.</li>
 *   <li>Usa PostgreSQL 16 real via Testcontainers ({@link TestcontainersConfiguration}),
 *       com o schema criado pelas migrations do Flyway.</li>
 *   <li>Antes de cada teste, trunca todas as tabelas ({@link DatabaseCleaner}),
 *       garantindo isolamento hermético entre os testes.</li>
 * </ul>
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureObservability
@ActiveProfiles("test")
@Import(TestcontainersConfiguration.class)
public abstract class AbstractIntegrationTest {

    @Autowired
    protected TestRestTemplate restTemplate;

    @Autowired
    protected JdbcTemplate jdbcTemplate;

    protected ApiClient api;

    private DatabaseCleaner databaseCleaner;

    @BeforeEach
    void cleanDatabase() {
        if (databaseCleaner == null) {
            databaseCleaner = new DatabaseCleaner(jdbcTemplate);
        }
        databaseCleaner.limpar();
        api = new ApiClient(restTemplate);
    }

    protected int contarLinhas(String sql, Object... args) {
        Integer total = jdbcTemplate.queryForObject(sql, Integer.class, args);
        return total == null ? 0 : total;
    }
}
