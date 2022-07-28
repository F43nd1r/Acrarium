package com.faendir.acra.annotation

import ch.vorburger.mariadb4j.springboot.autoconfigure.MariaDB4jSpringConfiguration
import com.faendir.acra.MariaDBTestConfiguration
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.boot.autoconfigure.EnableAutoConfiguration
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration
import org.springframework.context.annotation.Import
import org.springframework.test.annotation.DirtiesContext
import org.springframework.test.context.junit.jupiter.SpringExtension
import java.lang.annotation.Inherited

@Target(AnnotationTarget.CLASS)
@Retention(AnnotationRetention.RUNTIME)
@Inherited
@ExtendWith(SpringExtension::class)
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
@Import(MariaDBTestConfiguration::class)
@EnableAutoConfiguration(
    exclude = [MariaDB4jSpringConfiguration::class, DataSourceAutoConfiguration::class,
        ch.vorburger.mariadb4j.springboot.autoconfigure.DataSourceAutoConfiguration::class]
)
annotation class AcrariumTest
