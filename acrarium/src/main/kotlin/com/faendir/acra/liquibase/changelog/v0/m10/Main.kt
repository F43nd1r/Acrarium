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

import liquibase.changelog.DatabaseChangeLog
import liquibase.precondition.core.ChangeSetExecutedPrecondition
import liquibase.precondition.core.NotPrecondition
import liquibase.precondition.core.PreconditionContainer
import org.liquibase.kotlin.KotlinDatabaseChangeLog
import org.liquibase.kotlin.KotlinDatabaseChangeLogDefinition

class Main : KotlinDatabaseChangeLogDefinition {
    override fun define(): DatabaseChangeLog = changeLog {
        include(User::class.java)
        include(Roles::class.java)
        include(App::class.java)
        include(Permissions::class.java)
        include(Mail::class.java)
        include(Version::class.java)
        include(Bug::class.java)
        include(Stacktrace::class.java)
        include(StacktraceMatch::class.java)
        include(Report::class.java)
        include(Attachment::class.java)
    }
}

interface SubDefinition : KotlinDatabaseChangeLogDefinition {
    override fun changeLog(closure: KotlinDatabaseChangeLog.() -> Unit): DatabaseChangeLog {
        val changeLog = super.changeLog(closure)
        changeLog.changeSets.forEach {
            if(it.preconditions == null) {
                it.preconditions = PreconditionContainer()
            }
            it.preconditions.onFail = PreconditionContainer.FailOption.MARK_RAN
            it.preconditions.addNestedPrecondition(NotPrecondition().apply {
                addNestedPrecondition(ChangeSetExecutedPrecondition().apply {
                    id = "1527006878002-1"
                    author = "lukas (generated)"
                    changeLogFile = "classpath:/db/changelog/db.changelog-master.yaml"
                })
            })
        }
        return changeLog
    }
}