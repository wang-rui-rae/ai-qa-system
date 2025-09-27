package com.ai.qa.gateway.api.web.filter;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;
import java.util.List;

@Slf4j
@Component
//@RefreshScope // 为了动态刷新JWT密钥
public class AuthenticationFilter implements GlobalFilter, Ordered {

    @Value("${jwt.secret}")
    private String jwtSecret;

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        ServerHttpRequest request = exchange.getRequest();

        // 1. 定义白名单路径，这些路径不需要JWT验证
        List<String> whiteList = List.of("/api/auth/register", "/api/auth/login");
        if (whiteList.contains(request.getURI().getPath())) {
            return chain.filter(exchange); // 放行
        }

        // 2. Token 缺失或格式错误，返回 401
        String authHeader = request.getHeaders().getFirst("Authorization");
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
            return exchange.getResponse().setComplete();
        }

        // 3. Token 验证（签名和过期时间）
        String token = authHeader.substring(7);

        try {
            Claims claims = Jwts.parser()
//                    .setSigningKey(jwtSecret.getBytes())
                    .setSigningKey(Decoders.BASE64.decode(jwtSecret))
                    .build()
                    .parseClaimsJws(token)
                    .getBody();

            // 4. 解析用户信息并注入请求头 (身份传递的最佳实践)
            // 验证通过，可以将用户信息放入请求头，传递给下游服务
            // ️ 实际项目中，应解析出 Roles/Scopes，并注入 X-User-Roles 等头部
            ServerHttpRequest mutatedRequest = request.mutate()
                    .header("X-User-Id", claims.getSubject())
                    .header("X-User-Name", claims.get("username", String.class))
                    .build();
            // 5. 验证成功，继续转发请求
            return chain.filter(exchange.mutate().request(mutatedRequest).build());
        } catch (Exception e) {
            exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
            return exchange.getResponse().setComplete();
        }
    }

    @Override
    public int getOrder() {
        // 鉴权过滤器应在日志过滤器之后，在路由之前，优先级要高
        return -100;
    }
}