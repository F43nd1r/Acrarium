/*
 * (C) Copyright 2022-2023 Lukas Morawietz (https://github.com/F43nd1r)
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

import com.faendir.acra.jooq.generated.indexes.*
import com.faendir.acra.jooq.generated.tables.interfaces.IReport
import com.faendir.acra.jooq.generated.tables.references.REPORT
import com.faendir.acra.persistence.FilterDefinition
import com.faendir.acra.persistence.SortDefinition
import com.faendir.acra.persistence.app.AppId
import com.faendir.acra.persistence.bug.BugId
import com.faendir.acra.persistence.version.VersionKey
import org.jooq.Condition
import org.jooq.Field
import org.jooq.Index
import org.jooq.JSON
import org.jooq.impl.DSL
import java.time.Instant

data class Report(
    override val id: String,
    override val androidVersion: String?,
    override val content: JSON,
    override val date: Instant,
    override val phoneModel: String?,
    override val userComment: String?,
    override val userEmail: String?,
    override val brand: String?,
    override val installationId: String,
    override val isSilent: Boolean,
    override val device: String,
    override val marketingDevice: String,
    override val bugId: BugId,
    @get:JvmName("getAppId")
    override val appId: AppId,
    override val stacktrace: String,
    override val exceptionClass: String,
    override val message: String?,
    override val crashLine: String?,
    override val cause: String?,
    override val versionCode: Int,
    override val versionFlavor: String,
) : IReport {
    val versionKey: VersionKey = VersionKey(versionCode, versionFlavor)
}

data class ReportRow(
    val id: String,
    val androidVersion: String?,
    val date: Instant,
    val phoneModel: String?,
    val marketingDevice: String,
    val installationId: String,
    val isSilent: Boolean,
    val exceptionClass: String,
    val message: String?,
    val versionCode: Int,
    val versionFlavor: String,
    val bugId: BugId,
    val customColumns: List<String?>,
) {
    val versionKey: VersionKey = VersionKey(versionCode, versionFlavor)

    sealed class Filter(override val condition: Condition) : FilterDefinition {
        class BUG(id: BugId) : Filter(REPORT.BUG_ID.eq(id))
        class INSTALLATION_ID(contains: String) : Filter(REPORT.INSTALLATION_ID.contains(contains))
        class VERSION(code: Int, flavor: String) : Filter(REPORT.VERSION_CODE.eq(code).and(REPORT.VERSION_FLAVOR.eq(flavor)))
        class ANDROID_VERSION(contains: String) : Filter(REPORT.ANDROID_VERSION.contains(contains))
        class MARKETING_DEVICE(contains: String) : Filter(REPORT.MARKETING_DEVICE.contains(contains))
        class EXCEPTION_CLASS(contains: String) : Filter(REPORT.EXCEPTION_CLASS.contains(contains))
        class MESSAGE(contains: String) : Filter(REPORT.MESSAGE.contains(contains))
        object IS_NOT_SILENT : Filter(REPORT.IS_SILENT.eq(false))
        class CUSTOM_COLUMN(path: String, contains: String) : Filter(DSL.jsonValue(REPORT.CONTENT, path).cast(String::class.java).contains(contains))
    }

    sealed class Sort(override val field: Field<*>, val index: Index?) : SortDefinition {
        object INSTALLATION_ID : Sort(REPORT.INSTALLATION_ID, REPORT_IDX_REPORT_INSTALLATION_ID)
        object DATE : Sort(REPORT.DATE, REPORT_IDX_REPORT_DATE)
        object ANDROID_VERSION : Sort(REPORT.ANDROID_VERSION, REPORT_IDX_REPORT_ANDROID_VERSION)
        object IS_SILENT : Sort(REPORT.IS_SILENT, REPORT_IDX_REPORT_IS_SILENT)
        object MARKETING_DEVICE : Sort(REPORT.MARKETING_DEVICE, REPORT_IDX_REPORT_MARKETING_DEVICE)
        object VERSION_CODE : Sort(REPORT.VERSION_CODE, REPORT_IDX_REPORT_VERSION_CODE)
        class CUSTOM_COLUMN(path: String) : Sort(DSL.jsonValue(REPORT.CONTENT, path), null)
    }
}