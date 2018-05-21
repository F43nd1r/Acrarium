package com.faendir.acra.rest;

import com.faendir.acra.rest.multipart.Rfc1341MultipartResolver;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.lang.NonNull;
import org.springframework.web.multipart.MultipartResolver;

/**
 * @author lukas
 * @since 16.05.18
 */
@Configuration
public class RestConfiguration {
    @NonNull
    @Bean
    public static MultipartResolver multiPartResolver() {
        return new Rfc1341MultipartResolver();
    }
}
