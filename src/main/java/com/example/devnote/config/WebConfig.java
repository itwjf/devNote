package com.example.devnote.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {
    @Value("${file.upload-dir}")
    private String uploadDir;

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // 让 /uploads/** 映射到本地文件目录
        String pathPattern = "/uploads/**";
        String resourceLocation = "file:" + uploadDir;

        registry.addResourceHandler(pathPattern)
                .addResourceLocations(resourceLocation);
    }



}
