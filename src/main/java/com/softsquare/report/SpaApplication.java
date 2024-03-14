package com.softsquare.report;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.web.servlet.support.SpringBootServletInitializer;
import org.springframework.cache.annotation.EnableCaching;

import com.softsquare.report.core.utils.ApplicationProperties;

@SpringBootApplication
@EnableCaching
@EnableConfigurationProperties(ApplicationProperties.class)
public class SpaApplication extends SpringBootServletInitializer {

    @Override
    protected SpringApplicationBuilder configure(SpringApplicationBuilder application) {
        return application.sources(SpaApplication.class);
    }

    public static void main(String[] args) {
        SpringApplication.run(SpaApplication.class, args);
    }

}

