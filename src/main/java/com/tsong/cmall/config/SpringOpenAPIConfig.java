package com.tsong.cmall.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springdoc.core.models.GroupedOpenApi;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @Author Tsong
 * @Date 2023/4/13 16:48
 */
@Configuration
public class SpringOpenAPIConfig {
    @Bean
    public GroupedOpenApi adminAPI() {
        return GroupedOpenApi.builder()
                .group("Admin API")
                .pathsToMatch("/com/tsong/cmall/controller/admin/**")
                .build();
    }

    @Bean
    public GroupedOpenApi userAPI() {
        return GroupedOpenApi.builder()
                .group("User API")
                .pathsToMatch("/com/tsong/cmall/controller/mall/**")
                .build();
    }

    @Bean
    public OpenAPI openAPI(){
        return new OpenAPI()
                .info(info());
    }

    private Info info(){
        return new Info()
                .title("CMall接口文档")
                .description("OpenAPI接口文档")
                .version("1.0");
    }
}
