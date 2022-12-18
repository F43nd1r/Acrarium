package com.faendir.acra.persistence.bug

import com.faendir.acra.jooq.generated.tables.references.BUG
import com.faendir.acra.persistence.FilterDefinition
import com.faendir.acra.persistence.SortDefinition
import com.faendir.acra.persistence.app.AppId
import com.faendir.acra.persistence.version.VersionKey
import com.faendir.acra.settings.AcrariumConfiguration
import org.jooq.Condition
import org.jooq.Field
import org.jooq.impl.AbstractConverter
import org.springframework.stereotype.Component
import java.time.Instant

@JvmInline
value class BugId(val value: Int) {
    override fun toString(): String {
        return value.toString()
    }
}

@Component
class BugIdConverter : AbstractConverter<Int, BugId>(Int::class.javaPrimitiveType, BugId::class.java) {
    override fun from(databaseObject: Int?): BugId? = databaseObject?.let { BugId(it) }

    override fun to(userObject: BugId?): Int? = userObject?.value
}

data class Bug(
    val id: BugId,
    val title: String,
    val appId: AppId,
    val reportCount: Int,
    val latestReport: Instant?,
    val solvedVersionKey: VersionKey?,
    val latestVersionKey: VersionKey?,
    val affectedInstallations: Int,
)

data class BugIdentifier(
    val appId: AppId,
    val exceptionClass: String,
    val message: String?,
    val crashLine: String?,
    val cause: String?
) {
    companion object {
        private val codePointRegex = Regex("\\s*at\\s+(<?codepoint>.*)")
        private val causeRegex = Regex("\\s*Caused by:\\s+(<?cause>.*)")
        fun fromStacktrace(acrariumConfiguration: AcrariumConfiguration, appId: AppId, stacktrace: String): BugIdentifier {
            val lines = stacktrace.split('\n')
            return BugIdentifier(
                appId = appId,
                exceptionClass = lines.first().substringBefore(':'),
                message = lines.first().takeIf { it.contains(':') }?.substringAfter(':')?.replace(acrariumConfiguration.messageIgnoreRegex, "")
                    ?.take(255),
                crashLine = lines.mapNotNull { codePointRegex.matchEntire(it) }
                    .mapNotNull { it.groups["codepoint"]?.value }
                    .firstOrNull { !it.startsWith("android.") && !it.startsWith("java.") }?.take(255),
                cause = lines.mapNotNull { causeRegex.matchEntire(it) }.firstNotNullOfOrNull { it.groups["cause"]?.value }?.take(255)
            )
        }
    }
}

data class BugStats(
    val id: BugId,
    val title: String,
    val reportCount: Int,
    val latestVersionKey: VersionKey,
    val latestReport: Instant,
    val solvedVersionKey: VersionKey?,
    val affectedInstallations: Int,
) {
    sealed class Filter(override val condition: Condition) : FilterDefinition {
        class TITLE(contains: String) : Filter(BUG.TITLE.contains(contains))
        class LATEST_VERSION(code: Int, flavor: String) : Filter(BUG.LATEST_VERSION_CODE.eq(code).and(BUG.LATEST_VERSION_FLAVOR.eq(flavor)))
        object IS_NOT_SOLVED_OR_REGRESSION : Filter(BUG.SOLVED_VERSION_CODE.isNull.or(BUG.SOLVED_VERSION_CODE.lt(BUG.LATEST_VERSION_CODE)))
    }

    enum class Sort(override val field: Field<*>) : SortDefinition {
        TITLE(BUG.TITLE),
        REPORT_COUNT(BUG.REPORT_COUNT),
        LATEST_VERSION_CODE(BUG.LATEST_VERSION_CODE),
        LATEST_REPORT(BUG.LATEST_REPORT),
        SOLVED_VERSION_CODE(BUG.SOLVED_VERSION_CODE),
        AFFECTED_INSTALLATIONS(BUG.AFFECTED_INSTALLATIONS),
    }
}