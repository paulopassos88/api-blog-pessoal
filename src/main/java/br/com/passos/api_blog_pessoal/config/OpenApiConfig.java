package br.com.passos.api_blog_pessoal.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.License;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("API Blog Pessoal")
                        .description("API REST para gerenciamento de um blog pessoal, permitindo controle de usuários, posts, categorias, tags e comentários.")
                        .version("v1.0.0")
                        .contact(new Contact()
                                .name("Paulo Passos")
                                .email("paulo.passos.pereira1@outlook.com")
                                .url("https://github.com/paulopassos88")
                        )
                        .license(new License()
                                .name("Apache 2.0")
                                .url("https://www.apache.org/licenses/LICENSE-2.0")
                        )
                );
    }
}
