package com.faendir.acra.liquibase.changelog.v1_0_0

import com.faendir.acra.liquibase.changelog.Author
import com.faendir.acra.liquibase.changelog.ColumnType

databaseChangeLog {
    changeSet("1.0.0-create-stacktrace-match", Author.F43ND1R) {
        val tableName = "stacktrace_match"
        val leftColumn = "left_id"
        val rightColumn = "right_id"
        createTable(tableName) {
            column(name = leftColumn, type = ColumnType.INT) {
                constraints(nullable = false, referencedTableName = "stacktrace", referencedColumnNames = "id", deleteCascade = true, foreignKeyName = "FK_match_left_stacktrace")
            }
            column(name = rightColumn, type = ColumnType.INT) {
                constraints(nullable = false, referencedTableName = "stacktrace", referencedColumnNames = "id", deleteCascade = true, foreignKeyName = "FK_match_tight_stacktrace")
            }
            column(name = "score", type = ColumnType.INT) {
                constraints(nullable = false)
            }
        }
        addUniqueConstraint(tableName, "$leftColumn, $rightColumn", constraintName = "UK_match")
    }
}