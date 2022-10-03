package com.faendir.acra.dataprovider

import com.faendir.acra.model.App
import com.faendir.acra.model.view.VReport
import com.faendir.acra.util.sql
import com.vaadin.flow.data.provider.SortDirection
import java.sql.Timestamp
import java.util.stream.Stream
import javax.persistence.EntityManager

class ReportDataProvider(private val entityManager: EntityManager, private val app: App, private val baseFilter: ReportFilter?) :
    AcrariumDataProvider<VReport, ReportFilter, ReportSort>() {


    override fun fetch(filters: Set<ReportFilter>, sort: List<AcrariumSort<ReportSort>>, offset: Int, limit: Int): Stream<VReport> {
        val (from, where, parameters) = fromFilteredReports(filters)
        val orderBy = if (sort.isEmpty()) "" else sort.joinToString(prefix = "ORDER BY ") { "${it.sort.sql} ${if (it.direction == SortDirection.ASCENDING) "ASC" else "DESC"}" }
        val select = sql(
            """
            |SELECT 
            |`report`.`id` as `report_id`,
            |`report`.`date` as `report_date`,
            |`report`.`android_version` as `report_android_version`,
            |`report`.`phone_model` as `report_phone_model`,
            |`report`.`installation_id` as `report_installation_id`,
            |`report`.`is_silent` as `report_is_silent`,
            |`device`.`marketing_name` as `device_marketing_name`,
            |`stacktrace`.`stacktrace` as `stacktrace`,
            |`version`.`name` as `version_name`,
            |`bug`.`id` as `bug_id`,
            |`bug`.`app_id` as `app_id`
            |""".trimMargin()
        ) + (app.configuration.customReportColumns.takeIf { it.isNotEmpty() }
            ?.mapIndexed { index, customColumn -> sql("COALESCE(JSON_UNQUOTE(JSON_EXTRACT(content, \$.$customColumn)), '') as `report_custom_column$index`") }
            ?.joinToString(prefix = ", ") ?: "")
        parameters["offset"] = offset
        parameters["limit"] = limit
        val query = entityManager.createNativeQuery("$select $from $where $orderBy LIMIT :offset, :limit")
        parameters.forEach { query.setParameter(it.key, it.value) }
        @Suppress("UNCHECKED_CAST")
        return (query.resultList as List<Array<Any?>>).map {
            val reportId = it[0]
            val reportDate = it[1]
            val androidVersion = it[2]
            val phoneModel = it[3]
            val installationId = it[4]
            val isSilent = it[5]
            val marketingName = it[6]
            val stacktrace = it[7]
            val versionName = it[8]
            val bugId = it[9]
            val appId = it[10]
            val customColumns = List(app.configuration.customReportColumns.size) { index -> it[11 + index] }
            VReport(
                reportId as String,
                (reportDate as Timestamp).toLocalDateTime(),
                androidVersion as String,
                phoneModel as String,
                installationId as String,
                isSilent as Boolean,
                marketingName as? String,
                customColumns.map { value -> value as String },
                stacktrace as String,
                versionName as String,
                (bugId as Number).toInt(),
                (appId as Number).toInt(),
            )
        }.stream()
    }

    override fun size(filters: Set<ReportFilter>): Int {
        val (from, where, parameters) = fromFilteredReports(filters)
        val query = entityManager.createNativeQuery("SELECT COUNT(*) $from $where")
        parameters.forEach { query.setParameter(it.key, it.value) }
        return (query.singleResult as Number).toInt()
    }

    private fun fromFilteredReports(filters: Set<ReportFilter>): FilteredReports {
        val parameters = mutableMapOf<String, Any>()
        val from = sql(
            """
            |FROM `report` 
            |STRAIGHT_JOIN `stacktrace` ON `report`.`stacktrace_id` = `stacktrace`.`id`
            |INNER JOIN `bug` ON `stacktrace`.`bug_id` = `bug`.`id`
            |INNER JOIN `version` ON `stacktrace`.`version_id` = `version`.`id`
            |LEFT OUTER JOIN `device` ON (`report`.`phone_model` = `device`.`model` and `report`.`device` = `device`.`device`) 
            |""".trimMargin()
        )
        var where = sql("WHERE `bug`.`app_id` = :appId")
        parameters["appId"] = app.id
        (listOfNotNull(baseFilter) + filters).forEachIndexed { index, filter ->
            val name = "filter$index"
            when (filter) {
                is ReportFilter.AndroidVersion -> {
                    where += sql(" AND `report`.`android_version` LIKE :$name")
                    parameters[name] = "%${filter.contains}%"
                }

                is ReportFilter.InstallationId -> {
                    where += sql(" AND `report`.`installation_id` LIKE :$name")
                    parameters[name] = "%${filter.contains}%"
                }

                ReportFilter.IsNotSilent -> {
                    where += sql(" AND `report`.`is_silent` = false")
                }

                is ReportFilter.PhoneMarketingNameOrModel -> {
                    where += sql(" AND COALESCE(`device`.`marketing_name`, `report`.`phone_model`) LIKE :$name")
                    parameters[name] = "%${filter.contains}%"
                }

                is ReportFilter.Stacktrace -> {
                    where += sql(" AND `stacktrace`.`stacktrace` LIKE :$name")
                    parameters[name] = "%${filter.contains}%"
                }

                is ReportFilter.VersionName -> {
                    where += sql(" AND `version`.`name` LIKE :$name")
                    parameters[name] = "%${filter.contains}%"
                }

                is ReportFilter.Bug -> {
                    where += sql(" AND `bug`.`id` = :$name")
                    parameters[name] = filter.id
                }
            }
        }
        return FilteredReports(from, where, parameters)
    }
}


private data class FilteredReports(val from: String, val where: String, val parameters: MutableMap<String, Any>)

sealed class ReportFilter {
    class Bug(val id: Int) : ReportFilter()
    class InstallationId(val contains: String) : ReportFilter()
    class VersionName(val contains: String) : ReportFilter()
    class AndroidVersion(val contains: String) : ReportFilter()
    class PhoneMarketingNameOrModel(val contains: String) : ReportFilter()
    class Stacktrace(val contains: String) : ReportFilter()
    object IsNotSilent : ReportFilter()
}

enum class ReportSort(val sql: String) {
    InstallationId("`report`.`installation_id`"),
    Date("`report`.`date`"),
    AndroidVersion("`report`.`android_version`"),
    IsSilent("`report`.`is_silent`")
}