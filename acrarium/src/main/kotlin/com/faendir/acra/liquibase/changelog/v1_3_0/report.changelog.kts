package com.faendir.acra.liquibase.changelog.v1_3_0

import com.faendir.acra.liquibase.changelog.Author
import com.faendir.acra.liquibase.changelog.ColumnType
import com.faendir.acra.liquibase.changelog.Table

databaseChangeLog {
    changeSet("1.3.0-add-silent", Author.F43ND1R) {
        addColumn(Table.REPORT) {
            column(name = "is_silent", type = ColumnType.BOOLEAN, valueComputed = "`content` LIKE '%\\\"IS_SILENT\\\":true%'") {
                constraints(nullable = false)
            }
        }
    }
}