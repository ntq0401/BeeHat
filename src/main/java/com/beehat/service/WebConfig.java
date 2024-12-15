package com.beehat.service;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
@Configuration
public class WebConfig implements WebMvcConfigurer {
    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // Ánh xạ URL "/product-images/**" tới thư mục "D:/uploads/product-img/"
        registry.addResourceHandler("/product-img/**")
                .addResourceLocations("file:///D:/uploads/product-img/");
    }
}
