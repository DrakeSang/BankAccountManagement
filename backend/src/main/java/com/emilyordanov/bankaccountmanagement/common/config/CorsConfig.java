package com.emilyordanov.bankaccountmanagement.common.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * CORS configuration for the frontend.
 * <p>
 * React with Vite usually runs on:
 * http://localhost:5173
 * <p>
 * Spring Boot runs on:
 * http://localhost:8080
 * <p>
 * Because these are different origins, the browser blocks requests by default
 * unless the backend allows them.
 * <p>
 * This configuration allows the React frontend to call backend endpoints under /api/**.
 */
@Configuration
public class CorsConfig implements WebMvcConfigurer {
    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/api/**")
                /**
                 * Allows requests from the local Vite React frontend.
                 */
                .allowedOrigins("http://localhost:5173")

                /**
                 * Allows the HTTP methods used by our API.
                 */
                .allowedMethods("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS")

                /**
                 * Allows common request headers such as Content-Type.
                 */
                .allowedHeaders("*");
    }
}
