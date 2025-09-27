package com.ai.qa.user.infrastructure.config;

import java.util.Arrays;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import lombok.RequiredArgsConstructor;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

//    private final JwtAuthFilter jwtAuthFilter;
    private final IdentityHeaderFilter identityHeaderFilter; // 需要在构造函数中注入
    private final UserDetailsService userDetailsService;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                // (推荐) 使用新的Lambda DSL配置CSRF (跨站请求伪造) ，更清晰
                .csrf(AbstractHttpConfigurer::disable)
                // 配置授权规则
                .authorizeHttpRequests(authz -> authz
                        // 明确放行所有公共路径
                        .requestMatchers(
                                "/", "/index.html", "/*.js", "/*.css", "/*.ico", "/*.png", "/assets/**", // 前端静态资源
                                "/api/auth/**", // 所有认证相关的API
                                // TODO 新追加的45行
//                                "/login", "/register",
                                "/swagger-ui.html", "/swagger-ui/**", "/v3/api-docs/**", "/webjars/**" // Swagger文档
                        ).permitAll()
                        // 其他任何请求都需要身份验证
                        .anyRequest().authenticated()
                )
                // 配置会话管理为无状态，因为我们用JWT
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                // 关联我们自定义的AuthenticationProvider
                .authenticationProvider(authenticationProvider())
                // 在UsernamePasswordAuthenticationFilter之前添加我们的JWT过滤器
//                .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class);
                .addFilterBefore(identityHeaderFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();


//        http
//                // 1. 禁用 CSRF (关键修改)
//                // 解决所有 POST/PUT/DELETE 请求返回 403 的问题
//                .csrf(AbstractHttpConfigurer::disable)
//
//                // 2. 禁用默认登录和认证 (关键修改)
//                .formLogin(AbstractHttpConfigurer::disable)
//                .httpBasic(AbstractHttpConfigurer::disable)
//
//                // 3. 确保 Session 策略是无状态 (Stateless)
//                .sessionManagement(sess -> sess.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
//
//                // 4. 应用 CORS 配置（注意：不需要显式调用 .cors()，Spring Boot 会自动应用同名 Bean）
//                // 确保您的 @Bean public CorsConfigurationSource corsConfigurationSource() 方法是存在的
//
//                // 5. 权限配置 (白名单和认证规则)
//                .authorizeHttpRequests(auth -> auth
//                        // 💡 开放登录和注册接口 (对应 Gateway 剥离 /api/auth 后的路径)
//                        .requestMatchers("/api/auth/**").permitAll()
//                        // 💡 开放 Swagger/OpenAPI 文档路径 (如果使用)
//                        .requestMatchers("/swagger-ui/**", "/v3/api-docs/**").permitAll()
//                        // 💡 允许 OPTIONS 预检请求通过 (跨域请求必备)
//                        .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()
//
//                        // 所有其他请求都需要认证
//                        .anyRequest().authenticated()
//                )
//
//                // 6. 身份过滤器链
//                // 确保 IdentityHeaderFilter 在 UsernamePasswordAuthenticationFilter 之前执行
//                .authenticationProvider(authenticationProvider())
//                .addFilterBefore(identityHeaderFilter, UsernamePasswordAuthenticationFilter.class);
//
//        return http.build();
    }

    // 注意：CORS(跨源资源共享)的配置在这里，但securityFilterChain中并没有显式调用.cors()
    // Spring Boot会自动寻找名为corsConfigurationSource的Bean并应用它
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        // (重要修改) 允许任何来源，或您服务器的公网IP。用"*"在开发和测试中最方便
        configuration.setAllowedOrigins(Arrays.asList("*"));
        configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        configuration.setAllowedHeaders(Arrays.asList("Authorization", "Content-Type", "X-Requested-With", "Accept"
                // 新追加了以下两个
                , "X-User-Id"
                , "X-User-Name"));
        configuration.setAllowCredentials(true);
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }


    @Bean
    public AuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider();
        authProvider.setUserDetailsService(userDetailsService);
        authProvider.setPasswordEncoder(passwordEncoder());
        return authProvider;
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }}

