package com.softsquare.report.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {

	@Override
	public void addCorsMappings(CorsRegistry registry) {
		// registry.addMapping("/**").allowedMethods("*").allowedHeaders("*").allowCredentials(true).allowedOrigins(
		// // "https://localhost"
		// // ,"http://localhost"
		// // ,"https://localhost:9000"
		// // ,"http://localhost:9000"
		// "*");
		registry.addMapping("/**").allowedMethods("*").allowedHeaders("*").allowCredentials(true).allowedOrigins("*");
	}

}
