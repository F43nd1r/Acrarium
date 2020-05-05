/*
 * (C) Copyright 2019 Lukas Morawietz (https://github.com/F43nd1r)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.faendir.acra.liquibase.changelog.v0.m10

import com.faendir.acra.liquibase.changelog.AUTHOR
import com.faendir.acra.liquibase.changelog.ColumnType
import liquibase.changelog.DatabaseChangeLog

class Report : SubDefinition {
    override fun define(): DatabaseChangeLog = changeLog {
        changeSet("0.10-create-report", AUTHOR.F43ND1R) {
            createTable("report") {
                column(name = "id", type = ColumnType.STRING) {
                    constraints(nullable = false, primaryKey = true, primaryKeyName = "PK_report")
                }
                column(name = "android_version", type = ColumnType.STRING) {
                    constraints(nullable = true)
                }
                column(name = "content", type = ColumnType.TEXT) {
                    constraints(nullable = false)
                }
                column(name = "date", type = ColumnType.DATETIME) {
                    constraints(nullable = false)
                }
                column(name = "phone_model", type = ColumnType.STRING) {
                    constraints(nullable = true)
                }
                column(name = "user_comment", type = ColumnType.TEXT) {
                    constraints(nullable = true)
                }
                column(name = "user_email", type = ColumnType.STRING) {
                    constraints(nullable = true)
                }
                column(name = "brand", type = ColumnType.STRING) {
                    constraints(nullable = true)
                }
                column(name = "installation_id", type = ColumnType.STRING) {
                    constraints(nullable = false)
                }
                column(name = "stacktrace_id", type = ColumnType.INT) {
                    constraints(nullable = false, referencedTableName = "stacktrace", referencedColumnNames = "id", deleteCascade = true, foreignKeyName = "FK_report_stacktrace")
                }
            }
        }
    }
}
