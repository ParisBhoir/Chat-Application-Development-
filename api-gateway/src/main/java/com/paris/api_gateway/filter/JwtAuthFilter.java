package com.paris.api_gateway.filter;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Mono;

import javax.crypto.SecretKey;

@Component
public class JwtAuthFilter implements WebFilter {

    private static final Logger log = LoggerFactory.getLogger(JwtAuthFilter.class);
    private static final String SECRET_KEY = "mysecretkeymysecretkeymysecretkey12345"; // must match user-service

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, WebFilterChain chain) {
        String path = exchange.getRequest().getURI().getPath();
        log.debug("Incoming request path: {}", path);

        // 1️⃣ Allow public endpoints
       /* // Allow auth & WebSocket upgrade requests
        String upgrade = exchange.getRequest().getHeaders().getFirst("Upgrade");
        String connection = exchange.getRequest().getHeaders().getFirst("Connection");*/
        if (path.startsWith("/api/auth") || path.startsWith("/actuator") /*|| path.startsWith("/ws")*/
                /*"websocket".equalsIgnoreCase(upgrade) || (connection != null && connection.toLowerCase().contains("upgrade"))*/) {
            return chain.filter(exchange);
        }

        // 2️⃣ Extract token
        String authHeader = exchange.getRequest().getHeaders().getFirst(HttpHeaders.AUTHORIZATION);
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            log.warn("Missing or invalid Authorization header");
            exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
            return exchange.getResponse().setComplete();
        }

        String token = authHeader.substring(7);
        try {
            SecretKey key = Keys.hmacShaKeyFor(SECRET_KEY.getBytes());
            Claims claims = Jwts.parserBuilder()
                    .setSigningKey(key)
                    .build()
                    .parseClaimsJws(token)
                    .getBody();

            String username = claims.getSubject();
            String role = (String) claims.get("role");
            String userId = (String) claims.getId();

            log.info("Authenticated user: {} with role: {}", userId, role);

            // 3️⃣ Role-based access control (RBAC)
            if (path.startsWith("/api/users") && !"ADMIN".equalsIgnoreCase(role)) {
                log.warn("Access denied for user '{}' to {}", username, path);
                exchange.getResponse().setStatusCode(HttpStatus.FORBIDDEN);
                return exchange.getResponse().setComplete();
            }

            if (path.startsWith("/api/chat") &&
                    !("USER".equalsIgnoreCase(role) || "ADMIN".equalsIgnoreCase(role))) {
                log.warn("Access denied for user '{}' to {}", username, path);
                exchange.getResponse().setStatusCode(HttpStatus.FORBIDDEN);
                return exchange.getResponse().setComplete();
            }

            // 4️⃣ Pass user details downstream for auditing or personalization
            ServerHttpRequest mutatedRequest = exchange.getRequest().mutate()
                    .header("X-User-Id", userId)
                    .header("X-User-Name", username)
                    .header("X-User-Role", role)
                    .build();
            log.info("Forwarding headers: X-User-Name={}", username);
            log.info("Forwarding headers: X-User-Id={}", userId);
            return chain.filter(exchange.mutate().request(mutatedRequest).build());

        } catch (Exception e) {
            log.error("JWT validation failed: {}", e.getMessage());
            exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
            return exchange.getResponse().setComplete();
        }
    }
}
