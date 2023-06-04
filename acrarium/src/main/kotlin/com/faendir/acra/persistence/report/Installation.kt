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
package com.faendir.acra.persistence.report

import com.faendir.acra.jooq.generated.tables.references.REPORT
import com.faendir.acra.persistence.FilterDefinition
import com.faendir.acra.persistence.SortDefinition
import org.jooq.Condition
import org.jooq.Field
import org.jooq.impl.DSL
import java.time.Instant

data class Installation(val id: String, val reportCount: Int, val latestReport: Instant) {

    sealed class Filter(override val condition: Condition) : FilterDefinition {
        class ID(contains: String) : Filter(REPORT.INSTALLATION_ID.contains(contains))
    }

    enum class Sort(override val field: Field<*>) : SortDefinition {
        ID(REPORT.INSTALLATION_ID),
        REPORT_COUNT(DSL.count(REPORT.ID)),
        LATEST_REPORT(DSL.max(REPORT.DATE)),
    }
}
