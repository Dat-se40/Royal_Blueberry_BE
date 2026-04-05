package com.example.Royal_Blueberry.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

        public static final String SECURITY_SCHEME_NAME = "bearerAuth";

        @Bean
        public OpenAPI customOpenAPI() {
                return new OpenAPI()
                        .info(new Info()
                                .title("Royal Blueberry API")
                                .version("v1")
                                .description("""
                                        Dictionary API with JWT authentication, word lookup,
                                        semantic search, and package management.
                                        Only protected endpoints declare bearer auth in the spec.
                                        """))
                                .components(new Components()
                                        .addSecuritySchemes(SECURITY_SCHEME_NAME,
                                                new SecurityScheme()
                                                        .name("Authorization")
                                                        .type(SecurityScheme.Type.HTTP)
                                                        .scheme("bearer")
                                                        .bearerFormat("JWT")
                                                        .description("Enter the JWT access token.")));
        }
}
