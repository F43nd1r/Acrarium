package com.faendir.acra.liquibase.changelog.v1_3_0

import org.liquibase.kotlin.databaseChangeLog

databaseChangeLog {
    include("stacktrace.changelog.kts", true)
    include("report.changelog.kts", true)
}