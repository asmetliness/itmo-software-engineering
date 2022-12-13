package com.artefact.api.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class ImagesConfig implements WebMvcConfigurer {
    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        registry.addResourceHandler("/images/users/**")
                .addResourceLocations("classpath:/static/images/users/");

        registry.addResourceHandler("/images/artifacts/**")
                .addResourceLocations("classpath:/static/images/artifacts/");
    }
}
