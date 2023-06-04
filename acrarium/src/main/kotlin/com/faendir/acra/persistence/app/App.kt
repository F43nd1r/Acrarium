/*
 * (C) Copyright 2022 Lukas Morawietz (https://github.com/F43nd1r)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.faendir.acra.persistence.app

import com.faendir.acra.jooq.generated.tables.references.APP
import com.faendir.acra.jooq.generated.tables.references.REPORT
import com.faendir.acra.persistence.SortDefinition
import org.jooq.Field
import org.jooq.impl.AbstractConverter
import org.jooq.impl.DSL
import org.springframework.stereotype.Component

@JvmInline
value class AppId(val value: Int) {
    override fun toString(): String {
        return value.toString()
    }
}

@Component
class AppIdConverter : AbstractConverter<Int, AppId>(Int::class.javaPrimitiveType, AppId::class.java) {
    override fun from(databaseObject: Int?): AppId? = databaseObject?.let { AppId(it) }

    override fun to(userObject: AppId?): Int? = userObject?.value
}

data class App(val id: AppId, val name: String, val reporterUsername: String)

data class AppStats(val id: AppId, val name: String, val reportCount: Int, val bugCount: Int) {
    enum class Sort(override val field: Field<*>) : SortDefinition {
        NAME(APP.NAME),
        REPORT_COUNT(DSL.count(REPORT.ID)),
        BUG_COUNT(DSL.countDistinct(REPORT.BUG_ID))
    }
}

data class AppName(val id: AppId, val name: String)