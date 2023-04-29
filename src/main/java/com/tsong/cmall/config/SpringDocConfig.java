package com.tsong.cmall.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import jakarta.annotation.Resource;
import org.apache.commons.lang3.StringUtils;
import org.springdoc.core.customizers.OpenApiCustomizer;
import org.springdoc.core.models.GroupedOpenApi;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Arrays;
import java.util.Comparator;
import java.util.stream.Collectors;

/**
 * @Author Tsong
 * @Date 2023/4/13 16:48
 */
@Configuration
public class SpringDocConfig {

    @Bean
    public GroupedOpenApi adminAPI() {
        return GroupedOpenApi.builder()
                .group("admin")
                .addOpenApiCustomizer(sortTagsAlphabetically())
                .pathsToMatch("/admin/**")
                .build();
    }

    @Bean
    public GroupedOpenApi userAPI() {
        return GroupedOpenApi.builder()
                .addOpenApiCustomizer(sortTagsAlphabetically())
                .group("user")
                .pathsToMatch("/api/**")
                .build();
    }

    @Bean
    public OpenAPI openAPI(){
        return new OpenAPI()
                .info(info())
                .components(components())
                .addSecurityItem(
                        new SecurityRequirement().addList("token", Arrays.asList("read", "write")));
    }

    private Info info(){
        return new Info()
                .title("CMall接口文档")
                .description("Restful 接口文档")
                .version("1.0");
    }

    private Components components(){
        return new Components().addSecuritySchemes("token",
                new SecurityScheme()
                        .type(SecurityScheme.Type.HTTP)
                        .name("token")
                        .scheme("basic")
                        .in(SecurityScheme.In.HEADER)
                        .name("Authorization")
        );
    }

    private OpenApiCustomizer sortTagsAlphabetically(){
        return openApi -> openApi.setTags(openApi.getTags()
                .stream()
                .sorted(Comparator.comparing(tag -> StringUtils.stripAccents(tag.getName())))
                .collect(Collectors.toList()));
    }
}
