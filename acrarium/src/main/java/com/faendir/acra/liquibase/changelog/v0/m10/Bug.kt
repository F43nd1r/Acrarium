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

class Bug : SubDefinition {
    override fun define(): DatabaseChangeLog = changeLog {
        changeSet("0.10-create-bug", AUTHOR.F43ND1R) {
            createTable("bug") {
                column(name = "id", type = ColumnType.INT, autoIncrement = true) {
                    constraints(nullable = false, primaryKey = true, primaryKeyName = "PK_bug")
                }
                column(name = "title", type = ColumnType.TEXT) {
                    constraints(nullable = false)
                }
                column(name = "app_id", type = ColumnType.INT) {
                    constraints(nullable = false, referencedTableName = "app", referencedColumnNames = "id", deleteCascade = true, foreignKeyName = "FK_bug_app")
                }
                column(name = "solved_version", type = ColumnType.INT) {
                    constraints(nullable = true, referencedTableName = "version", referencedColumnNames = "id", foreignKeyName = "FK_bug_solved_version")
                }
            }
        }
    }
}
