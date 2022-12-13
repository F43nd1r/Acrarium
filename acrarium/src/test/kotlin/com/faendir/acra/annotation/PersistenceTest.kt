package com.faendir.acra.annotation

import com.faendir.acra.MariaDBTestConfiguration
import com.faendir.acra.persistence.PersistenceTestConfiguration
import org.springframework.boot.test.autoconfigure.jooq.JooqTest
import org.springframework.context.annotation.Import
import java.lang.annotation.Inherited

@Target(AnnotationTarget.CLASS)
@Retention(AnnotationRetention.RUNTIME)
@Inherited
@JooqTest
@Import(MariaDBTestConfiguration::class, PersistenceTestConfiguration::class)
annotation class PersistenceTest