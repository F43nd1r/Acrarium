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

class Attachment : SubDefinition {
    override fun define(): DatabaseChangeLog {
        return changeLog {
            changeSet("0.10-create-attachment", AUTHOR.F43ND1R) {
                val tableName = "attachment"
                val nameColumn = "filename"
                val reportColumn = "report_id"
                createTable(tableName) {
                    column(name = nameColumn, type = ColumnType.STRING) {
                        constraints(nullable = false)
                    }
                    column(name = reportColumn, type = ColumnType.STRING) {
                        constraints(nullable = false, referencedTableName = "report", referencedColumnNames = "id", deleteCascade = true, foreignKeyName = "FK_attachment_report")
                    }
                    column(name = "conent", type = ColumnType.BLOB) {
                        constraints(nullable = false)
                    }
                }
                addPrimaryKey(tableName, "$nameColumn, $reportColumn", "PK_attachment")
            }
        }
    }
}