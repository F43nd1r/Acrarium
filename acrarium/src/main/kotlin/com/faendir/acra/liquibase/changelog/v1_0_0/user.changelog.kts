package com.faendir.acra.liquibase.changelog.v1_0_0

import com.faendir.acra.liquibase.changelog.Author
import com.faendir.acra.liquibase.changelog.ColumnType

databaseChangeLog {
    changeSet("1.0.0-create-user", Author.F43ND1R) {
        createTable("user") {
            column(name = "username", type = ColumnType.STRING) {
                constraints(nullable = false, primaryKey = true, primaryKeyName = "PK_user")
            }
            column(name = "password", type = ColumnType.STRING) {
                constraints(nullable = false)
            }
            column(name = "mail", type = ColumnType.STRING) {
                constraints(nullable = true)
            }
        }
    }
}