package com.vn.fruitcart.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class StaticResourcesWebConfiguration
    implements WebMvcConfigurer {

  @Value("${upload.file.base.path}")
  private String baseURI;

  @Override
  public void addResourceHandlers(ResourceHandlerRegistry registry) {
    registry.addResourceHandler("/storage/**")
        .addResourceLocations(baseURI);
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
