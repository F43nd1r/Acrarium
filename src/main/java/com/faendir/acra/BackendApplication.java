package com.faendir.acra;

import com.faendir.acra.config.AcraConfiguration;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.web.servlet.support.SpringBootServletInitializer;
import org.springframework.context.annotation.PropertySource;
import org.springframework.lang.NonNull;

@SpringBootApplication(exclude = {SecurityAutoConfiguration.class})
@PropertySource("classpath:default.properties")
@PropertySource(value = "file:${user.home}/.acra/application.properties", ignoreResourceNotFound = true)
@EnableConfigurationProperties(AcraConfiguration.class)
public class BackendApplication extends SpringBootServletInitializer {
    public static void main(String[] args) {
        SpringApplication.run(BackendApplication.class, args);
    }

    @NonNull
    @Override
    protected SpringApplicationBuilder configure(@NonNull SpringApplicationBuilder builder) {
        return builder.sources(BackendApplication.class);
    }
}
