package com.tsong.cmall.config;

import com.tsong.cmall.common.Constants;
import com.tsong.cmall.config.requestHandler.NoRepeatSubmitInterceptor;
import com.tsong.cmall.config.requestHandler.TokenToAdminUserMethodArgumentResolver;
import com.tsong.cmall.config.requestHandler.TokenToMallUserMethodArgumentResolver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurationSupport;

import java.util.List;

/**
 * @Author Tsong
 * @Date 2023/4/10 18:09
 */
@Configuration
public class WebMvcConfig extends WebMvcConfigurationSupport {
    private static final Logger logger = LoggerFactory.getLogger(WebMvcConfig.class);
    @Autowired
    private TokenToMallUserMethodArgumentResolver tokenToMallUserMethodArgumentResolver;
    @Autowired
    private TokenToAdminUserMethodArgumentResolver tokenToAdminUserMethodArgumentResolver;
    @Autowired
    private NoRepeatSubmitInterceptor noRepeatSubmitInterceptor;

    /**
     * @param argumentResolvers
     * @tip @TokenToMallUser @TokenToAdminUser 添加方法参数注解处理方法
     */
    public void addArgumentResolvers(List<HandlerMethodArgumentResolver> argumentResolvers) {
        argumentResolvers.add(tokenToMallUserMethodArgumentResolver);
        argumentResolvers.add(tokenToAdminUserMethodArgumentResolver);
    }

    /**
     * @Description 添加方法注解处理方法
     * @Param [interceptor]
     * @Return void
     */
    public void addInterceptors(InterceptorRegistry interceptor){
        interceptor.addInterceptor(noRepeatSubmitInterceptor).addPathPatterns("/api/**");
    }

    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        String path = "file:" + Constants.FILE_UPLOAD_DIC;
        logger.info("registry static resource path: " + path);
        registry.addResourceHandler("/upload/**").addResourceLocations(path);
        registry.addResourceHandler("/goods-img/**").addResourceLocations(path);
        registry.
                addResourceHandler("/swagger-ui/**")
                .addResourceLocations("classpath:/META-INF/resources/webjars/swagger-ui/4.18.2/")
                .resourceChain(false);
    }

    /**
     * 跨域配置
     *
     * @param registry
     */
    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**").allowedOriginPatterns("*")
                .allowedMethods("GET", "HEAD", "POST", "PUT", "DELETE", "OPTIONS")
                .allowCredentials(true).maxAge(3600);
    }
}
