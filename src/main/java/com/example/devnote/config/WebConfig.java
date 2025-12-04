package com.example.devnote.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

// 这个类是用来配置静态资源的，比如图片，css，js等
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
