package com.faendir.acra.liquibase.changelog.v0.m10.m7

import com.faendir.acra.liquibase.changelog.AUTHOR
import com.faendir.acra.liquibase.changelog.ColumnType
import liquibase.changelog.DatabaseChangeLog
import org.liquibase.kotlin.KotlinDatabaseChangeLogDefinition

class Attachment : KotlinDatabaseChangeLogDefinition {
    override fun define(): DatabaseChangeLog {
        return changeLog {
            changeSet("0.10.7-fix-content-column-name", AUTHOR.F43ND1R) {
                renameColumn(tableName = "attachment", oldColumnName = "conent", newColumnName = "content", columnDataType = ColumnType.BLOB)
            }
        }
    }
}