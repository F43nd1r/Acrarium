package com.faendir.acra.liquibase.changelog.v1_0_0

import com.faendir.acra.liquibase.changelog.Author
import com.faendir.acra.liquibase.changelog.ColumnType

databaseChangeLog {
    changeSet("1.0.0-create-report", Author.F43ND1R) {
        createTable("report") {
            column(name = "id", type = ColumnType.STRING) {
                constraints(nullable = false, primaryKey = true, primaryKeyName = "PK_report")
            }
            column(name = "android_version", type = ColumnType.STRING) {
                constraints(nullable = true)
            }
            column(name = "content", type = ColumnType.TEXT) {
                constraints(nullable = false)
            }
            column(name = "date", type = ColumnType.DATETIME) {
                constraints(nullable = false)
            }
            column(name = "phone_model", type = ColumnType.STRING) {
                constraints(nullable = true)
            }
            column(name = "user_comment", type = ColumnType.TEXT) {
                constraints(nullable = true)
            }
            column(name = "user_email", type = ColumnType.STRING) {
                constraints(nullable = true)
            }
            column(name = "brand", type = ColumnType.STRING) {
                constraints(nullable = true)
            }
            column(name = "installation_id", type = ColumnType.STRING) {
                constraints(nullable = false)
            }
            column(name = "stacktrace_id", type = ColumnType.INT) {
                constraints(nullable = false, referencedTableName = "stacktrace", referencedColumnNames = "id", deleteCascade = true, foreignKeyName = "FK_report_stacktrace")
            }
        }
    }
}