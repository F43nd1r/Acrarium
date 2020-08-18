package com.faendir.acra.liquibase.changelog.v1_0_0

databaseChangeLog {
    include("user.changelog.kts", true)
    include("roles.changelog.kts", true)
    include("app.changelog.kts", true)
    include("permissions.changelog.kts", true)
    include("mail.changelog.kts", true)
    include("version.changelog.kts", true)
    include("bug.changelog.kts", true)
    include("stacktrace.changelog.kts", true)
    include("stacktrace_match.changelog.kts", true)
    include("report.changelog.kts", true)
    include("attachment.changelog.kts", true)
}