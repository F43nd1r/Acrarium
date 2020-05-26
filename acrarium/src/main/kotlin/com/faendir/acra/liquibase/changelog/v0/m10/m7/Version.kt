package com.faendir.acra.liquibase.changelog.v0.m10.m7

import com.faendir.acra.liquibase.changelog.AUTHOR
import com.faendir.acra.liquibase.changelog.ColumnType
import liquibase.changelog.DatabaseChangeLog
import org.liquibase.kotlin.KotlinDatabaseChangeLogDefinition

class Version : KotlinDatabaseChangeLogDefinition {
    override fun define(): DatabaseChangeLog {
        return changeLog {
            changeSet("0.10.7-nullable-mappings", AUTHOR.F43ND1R) {
                dropNotNullConstraint(tableName = "version", columnName = "mappings", columnDataType = ColumnType.TEXT)
            }
        }
    }
}