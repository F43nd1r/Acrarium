package com.faendir.acra;

import com.faendir.acra.service.multipart.Rfc1341MultipartResolver;
import org.springframework.lang.NonNull;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.security.SecurityAutoConfiguration;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.web.servlet.support.SpringBootServletInitializer;
import org.springframework.context.annotation.Bean;
import org.springframework.web.multipart.MultipartResolver;

@SpringBootApplication(exclude = {SecurityAutoConfiguration.class})
public class BackendApplication extends SpringBootServletInitializer {
    public static void main(String[] args) {
        SpringApplication.run(BackendApplication.class, args);
    }

    @NonNull
    @Override
    protected SpringApplicationBuilder configure(@NonNull SpringApplicationBuilder builder) {
        return builder.sources(BackendApplication.class);
    }

    @NonNull
    @Bean
    public MultipartResolver multiPartResolver() {
        return new Rfc1341MultipartResolver();
    }
}
