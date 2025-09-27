package com.ai.qa.user.infrastructure.config;

import com.ai.qa.user.application.JwtService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
@RequiredArgsConstructor
public class JwtAuthFilter extends OncePerRequestFilter {

    private final JwtService jwtService;
    private final UserDetailsService userDetailsService;


    @Override
    protected void doFilterInternal(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain
    ) throws ServletException, IOException {
//        final String authHeader = request.getHeader("Authorization");
//        final String jwt;
//        final String username;
//        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
//            filterChain.doFilter(request, response);
//            return;
//        }
//        jwt = authHeader.substring(7).trim();
//        username = jwtService.extractUsername(jwt);
//
//        if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {
//            UserDetails userDetails = this.userDetailsService.loadUserByUsername(username);
//            if (jwtService.isTokenValid(jwt, userDetails)) {
//                UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
//                        userDetails,
//                        null,
//                        userDetails.getAuthorities()
//                );
//                authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
//                SecurityContextHolder.getContext().setAuthentication(authToken);
//            }
//        }
//        filterChain.doFilter(request, response);

        /**
         * 最佳实践：中央认证，身份透传
         *
         * 认证 (Authentication)	API Gateway
         *      1. 验证 JWT 的签名和过期时间。
         *      2. 验证通过后，移除原始的 Authorization JWT 头。
         *      3. 将验证后的用户身份信息（user-id, roles 等）注入到自定义请求头（如 X-User-Id）中。
         *
         *  鉴权 (Authorization)	User Service
         *      1. 信任来自 Gateway 的 X-User-Id 头，不再进行 JWT 验证。
         *      2. 根据 X-User-Id 加载用户权限信息。
         *      3. 执行业务层面的权限校验（例如：这个用户是否有权限修改 ID=3 的数据）。
         */

        // 1. 获取 Gateway 注入的头部
        final String userId = request.getHeader("X-User-Id");
        if (userId == null) {
            // 可能是未登录用户或内部服务调用，放行给下一个过滤器处理
            filterChain.doFilter(request, response);
            return;
        }

        // 2. 使用 userId 加载用户详情 (UserDetails)
        UserDetails userDetails = this.userDetailsService.loadUserByUsername(userId); // 假设您的 UserDetailsService 可以用 userId 加载

        // 3. 设置 SecurityContextHolder
        UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
                userDetails, null, userDetails.getAuthorities()
        );
        SecurityContextHolder.getContext().setAuthentication(authToken);

        filterChain.doFilter(request, response);
    }
}