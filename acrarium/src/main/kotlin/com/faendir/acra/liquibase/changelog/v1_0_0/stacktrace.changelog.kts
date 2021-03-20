package com.faendir.acra.liquibase.changelog.v1_0_0

import com.faendir.acra.liquibase.changelog.Author
import com.faendir.acra.liquibase.changelog.ColumnType
import com.faendir.acra.liquibase.changelog.Table

databaseChangeLog {
    changeSet("1.0.0-create-stacktrace", Author.F43ND1R) {
        createTable(Table.STACKTRACE) {
            column(name = "id", type = ColumnType.INT, autoIncrement = true) {
                constraints(nullable = false, primaryKey = true, primaryKeyName = "PK_stacktrace")
            }
            column(name = "bug_id", type = ColumnType.INT) {
                constraints(nullable = false, referencedTableName = "bug", referencedColumnNames = "id", deleteCascade = true, foreignKeyName = "FK_stacktrace_bug")
            }
            column(name = "stacktrace", type = ColumnType.TEXT) {
                constraints(nullable = false)
            }
            column(name = "version_id", type = ColumnType.INT) {
                constraints(nullable = false, referencedTableName = "version", referencedColumnNames = "id", deleteCascade = true, foreignKeyName = "FK_stacktrace_version")
            }
        }
    }
}