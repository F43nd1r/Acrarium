package com.faendir.acra.liquibase.changelog.v1_0_0

import com.faendir.acra.liquibase.changelog.Author
import com.faendir.acra.liquibase.changelog.ColumnType
import com.faendir.acra.liquibase.changelog.Table

databaseChangeLog {
    changeSet("1.0.0-create-permissions", Author.F43ND1R) {
        val tableName = "user_permissions"
        val userColumn = "user_username"
        val appColumn = "app_id"
        createTable(tableName) {
            column(name = userColumn, type = ColumnType.STRING) {
                constraints(nullable = false, referencedTableName = "user", referencedColumnNames = "username", deleteCascade = true, foreignKeyName = "FK_permissions_username")
            }
            column(name = appColumn, type = ColumnType.INT) {
                constraints(nullable = false, referencedTableName = Table.APP, referencedColumnNames = "id", deleteCascade = true, foreignKeyName = "FK_permissions_app")
            }
            column(name = "level", type = ColumnType.INT) {
                constraints(nullable = false)
            }
        }
        addPrimaryKey(tableName, "$userColumn, $appColumn", "PK_user_permissions")
    }
}