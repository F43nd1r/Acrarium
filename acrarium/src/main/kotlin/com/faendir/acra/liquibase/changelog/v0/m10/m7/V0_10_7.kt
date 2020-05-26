package com.faendir.acra.liquibase.changelog.v0.m10.m7

import org.liquibase.kotlin.KotlinDatabaseChangeLogDefinition

class V0_10_7 : KotlinDatabaseChangeLogDefinition {
    override fun define() = changeLog {
        include(Version::class.java)
        include(Attachment::class.java)
    }
}