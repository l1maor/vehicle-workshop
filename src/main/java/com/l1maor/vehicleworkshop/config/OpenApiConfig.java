package com.l1maor.vehicleworkshop.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.security.SecurityRequirement;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springdoc.core.models.GroupedOpenApi;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.beans.factory.annotation.Value;

@Configuration
public class OpenApiConfig {
    
    private static final Logger logger = LoggerFactory.getLogger(OpenApiConfig.class);

    @Value("${springdoc.swagger-ui.path:/swagger-ui.html}")
    private String swaggerPath;

    @Bean
    @Primary
    public OpenAPI customOpenAPI() {
        logger.info("Configuring OpenAPI documentation");
        final String securitySchemeName = "bearerAuth";
        
        logger.debug("Setting up OpenAPI with JWT security scheme: {}", securitySchemeName);
        
        return new OpenAPI()
                .info(new Info()
                        .title("Vehicle Workshop API")
                        .version("1.0")
                        .description("API for vehicle inventory and conversion management")
                        .contact(new Contact().name("Ed").email("contact@example.com")))
                .components(new Components()
                        .addSecuritySchemes(securitySchemeName,
                                new SecurityScheme()
                                        .type(SecurityScheme.Type.HTTP)
                                        .scheme("bearer")
                                        .bearerFormat("JWT")))
                .addSecurityItem(new SecurityRequirement().addList(securitySchemeName));
    }
    
    @Bean
    public GroupedOpenApi publicApi() {
        logger.info("Creating public API group for OpenAPI documentation");
        return GroupedOpenApi.builder()
                .group("public-api")
                .pathsToMatch("/api/**")
                .build();
    }
}
