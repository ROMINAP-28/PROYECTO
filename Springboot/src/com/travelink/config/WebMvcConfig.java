package com.travelink.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.http.CacheControl;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.ViewControllerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.time.Duration;
import java.util.Arrays;

@Configuration
public class WebMvcConfig implements WebMvcConfigurer {

    @Autowired
    private Environment environment;

    @Override
    public void addViewControllers(ViewControllerRegistry registry) {
        registry.addViewController("/").setViewName("forward:/index.html");
    }

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        boolean isDev = Arrays.asList(environment.getActiveProfiles()).contains("dev");
        CacheControl cacheControl = isDev 
                ? CacheControl.noCache().mustRevalidate() 
                : CacheControl.maxAge(Duration.ofDays(7)).cachePublic();

        registry.addResourceHandler("/css/**")
                .addResourceLocations("file:./Springboot/resources/static/css/", "file:./backend/resources/static/css/", "classpath:/static/css/")
                .setCacheControl(cacheControl);

        registry.addResourceHandler("/js/**")
                .addResourceLocations("file:./Springboot/resources/static/js/", "file:./backend/resources/static/js/", "classpath:/static/js/")
                .setCacheControl(cacheControl);

        registry.addResourceHandler("/img/**")
                .addResourceLocations("file:./Springboot/resources/static/img/", "file:./backend/resources/static/img/", "classpath:/static/img/")
                .setCacheControl(cacheControl);

        // Raíz y páginas HTML
        registry.addResourceHandler("/**")
                .addResourceLocations(
                        "file:./Springboot/resources/static/",
                        "file:./backend/resources/static/",
                        "classpath:/static/"
                )
                .setCacheControl(isDev ? CacheControl.noCache().mustRevalidate() : CacheControl.empty());
    }
}
