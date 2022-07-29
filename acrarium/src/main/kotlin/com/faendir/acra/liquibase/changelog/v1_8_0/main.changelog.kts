package com.faendir.acra.liquibase.changelog.v1_8_0

import com.faendir.acra.liquibase.changelog.Author
import com.faendir.acra.liquibase.changelog.Table

databaseChangeLog {
    changeSet("1.8.0-bug-solved-version-on-delete-set-null", Author.F43ND1R) {
        dropForeignKeyConstraint(Table.BUG, "FK_bug_solved_version")
        addForeignKeyConstraint("FK_bug_solved_version2", Table.BUG, "solved_version", Table.VERSION, "id", onDelete = "SET NULL")
    }
}