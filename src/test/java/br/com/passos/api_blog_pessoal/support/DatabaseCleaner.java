package br.com.passos.api_blog_pessoal.support;

import org.springframework.jdbc.core.JdbcTemplate;

import java.util.List;

/**
 * Limpa todas as tabelas de negócio entre os testes de integração.
 * <p>
 * A lista de tabelas é descoberta dinamicamente no catálogo do PostgreSQL
 * (excluindo o histórico do Flyway), então novas tabelas criadas por migrations
 * futuras passam a ser limpas sem alteração neste código.
 */
public class DatabaseCleaner {

    private static final String SQL_LISTAR_TABELAS = """
            SELECT tablename FROM pg_tables
            WHERE schemaname = 'public' AND tablename <> 'flyway_schema_history'
            """;

    private final JdbcTemplate jdbcTemplate;
    private String truncateSql;

    public DatabaseCleaner(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public void limpar() {
        jdbcTemplate.execute(montarTruncate());
    }

    private String montarTruncate() {
        if (truncateSql != null) {
            return truncateSql;
        }
        List<String> tabelas = jdbcTemplate.queryForList(SQL_LISTAR_TABELAS, String.class);
        truncateSql = "TRUNCATE TABLE " + String.join(", ", tabelas) + " RESTART IDENTITY CASCADE";
        return truncateSql;
    }
}
