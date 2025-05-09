package com.l1maor.vehicleworkshop.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Vehicle Workshop API")
                        .version("1.0")
                        .description("API for vehicle inventory and conversion management")
                        .contact(new Contact().name("Ed").email("contact@example.com")));
    }
}
