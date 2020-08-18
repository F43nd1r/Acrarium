package com.faendir.acra.liquibase.changelog.v1_0_0

import com.faendir.acra.liquibase.changelog.Author
import com.faendir.acra.liquibase.changelog.ColumnType

databaseChangeLog {
    changeSet("1.0.0-create-roles", Author.F43ND1R) {
        createTable("user_roles") {
            column(name = "user_username", type = ColumnType.STRING) {
                constraints(nullable = false, referencedTableName = "user", referencedColumnNames = "username", deleteCascade = true, foreignKeyName = "FK_roles_username")
            }
            column(name = "roles", type = ColumnType.STRING) {
                constraints(nullable = false)
            }
        }
    }
}