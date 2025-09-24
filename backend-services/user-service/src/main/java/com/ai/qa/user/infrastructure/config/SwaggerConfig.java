package com.ai.qa.user.infrastructure.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.servers.Server;
import io.swagger.v3.oas.models.OpenAPI;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

//import static sun.net.www.protocol.http.AuthCacheValue.Type.Server;

@Configuration  //声明这是一个配置类
public class SwaggerConfig {
    // ******************** 配置swagger ************************
    // step1: 引入依赖>springdoc-openapi-ui
    // step2: 创建配置类>SwaggerConfig.java
    //          2.1 基础信息配置（info对象）
    //          2.2 安全配置（SecurityScheme 和 SecurityRequirement ）
    // step3: 配置文件管理(在 application.yml 中，可以配置一些 Swagger 的通用属性。)
    // step4: API 分组与模块化(推荐，暂时没有做)最佳实践是按照业务模块或服务进行分组。
    // step5: 使用注解增强文档细节(Controller 和 DTO 类上使用注解来提供更详细的文档信息)
    //          参照OrderController中注解的使用（@Tag/@Operation/@ApiResponse等）

    @Bean   // 将 OpenAPI 对象注入到 Spring 容器中
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                // Info对象: 包含 API 文档的基本信息，如标题、版本、描述和许可证
                .info(new Info()
                        .title("航空公司订单管理系统")
                        .description("航空公司订单管理系统的RESTful")
                        .version("1.0.0")
                        .contact(new Contact()
                                .name("Postion")
                                .email("support@postion.app")
                                .url("https://github.com/wang-rui-rae"))
                       )
                .servers(java.util.Arrays.asList(
                        new Server().url("http://52.53.199.13:8080").description("开发环境"),
                        new Server().url("https://api.airline.com").description("生产环境")
                ))

                //安全配置
                //在商业项目中，API 往往需要认证和授权。Swagger 应该能够展示如何传递令牌（如 JWT）
                //你可以通过配置 SecurityScheme 和 SecurityRequirement 来实现。
                .addSecurityItem(new SecurityRequirement().addList("bearerAuth"))
                .components(new Components()
                        .addSecuritySchemes("bearerAuth", new SecurityScheme()
                                .name("bearerAuth")
                                .type(SecurityScheme.Type.HTTP)
                                .scheme("bearer")
                                .bearerFormat("JWT")
                                .description("JWT认证令牌")));
    }
}
