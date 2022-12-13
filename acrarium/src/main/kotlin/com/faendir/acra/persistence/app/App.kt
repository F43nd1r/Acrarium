package com.faendir.acra.persistence.app

import com.faendir.acra.jooq.generated.Tables
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
    enum class Sort(override val field: Field<out Any>) : SortDefinition {
        NAME(Tables.APP.NAME),
        REPORT_COUNT(DSL.count(Tables.REPORT.ID)),
        BUG_COUNT(DSL.countDistinct(Tables.REPORT.BUG_ID))
    }
}

data class AppName(val id: AppId, val name: String)