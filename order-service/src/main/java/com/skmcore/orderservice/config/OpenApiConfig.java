package com.skmcore.orderservice.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI orderServiceOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Order Service API")
                        .description("EcomEx Order Management Microservice — create, track, and manage customer orders.")
                        .version("1.0.0")
                        .contact(new Contact()
                                .name("SKM Core Team")
                                .email("dev@skmcore.com"))
                        .license(new License()
                                .name("Proprietary")));
    }
}
