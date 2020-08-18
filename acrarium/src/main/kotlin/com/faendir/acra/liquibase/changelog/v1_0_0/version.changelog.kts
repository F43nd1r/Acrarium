package com.faendir.acra.liquibase.changelog.v1_0_0

import com.faendir.acra.liquibase.changelog.Author
import com.faendir.acra.liquibase.changelog.ColumnType

databaseChangeLog {
    changeSet("1.0.0-create-version", Author.F43ND1R) {
        val codeColumn = "code"
        val appColumn = "app_id"
        createTable("version") {
            column(name = "id", type = ColumnType.INT, autoIncrement = true) {
                constraints(nullable = false, primaryKey = true, primaryKeyName = "PK_version")
            }
            column(name = codeColumn, type = ColumnType.INT) {
                constraints(nullable = false)
            }
            column(name = "name", type = ColumnType.STRING) {
                constraints(nullable = false)
            }
            column(name = appColumn, type = ColumnType.INT) {
                constraints(nullable = false, referencedTableName = "app", referencedColumnNames = "id", deleteCascade = true, foreignKeyName = "FK_version_app")
            }
            column(name = "mappings", type = ColumnType.TEXT) {
                constraints(nullable = true)
            }
        }
        addUniqueConstraint("version", "$codeColumn, $appColumn", constraintName = "UK_version")
    }
}