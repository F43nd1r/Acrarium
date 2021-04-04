package com.faendir.acra.liquibase.changelog.v1_0_0

import com.faendir.acra.liquibase.changelog.Author
import com.faendir.acra.liquibase.changelog.ColumnType
import com.faendir.acra.liquibase.changelog.Table

databaseChangeLog {
    changeSet("1.0.0-create-app", Author.F43ND1R) {
        createTable(Table.APP) {
            column(name = "id", type = ColumnType.INT, autoIncrement = true) {
                constraints(nullable = false, primaryKey = true, primaryKeyName = "PK_app")
            }
            column(name = "name", type = ColumnType.STRING) {
                constraints(nullable = false)
            }
            column(name = "reporter_username", type = ColumnType.STRING) {
                constraints(nullable = false, unique = true, uniqueConstraintName = "UK_app_reporter",
                        referencedTableName = "user", referencedColumnNames = "username", deleteCascade = true, foreignKeyName = "FK_app_reporter")
            }
            column(name = "min_score", type = ColumnType.INT, defaultValueNumeric = 95) {
                constraints(nullable = false)
            }
        }
    }
}