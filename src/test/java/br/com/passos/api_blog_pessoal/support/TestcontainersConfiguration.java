package br.com.passos.api_blog_pessoal.support;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.context.annotation.Bean;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.utility.DockerImageName;

/**
 * Declara o container PostgreSQL como bean do Spring.
 * <p>
 * Como o contexto de teste é cacheado pelo Spring entre as classes {@code *IT}
 * que compartilham a mesma configuração, um único container é iniciado para
 * toda a suíte de integração e encerrado junto com o contexto.
 * {@link ServiceConnection} injeta URL, usuário e senha no DataSource automaticamente.
 */
@TestConfiguration(proxyBeanMethods = false)
public class TestcontainersConfiguration {

    @Bean
    @ServiceConnection
    PostgreSQLContainer<?> postgresContainer() {
        return new PostgreSQLContainer<>(DockerImageName.parse("postgres:16-alpine"));
    }
}
