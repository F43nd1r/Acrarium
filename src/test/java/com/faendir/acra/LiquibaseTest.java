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

import com.faendir.acra.liquibase.ChangeAwareSpringLiquibase;
import com.faendir.acra.liquibase.LiquibaseChangePostProcessor;
import liquibase.exception.LiquibaseException;
import liquibase.integration.spring.SpringLiquibase;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.ImportAutoConfiguration;
import org.springframework.boot.autoconfigure.jdbc.EmbeddedDataSourceConfiguration;
import org.springframework.boot.autoconfigure.liquibase.LiquibaseAutoConfiguration;
import org.springframework.boot.autoconfigure.liquibase.LiquibaseProperties;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.FilterType;
import org.springframework.core.io.ResourceLoader;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.junit4.SpringRunner;

import javax.sql.DataSource;
import java.util.List;

/**
 * @author lukas
 * @since 25.06.18
 */
@DataJpaTest
@RunWith(SpringRunner.class)
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@ComponentScan(basePackageClasses = BackendApplication.class, excludeFilters = @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, value = SpringLiquibase.class))
@ImportAutoConfiguration(exclude = {LiquibaseAutoConfiguration.class, EmbeddedDataSourceConfiguration.class})
public abstract class LiquibaseTest {
    @Autowired ResourceLoader resourceLoader;
    @Autowired DataSource dataSource;
    @Autowired List<LiquibaseChangePostProcessor> processors;

    @Test
    public void setupTest() throws LiquibaseException {
        SpringLiquibase liquibase = new ChangeAwareSpringLiquibase(new LiquibaseProperties(), dataSource, processors);
        liquibase.setResourceLoader(resourceLoader);
        liquibase.afterPropertiesSet();
    }

    @Configuration
    @ComponentScan("com.faendir.acra.liquibase.change")
    public static class Config {
    }
}
