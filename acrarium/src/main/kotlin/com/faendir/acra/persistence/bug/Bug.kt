/*
 * (C) Copyright 2022-2025 Lukas Morawietz (https://github.com/F43nd1r)
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
import java.beans.ConstructorProperties
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

interface BugVersionInfo {
    val id: BugId
    val latestVersionKey: VersionKey
    val solvedVersionKey: VersionKey?
}

data class Bug(
    override val id: BugId,
    val title: String,
    @get:JvmName("getAppId")
    val appId: AppId,
    val reportCount: Int,
    val latestReport: Instant?,
    override val solvedVersionKey: VersionKey?,
    override val latestVersionKey: VersionKey,
    val affectedInstallations: Int,
) : BugVersionInfo {

    @ConstructorProperties
    constructor(
        id: BugId,
        title: String,
        appId: AppId,
        reportCount: Int,
        latestReport: Instant?,
        solvedVersionCode: Int?,
        solvedVersionFlavor: String?,
        latestVersionCode: Int,
        latestVersionFlavor: String,
        affectedInstallations: Int,
    ) : this(
        id = id,
        title = title,
        appId = appId,
        reportCount = reportCount,
        latestReport = latestReport,
        solvedVersionKey = if (solvedVersionCode != null && solvedVersionFlavor != null) VersionKey(solvedVersionCode, solvedVersionFlavor) else null,
        latestVersionKey = VersionKey(latestVersionCode, latestVersionFlavor),
        affectedInstallations = affectedInstallations
    )
}

data class BugIdentifier(
    @get:JvmName("getAppId")
    val appId: AppId,
    val exceptionClass: String,
    val message: String?,
    val crashLine: String?,
    val cause: String?
) {
    companion object {
        private val codePointRegex = Regex("\\s*at\\s+(<?codepoint>.*)")
        private val causeRegex = Regex("\\s*Caused by:\\s+(<?cause>.*)")
        fun fromStacktrace(
            acrariumConfiguration: AcrariumConfiguration,
            appId: AppId,
            stacktrace: String
        ): BugIdentifier {
            val lines = stacktrace.split('\n')
            return BugIdentifier(
                appId = appId,
                exceptionClass = lines.first()
                    .substringBefore(':')
                    .trim(),
                message = lines.first()
                    .takeIf { it.contains(':') }
                    ?.substringAfter(':')
                    ?.replace(acrariumConfiguration.messageIgnoreRegex, "")
                    ?.take(255)
                    ?.trim(),
                crashLine = lines
                    .asSequence()
                    .mapNotNull { codePointRegex.matchEntire(it) }
                    .mapNotNull { it.groups["codepoint"]?.value }
                    .firstOrNull { !it.startsWith("android.") && !it.startsWith("java.") }?.take(255)
                    ?.trim(),
                cause = lines
                    .asSequence()
                    .mapNotNull { causeRegex.matchEntire(it) }
                    .firstNotNullOfOrNull { it.groups["cause"]?.value }?.take(255)
                    ?.trim()
            )
        }
    }
}

data class BugStats(
    override val id: BugId,
    val title: String,
    val reportCount: Int,
    override val latestVersionKey: VersionKey,
    val latestReport: Instant,
    override val solvedVersionKey: VersionKey?,
    val affectedInstallations: Int,
) : BugVersionInfo {
    @ConstructorProperties
    constructor(
        id: BugId,
        title: String,
        reportCount: Int,
        latestVersionCode: Int,
        latestVersionFlavor: String,
        latestReport: Instant,
        solvedVersionCode: Int?,
        solvedVersionFlavor: String?,
        affectedInstallations: Int,
    ) : this(
        id = id,
        title = title,
        reportCount = reportCount,
        latestVersionKey = VersionKey(latestVersionCode, latestVersionFlavor),
        latestReport = latestReport,
        solvedVersionKey = if (solvedVersionCode != null && solvedVersionFlavor != null) VersionKey(solvedVersionCode, solvedVersionFlavor) else null,
        affectedInstallations = affectedInstallations
    )

    sealed class Filter(override val condition: Condition) : FilterDefinition {
        class TITLE(contains: String) : Filter(BUG.TITLE.contains(contains))
        class LATEST_VERSION(code: Int, flavor: String) :
            Filter(BUG.LATEST_VERSION_CODE.eq(code).and(BUG.LATEST_VERSION_FLAVOR.eq(flavor)))

        object IS_NOT_SOLVED_OR_REGRESSION :
            Filter(BUG.SOLVED_VERSION_CODE.isNull.or(BUG.SOLVED_VERSION_CODE.lt(BUG.LATEST_VERSION_CODE)))
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