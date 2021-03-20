package com.faendir.acra.liquibase.changelog

databaseChangeLog {
    include("v1_0_0/main.changelog.kts", true)
    include("v1_3_0/stacktrace.changelog.kts", true)
}