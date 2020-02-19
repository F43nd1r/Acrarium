/*
 * (C) Copyright 2020 Lukas Morawietz (https://github.com/F43nd1r)
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

package com.faendir.acra.service;

import com.vaadin.flow.router.RouteConfiguration;
import com.vaadin.flow.server.startup.ApplicationRouteRegistry;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.lang.NonNull;

import javax.servlet.ServletContext;

@Configuration
@ConditionalOnProperty(prefix = "spring.mail", name = "host")
public class MailConfiguration {

    @Bean
    public static RouteConfiguration routeConfiguration(@NonNull ServletContext servletContext) {
        return RouteConfiguration.forRegistry(ApplicationRouteRegistry.getInstance(servletContext));
    }
}
