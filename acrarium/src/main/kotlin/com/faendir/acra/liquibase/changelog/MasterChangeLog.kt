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

package com.faendir.acra.liquibase.changelog

import com.faendir.acra.liquibase.changelog.v0.m10.V0_10
import com.faendir.acra.liquibase.changelog.v0.m10.m7.V0_10_7
import liquibase.changelog.DatabaseChangeLog
import org.liquibase.kotlin.KotlinDatabaseChangeLogDefinition

class MasterChangeLog : KotlinDatabaseChangeLogDefinition {
    override fun define(): DatabaseChangeLog {
        return changeLog {
            include(V0_10::class.java)
            include(V0_10_7::class.java)
        }
    }
}