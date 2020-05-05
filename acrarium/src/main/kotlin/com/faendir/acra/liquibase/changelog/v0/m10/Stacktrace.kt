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

class Stacktrace : SubDefinition {
    override fun define(): DatabaseChangeLog = changeLog {
        changeSet("0.10-create-stacktrace", AUTHOR.F43ND1R) {
            createTable("stacktrace") {
                column(name = "id", type = ColumnType.INT, autoIncrement = true) {
                    constraints(nullable = false, primaryKey = true, primaryKeyName = "PK_stacktrace")
                }
                column(name = "bug_id", type = ColumnType.INT) {
                    constraints(nullable = false, referencedTableName = "bug", referencedColumnNames = "id", deleteCascade = true, foreignKeyName = "FK_stacktrace_bug")
                }
                column(name = "stacktrace", type = ColumnType.TEXT) {
                    constraints(nullable = false)
                }
                column(name = "version_id", type = ColumnType.INT) {
                    constraints(nullable = false, referencedTableName = "version", referencedColumnNames = "id", deleteCascade = true, foreignKeyName = "FK_stacktrace_version")
                }
            }
        }
    }
}
