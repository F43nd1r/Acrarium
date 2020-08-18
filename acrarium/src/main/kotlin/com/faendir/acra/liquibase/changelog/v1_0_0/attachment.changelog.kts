package com.faendir.acra.liquibase.changelog.v1_0_0

import com.faendir.acra.liquibase.changelog.Author
import com.faendir.acra.liquibase.changelog.ColumnType

databaseChangeLog {
    changeSet("1.0.0-create-attachment", Author.F43ND1R) {
        val tableName = "attachment"
        val nameColumn = "filename"
        val reportColumn = "report_id"
        createTable(tableName) {
            column(name = nameColumn, type = ColumnType.STRING) {
                constraints(nullable = false)
            }
            column(name = reportColumn, type = ColumnType.STRING) {
                constraints(nullable = false, referencedTableName = "report", referencedColumnNames = "id", deleteCascade = true, foreignKeyName = "FK_attachment_report")
            }
            column(name = "content", type = ColumnType.BLOB) {
                constraints(nullable = false)
            }
        }
        addPrimaryKey(tableName, "$nameColumn, $reportColumn", "PK_attachment")
    }
}