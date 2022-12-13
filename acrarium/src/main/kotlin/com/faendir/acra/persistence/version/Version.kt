package com.faendir.acra.persistence.version

import com.faendir.acra.jooq.generated.Tables.VERSION
import com.faendir.acra.persistence.SortDefinition
import com.faendir.acra.persistence.app.AppId
import org.jooq.Field

data class Version(
    val code: Int,
    val name: String,
    val appId: AppId,
    val mappings: String? = null,
    val flavor: String,
) {
    enum class Sort(override val field: Field<out Any>) : SortDefinition {
        CODE(VERSION.CODE),
        NAME(VERSION.NAME),
        MAPPINGS(VERSION.MAPPINGS)
    }
}

data class VersionKey(
    val appId: AppId,
    val code: Int,
    val flavor: String,
)

data class VersionName(
    val code: Int,
    val flavor: String,
    val name: String
)

fun Version.toVersionKey() = VersionKey(appId, code, flavor)
