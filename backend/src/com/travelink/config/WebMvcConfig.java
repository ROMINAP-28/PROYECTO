package com.travelink.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.ViewControllerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebMvcConfig implements WebMvcConfigurer {

    @Override
    public void addViewControllers(ViewControllerRegistry registry) {
        registry.addViewController("/").setViewName("forward:/index.html");
    }

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // Caching de 7 días para recursos estáticos (CSS, JS, Imágenes) - FASE 11
        registry.addResourceHandler("/css/**")
                .addResourceLocations("file:./backend/resources/static/css/", "classpath:/static/css/")
                .setCacheControl(org.springframework.http.CacheControl.maxAge(java.time.Duration.ofDays(7)).cachePublic());

        registry.addResourceHandler("/js/**")
                .addResourceLocations("file:./backend/resources/static/js/", "classpath:/static/js/")
                .setCacheControl(org.springframework.http.CacheControl.maxAge(java.time.Duration.ofDays(7)).cachePublic());

        registry.addResourceHandler("/img/**")
                .addResourceLocations("file:./backend/resources/static/img/", "classpath:/static/img/")
                .setCacheControl(org.springframework.http.CacheControl.maxAge(java.time.Duration.ofDays(7)).cachePublic());

        // Raíz y páginas HTML
        registry.addResourceHandler("/**")
                .addResourceLocations(
                        "file:./backend/resources/static/",
                        "classpath:/static/"
                );
    }
}
