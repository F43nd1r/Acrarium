package com.faendir.acra.persistence.report

import com.faendir.acra.jooq.generated.Tables.REPORT
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

    enum class Sort(override val field: Field<out Any>) : SortDefinition {
        ID(REPORT.INSTALLATION_ID),
        REPORT_COUNT(DSL.count(REPORT.ID)),
        LATEST_REPORT(DSL.max(REPORT.DATE)),
    }
}
