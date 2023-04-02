package com.faendir.acra.annotation

import com.faendir.acra.DatabaseTestConfiguration
import com.faendir.acra.persistence.PersistenceTestConfiguration
import org.springframework.boot.test.autoconfigure.jooq.JooqTest
import org.springframework.context.annotation.Import
import java.lang.annotation.Inherited

@Target(AnnotationTarget.CLASS)
@Retention(AnnotationRetention.RUNTIME)
@Inherited
@JooqTest
@Import(DatabaseTestConfiguration::class, PersistenceTestConfiguration::class)
annotation class PersistenceTest
