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

class Mail : SubDefinition {
    override fun define(): DatabaseChangeLog {
        return changeLog {
            changeSet("0.10-create-mail", AUTHOR.F43ND1R) {
                val tableName = "mail_settings"
                val appColumn = "app_id"
                val userColumn = "username"
                createTable(tableName) {
                    column(name = appColumn, type = ColumnType.INT) {
                        constraints(nullable = false, referencedTableName = "app", referencedColumnNames = "id", deleteCascade = true, foreignKeyName = "FK_mail_app")
                    }
                    column(name = userColumn, type = ColumnType.STRING) {
                        constraints(nullable = false, referencedTableName = "user", referencedColumnNames = "username", deleteCascade = true, foreignKeyName = "FK_mail_username")
                    }
                    column(name = "new_bug", type = ColumnType.BOOLEAN) {
                        constraints(nullable = false)
                    }
                    column(name = "regression", type = ColumnType.BOOLEAN) {
                        constraints(nullable = false)
                    }
                    column(name = "spike", type = ColumnType.BOOLEAN) {
                        constraints(nullable = false)
                    }
                    column(name = "summary", type = ColumnType.BOOLEAN) {
                        constraints(nullable = false)
                    }
                }
                addPrimaryKey(tableName, "$appColumn, $userColumn", "PK_mail")
            }
        }
    }
}
