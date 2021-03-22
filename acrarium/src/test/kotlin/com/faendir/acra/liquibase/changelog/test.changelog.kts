package com.faendir.acra.liquibase.changelog

databaseChangeLog {
    changeSet("Prepare H2", Author.F43ND1R) {
        sql("CREATE ALIAS SUBSTRING_INDEX FOR \"com.faendir.acra.util.H2Helper.subStringIndex\";")
    }
    include("master.changelog.kts", true)
}