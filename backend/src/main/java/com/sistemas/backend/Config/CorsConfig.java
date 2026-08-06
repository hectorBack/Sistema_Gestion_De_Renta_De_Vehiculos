package com.sistemas.backend.Config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class CorsConfig implements WebMvcConfigurer {

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/api/**") // Aplica a todos los endpoints bajo /api
                .allowedOrigins("http://localhost:4200") // Origen del servidor de dev de Angular
                .allowedMethods("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS") // Métodos HTTP permitidos
                .allowedHeaders("*") // Permite todos los encabezados (Authorization, Content-Type, etc.)
                .allowCredentials(true) // Permite el envío de cookies o headers de autenticación
                .maxAge(3600); // Guarda la respuesta de preflight (OPTIONS) por 1 hora en cache
    }
}
