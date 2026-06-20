package com.example.demo.config;

import com.example.demo.interceptor.JwtInterceptor;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
@RequiredArgsConstructor
public class WebConfig implements WebMvcConfigurer {

    private final JwtInterceptor jwtInterceptor;
    private final UploadProperties uploadProperties;

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**")
            .allowedOrigins("http://localhost:5173", "http://localhost:3000")
            .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")
            .allowedHeaders("*");
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(jwtInterceptor)
            .addPathPatterns("/**")
            .excludePathPatterns(
                "/user/login",
                "/user/register",
                "/products",
                "/products/**",
                "/categories",
                "/categories/**",
                "/hello",
                "/images/**"
            );
    }

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // 将 /images/products/** 映射到本地 uploads/products/ 目录
        String urlPrefix = uploadProperties.getUrlPrefix();
        if (urlPrefix != null && !urlPrefix.isEmpty()) {
            registry.addResourceHandler(urlPrefix + "**")
                    .addResourceLocations("file:" + uploadProperties.getPath());
        }
    }
}
