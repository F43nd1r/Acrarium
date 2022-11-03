package com.faendir.acra.dataprovider

import com.faendir.acra.model.App
import com.faendir.acra.model.Bug
import com.faendir.acra.model.Version
import com.faendir.acra.model.view.VBug
import com.faendir.acra.util.sql
import com.vaadin.flow.data.provider.SortDirection
import java.sql.Timestamp
import java.time.Instant
import java.util.stream.Stream
import javax.persistence.EntityManager

class BugDataProvider(private val entityManager: EntityManager, private val app: App) : AcrariumDataProvider<VBug, BugFilter, BugSort>() {
    override fun fetch(filters: Set<BugFilter>, sort: List<AcrariumSort<BugSort>>, offset: Int, limit: Int): Stream<VBug> {
        val (innerFrom, where, parameters) = fromFilteredBugs(filters)
        val orderBy = if (sort.isEmpty()) "" else sort.joinToString(prefix = "ORDER BY ") { "${it.sort.sql} ${if (it.direction == SortDirection.ASCENDING) "ASC" else "DESC"}" }
        val select = sql(
            """
            |SELECT 
            |`bug`.`id` as `bug_id`,
            |`bug`.`title` as `bug_title`,
            |`solved_version`.`id` as `solved_version_id`,
            |`solved_version`.`code` as `solved_version_code`,
            |`solved_version`.`name` as `solved_version_name`,
            |COUNT(`report`.`id`) as `report_count`,
            |MAX(`report`.`date`) as `max_report_date`,
            |MAX(`version`.`code`) as `max_version_code`,
            |COUNT(DISTINCT `report`.`installation_id`) as `user_count`
            |""".trimMargin()
        )
        val from = sql(
            """
            |$innerFrom 
            |LEFT OUTER JOIN `stacktrace` ON `bug`.`id` = `stacktrace`.`bug_id`
            |LEFT OUTER JOIN `version` ON `stacktrace`.`version_id` = `version`.`id`
            |LEFT OUTER JOIN `report` ON `report`.`stacktrace_id` = `stacktrace`.`id`
            |""".trimMargin()
        )
        parameters["offset"] = offset
        parameters["limit"] = limit
        val query = entityManager.createNativeQuery("$select $from $where GROUP BY `bug`.`id` $orderBy LIMIT :offset, :limit")
        parameters.forEach { query.setParameter(it.key, it.value) }
        @Suppress("UNCHECKED_CAST")
        return (query.resultList as List<Array<Any?>>).map {
            val bugId = it[0]
            val bugTitle = it[1]
            val solvedVersionId = it[2]
            val solvedVersionCode = it[3]
            val solvedVersionName = it[4]
            val reportCount = it[5]
            var maxReportDate = it[6]
            var maxVersionCode = it[7]
            val userCount = it[8]

            if(maxReportDate == null) {
                maxReportDate = Timestamp.from(Instant.MIN);
            }
            if(maxVersionCode == null) {
                maxVersionCode = -1;
            }

            VBug(
                bug = Bug(
                    id = (bugId as Number).toInt(),
                    title = bugTitle as String,
                    app = app,
                    solvedVersion = solvedVersionId?.let { Version((solvedVersionId as Number).toInt(), (solvedVersionCode as Number).toInt(), solvedVersionName as String, app) }
                ),
                lastReport = (maxReportDate as Timestamp).toLocalDateTime(),
                reportCount = (reportCount as Number).toLong(),
                highestVersionCode = (maxVersionCode as Number).toInt(),
                userCount = (userCount as Number).toLong()
            )
        }.stream()
    }

    override fun size(filters: Set<BugFilter>): Int {
        val (from, where, parameters) = fromFilteredBugs(filters)
        val query = entityManager.createNativeQuery("SELECT COUNT(*) $from $where")
        parameters.forEach { query.setParameter(it.key, it.value) }
        return (query.singleResult as Number).toInt()
    }

    private fun fromFilteredBugs(filters: Set<BugFilter>): FilteredBugs {
        val parameters = mutableMapOf<String, Any>()
        var where = sql("WHERE `bug`.`app_id` = :appId")
        parameters["appId"] = app.id
        filters.filterIsInstance<BugFilter.TitleFilter>().forEachIndexed { index, filter ->
            val name = "titleFilter$index"
            where += sql(" AND `bug`.`title` LIKE :$name")
            parameters[name] = "%${filter.contains}%"
        }
        val from: String
        if (filters.contains(BugFilter.NotSolvedOrRegressionFilter)) {
            from = sql(
                """
                    |FROM `bug` 
                    |LEFT OUTER JOIN (
                        |SELECT 
                            |`stacktrace`.`bug_id` AS `bug_id`,
                            |MAX(`version`.`code`) AS `max_version_code` 
                        |FROM `stacktrace` 
                        |INNER JOIN `version` ON `stacktrace`.`version_id` = `version`.`id` 
                        |GROUP BY `stacktrace`.`bug_id`
                    |) `max_version` ON `bug`.`id` = `max_version`.`bug_id`
                    |LEFT OUTER JOIN `version` `solved_version` on `bug`.`solved_version` = `solved_version`.`id`
                        |""".trimMargin()
            )
            where += sql(" AND (`bug`.`solved_version` IS NULL OR `solved_version`.`code` < `max_version`.`max_version_code`)")
        } else {
            from = sql("FROM `bug` LEFT OUTER JOIN `version` `solved_version` on `bug`.`solved_version` = `solved_version`.`id`")
        }
        return FilteredBugs(from, where, parameters)
    }
}

private data class FilteredBugs(val from: String, val where: String, val parameters: MutableMap<String, Any>)

sealed class BugFilter {
    class TitleFilter(val contains: String) : BugFilter()
    object NotSolvedOrRegressionFilter : BugFilter()
}

enum class BugSort(val sql: String) {
    REPORT_COUNT(sql("COUNT(`report`.`id`)")),
    MAX_REPORT_DATE(sql("MAX(`report`.`date`)")),
    MAX_VERSION_CODE(sql("MAX(`version`.`code`)")),
    USER_COUNT(sql("COUNT(DISTINCT `report`.`installation_id`)")),
    TITLE(sql("`bug`.`title`")),
    SOLVED_VERSION(sql("`solved_version`.`code`")),
}