package com.example.bgl.gol.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.fasterxml.jackson.databind.ObjectMapper;

@Configuration
public class DataFormatConfiguration {

    @Bean
    public ObjectMapper gameStateOutputObjectMapper() {
        // currently no special configuration needed, for injection by utility classes only
        return new ObjectMapper();
    }

    @Bean
    public ObjectMapper anotherObjectMapper() {
        // currently no special configuration needed, for injection by utility classes only
        return new ObjectMapper();
    }

}
