package com.faendir.acra;

import com.faendir.acra.service.multipart.Rfc1341MultipartResolver;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.security.SecurityAutoConfiguration;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.web.support.SpringBootServletInitializer;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.AdviceMode;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.web.multipart.MultipartResolver;

import java.security.SecureRandom;

@SpringBootApplication(exclude = { SecurityAutoConfiguration.class })
@ComponentScan
@EnableCaching(mode = AdviceMode.ASPECTJ)
public class BackendApplication extends SpringBootServletInitializer {
	@Override
	protected SpringApplicationBuilder configure(SpringApplicationBuilder builder) {
		return builder.sources(BackendApplication.class);
	}

	public static void main(String[] args) {
		SpringApplication.run(BackendApplication.class, args);
	}

	@Bean
	public SecureRandom secureRandom(){
		return new SecureRandom();
	}

	@Bean
	public MultipartResolver multiPartResolver(){
		return new Rfc1341MultipartResolver();
	}

}
