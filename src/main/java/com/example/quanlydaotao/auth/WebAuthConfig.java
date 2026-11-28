package com.example.quanlydaotao.auth;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebAuthConfig implements WebMvcConfigurer {

    private final AuthInterceptor authInterceptor;

    public WebAuthConfig(AuthInterceptor authInterceptor) {
        this.authInterceptor = authInterceptor;
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(authInterceptor)
                .addPathPatterns("/api/students/**", "/api/grades/**", "/api/n8/**");
    }
}
