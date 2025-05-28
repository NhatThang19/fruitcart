package com.vn.fruitcart.config;

import java.nio.file.Paths;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class StaticResourcesWebConfiguration
        implements WebMvcConfigurer {

    @Value("${file.upload-dir}")
    private String uploadDirProperty;

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        String resolvedFileSystemPath = Paths.get(uploadDirProperty)
                .toAbsolutePath()
                .normalize()
                .toString();

        if (!resolvedFileSystemPath.endsWith("/") && !resolvedFileSystemPath.endsWith("\\")) {
            resolvedFileSystemPath = resolvedFileSystemPath + "/";
        }

        String resourceLocation = "file:" + resolvedFileSystemPath;

        registry.addResourceHandler("/storage/**")
                .addResourceLocations(resourceLocation);
        registry.addResourceHandler("/static/**")
                .addResourceLocations("classpath:/static/");

        registry.addResourceHandler("/admin/**")
                .addResourceLocations("classpath:/static/admin/");

        registry.addResourceHandler("/client/**")
                .addResourceLocations("classpath:/static/client/");

        registry.addResourceHandler("/shared/**")
                .addResourceLocations("classpath:/static/shared/");
    }
}
