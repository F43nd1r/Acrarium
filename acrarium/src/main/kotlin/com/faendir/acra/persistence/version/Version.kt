package com.faendir.acra.persistence.version

import com.faendir.acra.jooq.generated.embeddables.interfaces.IVersionKey
import com.faendir.acra.jooq.generated.embeddables.records.VersionKeyRecord
import com.faendir.acra.jooq.generated.tables.references.VERSION
import com.faendir.acra.persistence.SortDefinition
import com.faendir.acra.persistence.app.AppId
import org.jooq.Field
import org.jooq.impl.AbstractConverter
import org.springframework.stereotype.Component

data class Version(
    val code: Int,
    val name: String,
    val appId: AppId,
    val mappings: String? = null,
    val flavor: String,
) {
    enum class Sort(override val field: Field<*>) : SortDefinition {
        CODE(VERSION.CODE),
        NAME(VERSION.NAME),
        MAPPINGS(VERSION.MAPPINGS)
    }
}

data class VersionKey(
    override val code: Int,
    override val flavor: String,
) : IVersionKey

@Component
class VersionKeyConverter : AbstractConverter<VersionKeyRecord, VersionKey>(VersionKeyRecord::class.java, VersionKey::class.java) {
    override fun from(databaseObject: VersionKeyRecord?): VersionKey? =
        if (databaseObject?.code != null && databaseObject.flavor != null) VersionKey(databaseObject.code!!, databaseObject.flavor!!) else null

    override fun to(userObject: VersionKey?): VersionKeyRecord? = userObject?.let { VersionKeyRecord(it.code, it.flavor) }
}

data class VersionName(
    val code: Int,
    val flavor: String,
    val name: String
)

fun Version.toVersionKey() = VersionKey(code, flavor)
fun VersionName.toVersionKey() = VersionKey(code, flavor)
