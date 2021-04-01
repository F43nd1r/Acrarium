package com.faendir.acra.liquibase.changelog.v1_3_0

import com.faendir.acra.liquibase.changelog.Author
import com.faendir.acra.liquibase.changelog.ColumnType
import com.faendir.acra.liquibase.changelog.Table
import org.liquibase.kotlin.databaseChangeLog

databaseChangeLog {
    changeSet("1.3.0-report-set-json-type", Author.F43ND1R) {
        modifyDataType(Table.REPORT, "content", ColumnType.JSON)
    }
    changeSet("1.3.0-extract-stacktrace-class", Author.F43ND1R) {
        addColumn(Table.STACKTRACE) {
            column(name = "class", type = ColumnType.STRING, valueComputed = "SUBSTRING_INDEX(`stacktrace`,':',1)") {
                constraints(nullable = false)
            }
        }
    }
    changeSet("1.3.0-add-silent", Author.F43ND1R) {
        addColumn(Table.REPORT) {
            column(name = "is_silent", type = ColumnType.BOOLEAN, valueComputed = "JSON_EXTRACT(`content`, '$.IS_SILENT') = true") {
                constraints(nullable = false)
            }
        }
    }
    changeSet("1.3.0-add-device-marketing-name-table", Author.F43ND1R) {
        createTable("device") {
            column(name = "device", type = ColumnType.STRING_CS) {
                constraints(nullable = false, primaryKey = true, primaryKeyName = "PK_device")
            }
            column(name = "model", type = ColumnType.STRING_CS) {
                constraints(nullable = false, primaryKey = true, primaryKeyName = "PK_device")
            }
            column(name = "marketing_name", type = ColumnType.STRING) {
                constraints(nullable = false)
            }
        }
        addColumn(Table.REPORT) {
            column(name = "device", type = ColumnType.STRING, valueComputed = "IFNULL(JSON_UNQUOTE(JSON_EXTRACT(`content`,'$.BUILD.DEVICE')), '')") {
                constraints(nullable = false)
            }
        }
    }
}