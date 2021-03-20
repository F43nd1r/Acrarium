package com.faendir.acra.liquibase.changelog.v1_3_0

import com.faendir.acra.liquibase.changelog.Author
import com.faendir.acra.liquibase.changelog.ColumnType
import com.faendir.acra.liquibase.changelog.Table

databaseChangeLog {
    changeSet("1.3.0-extract-stacktrace-class", Author.F43ND1R) {
        addColumn(Table.STACKTRACE) {
            column(name = "class", type = ColumnType.STRING, defaultValueComputed = "SUBSTRING_INDEX(`stacktrace`,':',1)") {
                constraints(nullable = true)
            }
        }
    }
}