package com.faendir.acra.liquibase.changelog.v1_9_0

import com.faendir.acra.liquibase.changelog.Author
import com.faendir.acra.liquibase.changelog.Table

databaseChangeLog {
    changeSet("1.9.0-report-indexes", Author.F43ND1R) {
        createIndex(tableName = Table.REPORT, indexName = "IDX_report_date") {
            column("date")
        }
        createIndex(tableName = Table.REPORT, indexName = "IDX_report_installation_id") {
            column("installation_id")
        }
        createIndex(tableName = Table.REPORT, indexName = "IDX_report_android_version") {
            column("android_version")
        }
        createIndex(tableName = Table.REPORT, indexName = "IDX_report_is_silent") {
            column("is_silent")
        }
    }
}