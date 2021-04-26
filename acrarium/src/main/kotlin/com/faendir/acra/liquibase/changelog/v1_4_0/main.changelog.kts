package com.faendir.acra.liquibase.changelog.v1_4_0

import com.faendir.acra.liquibase.changelog.Author

databaseChangeLog {
    changeSet("1.4.0-drop-stacktrace-match", Author.F43ND1R) {
        dropTable("stacktrace_match")
    }
}