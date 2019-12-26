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
package com.faendir.acra.liquibase;

import com.faendir.acra.BackendApplication;
import com.faendir.acra.security.VaadinSessionSecurityContextHolderStrategy;
import liquibase.exception.LiquibaseException;
import liquibase.integration.spring.SpringLiquibase;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.ImportAutoConfiguration;
import org.springframework.boot.autoconfigure.jdbc.EmbeddedDataSourceConfiguration;
import org.springframework.boot.autoconfigure.liquibase.LiquibaseAutoConfiguration;
import org.springframework.boot.autoconfigure.liquibase.LiquibaseProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.FilterType;
import org.springframework.core.io.ResourceLoader;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.junit4.SpringRunner;

import javax.sql.DataSource;
import javax.validation.Validation;
import javax.validation.Validator;
import java.util.List;

/**
 * @author lukas
 * @since 25.06.18
 */
@DataJpaTest
@RunWith(SpringRunner.class)
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@ComponentScan(basePackageClasses = BackendApplication.class, excludeFilters = {@ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, value = SpringLiquibase.class), @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, value = VaadinSessionSecurityContextHolderStrategy.class)})
@ImportAutoConfiguration(exclude = {LiquibaseAutoConfiguration.class, EmbeddedDataSourceConfiguration.class})
@EnableConfigurationProperties(LiquibaseProperties.class)
public abstract class LiquibaseTest {
    @Autowired
    ResourceLoader resourceLoader;
    @Autowired
    DataSource dataSource;
    @Autowired(required = false)
    List<LiquibaseChangePostProcessor> processors;
    @Autowired
    LiquibaseProperties properties;

    @Test
    public void setupTest() throws LiquibaseException {
        ChangeAwareSpringLiquibase liquibase = new ChangeAwareSpringLiquibase(properties, dataSource);
        if (processors != null) {
            liquibase.setProcessors(processors);
        }
        liquibase.setResourceLoader(resourceLoader);
        liquibase.afterPropertiesSet();
    }

    @Configuration
    public static class Config {
        @Bean
        public Validator validator() {
            return Validation.buildDefaultValidatorFactory().getValidator();
        }
    }
}
