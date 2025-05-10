package com.vn.fruitcart.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class InterceptionConfig implements WebMvcConfigurer {
    private final UserBlockInterceptor userBlockInterceptor;

    public InterceptionConfig(UserBlockInterceptor userBlockInterceptor) {
        this.userBlockInterceptor = userBlockInterceptor;
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(userBlockInterceptor)
               .excludePathPatterns("/login", "/logout", "/register", "/storage/**", "/admin/assets/**", "/client/assets/**", "/shared/assets/**");
    }
}
