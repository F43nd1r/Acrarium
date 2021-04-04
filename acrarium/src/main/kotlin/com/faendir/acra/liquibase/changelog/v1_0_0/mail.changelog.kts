package com.faendir.acra.liquibase.changelog.v1_0_0

import com.faendir.acra.liquibase.changelog.Author
import com.faendir.acra.liquibase.changelog.ColumnType
import com.faendir.acra.liquibase.changelog.Table

databaseChangeLog {
    changeSet("1.0.0-create-mail", Author.F43ND1R) {
        val tableName = "mail_settings"
        val appColumn = "app_id"
        val userColumn = "username"
        createTable(tableName) {
            column(name = appColumn, type = ColumnType.INT) {
                constraints(nullable = false, referencedTableName = Table.APP, referencedColumnNames = "id", deleteCascade = true, foreignKeyName = "FK_mail_app")
            }
            column(name = userColumn, type = ColumnType.STRING) {
                constraints(nullable = false, referencedTableName = "user", referencedColumnNames = "username", deleteCascade = true, foreignKeyName = "FK_mail_username")
            }
            column(name = "new_bug", type = ColumnType.BOOLEAN) {
                constraints(nullable = false)
            }
            column(name = "regression", type = ColumnType.BOOLEAN) {
                constraints(nullable = false)
            }
            column(name = "spike", type = ColumnType.BOOLEAN) {
                constraints(nullable = false)
            }
            column(name = "summary", type = ColumnType.BOOLEAN) {
                constraints(nullable = false)
            }
        }
        addPrimaryKey(tableName, "$appColumn, $userColumn", "PK_mail")
    }
}