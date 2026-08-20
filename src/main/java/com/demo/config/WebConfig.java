package com.demo.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {
    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/api/**") // Apply to all API endpoints
                .allowedOrigins("http://localhost:5175/", "http://localhost:5173",
                        "https://banking-simulator-xq4p.onrender.com") // Allow your React app
                .allowedMethods("GET", "POST", "PUT", "DELETE", "PATCH") // Allowed verbs
                .allowedHeaders("*") // Allow all headers
                .allowCredentials(true);
    }

}