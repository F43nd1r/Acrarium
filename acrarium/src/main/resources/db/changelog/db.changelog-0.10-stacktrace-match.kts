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
import liquibase.precondition.core.PreconditionContainer
import org.liquibase.kotlin.databaseChangeLog

databaseChangeLog {
    changeSet("0.10-create-stacktrace-match", "f43nd1r") {
        preConditions(onFail = PreconditionContainer.FailOption.MARK_RAN) {
            not {
                changeSetExecuted("1527006878002-1", "lukas (generated)", "classpath:/db/changelog/db.changelog-master.yaml")
            }
        }
        val tableName = "stacktrace_match"
        val leftColumn = "left_id"
        val rightColumn = "right_id"
        createTable(tableName) {
            column(name = leftColumn, type = "INT") {
                constraints(nullable = false, referencedTableName = "stacktrace", referencedColumnNames = "id", deleteCascade = true, foreignKeyName = "FK_match_left_stacktrace")
            }
            column(name = rightColumn, type = "INT") {
                constraints(nullable = false, referencedTableName = "stacktrace", referencedColumnNames = "id", deleteCascade = true, foreignKeyName = "FK_match_tight_stacktrace")
            }
            column(name = "score", type = "INT") {
                constraints(nullable = false)
            }
        }
        addUniqueConstraint(tableName, "$leftColumn, $rightColumn", constraintName = "UK_match")
    }
}