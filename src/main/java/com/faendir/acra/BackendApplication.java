/*
 * (C) Copyright 2018 Lukas Morawietz (https://github.com/F43nd1r)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.faendir.acra;

import com.faendir.acra.config.AcraConfiguration;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.mail.MailSenderAutoConfiguration;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.web.servlet.support.SpringBootServletInitializer;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.PropertySource;
import org.springframework.lang.NonNull;

@SpringBootApplication(exclude = {SecurityAutoConfiguration.class})
@PropertySource("classpath:default.properties")
@PropertySource(value = "file:${user.home}/.config/acrarium/application.properties", ignoreResourceNotFound = true)
@PropertySource(value = "file:${user.home}/.acra/application.properties", ignoreResourceNotFound = true)
@EnableConfigurationProperties(AcraConfiguration.class)
@Import(MailSenderAutoConfiguration.class)
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
