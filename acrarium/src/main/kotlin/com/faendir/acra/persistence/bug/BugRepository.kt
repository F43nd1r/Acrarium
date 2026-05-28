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

import com.faendir.acra.dataprovider.AcrariumDataProvider
import com.faendir.acra.dataprovider.AcrariumSort
import com.faendir.acra.jooq.generated.tables.references.BUG
import com.faendir.acra.jooq.generated.tables.references.BUG_IDENTIFIER
import com.faendir.acra.jooq.generated.tables.references.REPORT
import com.faendir.acra.persistence.*
import com.faendir.acra.persistence.app.AppId
import com.faendir.acra.persistence.version.VersionKey
import jakarta.validation.constraints.Size
import mu.KotlinLogging
import org.jooq.DSLContext
import org.springframework.security.access.prepost.PostAuthorize
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.stereotype.Repository
import org.springframework.transaction.annotation.Transactional
import java.time.Instant
import java.util.stream.Stream


private val logger = KotlinLogging.logger {}

@Repository
class BugRepository(
    private val jooq: DSLContext,
) {

    @PreAuthorize("isReporter() || hasViewPermission(#identifier.appId)")
    fun findId(identifier: BugIdentifier): BugId? {
        val results =  jooq.select(BUG_IDENTIFIER.BUG_ID).from(BUG_IDENTIFIER).where(BUG_IDENTIFIER.matches(identifier)).fetchList()

        if (results.size == 1) {
            return results[0]
        }
        if (results.size > 1) {

            var ids = ""
            for (id in results) {
                ids += "$id, "
            }

            logger.warn { "Found multiple bug ids[$ids] for identifier[$identifier]. Using ${results[0]}" }
            return results[0]
        }
        return null
    }

    @PostAuthorize("returnObject == null || hasViewPermission(returnObject.appId)")
    fun find(bugId: BugId): Bug? =
        jooq.selectFrom(BUG).where(BUG.ID.eq(bugId)).fetchValueInto()

    fun findInRange(appId: AppId, range: ClosedRange<Instant>): List<Bug> =
        jooq.selectFrom(BUG)
            .where(
                BUG.APP_ID.eq(appId),
                BUG.LATEST_REPORT.greaterOrEqual(range.start),
                BUG.LATEST_REPORT.lessOrEqual(range.endInclusive)
            )
            .fetchListInto()

    fun getIdentifiers(bugId: BugId): List<BugIdentifier> =
        jooq.select(BUG_IDENTIFIER.fields().toList() - BUG_IDENTIFIER.BUG_ID)
            .from(BUG_IDENTIFIER)
            .where(BUG_IDENTIFIER.BUG_ID.eq(bugId))
            .fetchListInto()

    @PreAuthorize("hasViewPermission(#appId)")
    fun getAllIds(appId: AppId): List<BugId> =
        jooq.select(BUG.ID.NOT_NULL).from(BUG).where(BUG.APP_ID.eq(appId)).fetchList()


    @PreAuthorize("isReporter()")
    fun create(identifier: BugIdentifier, title: String): BugId {
        var bugId = findId(identifier)

        if (bugId == null) {
            val insertId = jooq.insertInto(BUG)
                .set(BUG.APP_ID, identifier.appId)
                .set(BUG.TITLE, title)
                .returningResult(BUG.ID)
                .fetchValue()

            jooq.insertInto(BUG_IDENTIFIER)
                .set(BUG_IDENTIFIER.BUG_ID, insertId)
                .set(BUG_IDENTIFIER.APP_ID, identifier.appId)
                .set(BUG_IDENTIFIER.EXCEPTION_CLASS, identifier.exceptionClass)
                .set(BUG_IDENTIFIER.MESSAGE, identifier.message)
                .set(BUG_IDENTIFIER.CRASH_LINE, identifier.crashLine)
                .set(BUG_IDENTIFIER.CAUSE, identifier.cause)
                .execute()

            bugId = insertId
        }

        if (bugId == null) {
            throw RuntimeException("BugId didn't exist and could not be created")
        }

        return bugId
    }

    @PreAuthorize("hasEditPermission(#appId)")
    fun setSolved(appId: AppId, bugId: BugId, solvedVersion: VersionKey?) {
        jooq.update(BUG)
            .set(BUG.SOLVED_VERSION_CODE, solvedVersion?.code)
            .set(BUG.SOLVED_VERSION_FLAVOR, solvedVersion?.flavor)
            .where(BUG.ID.eq(bugId), BUG.APP_ID.eq(appId))
            .execute()
    }

    @PreAuthorize("hasEditPermission(#appId)")
    fun setTitle(appId: AppId, bugId: BugId, title: String) {
        jooq.update(BUG)
            .set(BUG.TITLE, title)
            .where(BUG.ID.eq(bugId), BUG.APP_ID.eq(appId))
            .execute()
    }

    @PreAuthorize("hasEditPermission(#appId)")
    @Transactional
    fun mergeBugs(appId: AppId, bugIds: @Size(min = 2) Collection<BugId>, title: String) {
        val firstBug = bugIds.first()
        val remainingBugs = bugIds.drop(1)
        jooq.update(BUG)
            .set(BUG.TITLE, title)
            .where(BUG.ID.eq(firstBug), BUG.APP_ID.eq(appId))
            .execute()
        jooq.update(BUG_IDENTIFIER)
            .set(BUG_IDENTIFIER.BUG_ID, firstBug)
            .where(BUG_IDENTIFIER.BUG_ID.`in`(remainingBugs))
            .execute()
        jooq.update(REPORT)
            .set(REPORT.BUG_ID, firstBug)
            .where(REPORT.BUG_ID.`in`(remainingBugs))
            .execute()
    }

    @PreAuthorize("hasEditPermission(#identifier.appId)")
    @Transactional
    fun splitFromBug(bugId: BugId, identifier: BugIdentifier) {
        val title = jooq.select(REPORT.EXCEPTION_CLASS.NOT_NULL, REPORT.MESSAGE).from(REPORT)
            .where(REPORT.hasBugIdentifier(identifier))
            .limit(1)
            .fetchOne {
                val (exceptionClass: String, message: String?) = it
                if (message != null) "$exceptionClass:$message" else exceptionClass
            }!!
        val newBugId = jooq.insertInto(BUG)
            .set(BUG.APP_ID, identifier.appId)
            .set(BUG.TITLE, title)
            .returningResult(BUG.ID)
            .fetchValue()!!
        jooq.update(BUG_IDENTIFIER)
            .set(BUG_IDENTIFIER.BUG_ID, newBugId)
            .where(BUG_IDENTIFIER.matches(identifier))
            .execute()
        jooq.update(REPORT)
            .set(REPORT.BUG_ID, newBugId)
            .where(REPORT.BUG_ID.eq(bugId).and(REPORT.hasBugIdentifier(identifier)))
            .execute()
    }

    @PreAuthorize("hasEditPermission(#appId)")
    @Transactional
    fun delete(appId: AppId, bugId: BugId) {
        jooq.deleteFrom(BUG).where(BUG.ID.eq(bugId), BUG.APP_ID.eq(appId)).execute()
    }

    @PreAuthorize("hasViewPermission(#appId)")
    fun getProvider(appId: AppId) = object : AcrariumDataProvider<BugStats, BugStats.Filter, BugStats.Sort>() {
        override fun fetch(
            filters: Set<BugStats.Filter>,
            sort: List<AcrariumSort<BugStats.Sort>>,
            offset: Int,
            limit: Int
        ): Stream<BugStats> {
            return jooq.select(
                BUG.ID,
                BUG.TITLE,
                BUG.REPORT_COUNT,
                BUG.LATEST_VERSION_CODE,
                BUG.LATEST_VERSION_FLAVOR,
                BUG.LATEST_REPORT,
                BUG.SOLVED_VERSION_CODE,
                BUG.SOLVED_VERSION_FLAVOR,
                BUG.AFFECTED_INSTALLATIONS,
            )
                .from(BUG)
                .where(BUG.APP_ID.eq(appId).and(filters))
                .orderBy(sort.asOrderFields())
                .offset(offset)
                .limit(limit)
                .fetchListInto<BugStats>()
                .stream()
        }

        override fun size(filters: Set<BugStats.Filter>) =
            jooq.selectCount().from(BUG).where(BUG.APP_ID.eq(appId).and(filters)).fetchValue() ?: 0

    }
}