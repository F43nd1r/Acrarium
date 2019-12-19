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
    changeSet("0.10-create-user", "f43nd1r") {
        preConditions(onFail = PreconditionContainer.FailOption.MARK_RAN) {
            not {
                changeSetExecuted("1527006878002-1", "lukas (generated)", "classpath:/db/changelog/db.changelog-master.yaml")
            }
        }
        createTable("user") {
            column(name = "username", type = "VARCHAR(255)") {
                constraints(nullable = false, primaryKey = true, primaryKeyName = "PK_user")
            }
            column(name = "password", type = "VARCHAR(255)") {
                constraints(nullable = false)
            }
            column(name = "mail", type = "VARCHAR(255)") {
                constraints(nullable = true)
            }
        }
    }
}
