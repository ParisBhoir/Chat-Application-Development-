package com.paris.api_gateway.config;

import com.paris.api_gateway.filter.JwtAuthFilter;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.web.server.WebFilter;

@Configuration
@RequiredArgsConstructor
public class FilterConfig {

    private final JwtAuthFilter jwtAuthFilter;

    @Bean
    @Order(Ordered.HIGHEST_PRECEDENCE)
    public WebFilter jwtFilter() {
        return jwtAuthFilter;
    }
}
