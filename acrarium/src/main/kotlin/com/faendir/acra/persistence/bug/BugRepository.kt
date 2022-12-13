package com.faendir.acra.persistence.bug

import com.faendir.acra.dataprovider.AcrariumDataProvider
import com.faendir.acra.dataprovider.AcrariumSort
import com.faendir.acra.jooq.generated.Tables.BUG
import com.faendir.acra.jooq.generated.Tables.BUG_IDENTIFIER
import com.faendir.acra.jooq.generated.Tables.REPORT
import com.faendir.acra.persistence.and
import com.faendir.acra.persistence.app.AppId
import com.faendir.acra.persistence.asOrderFields
import com.faendir.acra.persistence.fetchList
import com.faendir.acra.persistence.fetchListInto
import com.faendir.acra.persistence.fetchValue
import com.faendir.acra.persistence.fetchValueInto
import com.faendir.acra.persistence.hasBugIdentifier
import com.faendir.acra.persistence.matches
import jakarta.validation.constraints.Size
import org.jooq.DSLContext
import org.springframework.security.access.prepost.PostAuthorize
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.stereotype.Repository
import org.springframework.transaction.annotation.Transactional
import java.time.Instant
import java.util.stream.Stream

@Repository
class BugRepository(
    private val jooq: DSLContext,
) {

    @PreAuthorize("hasViewPermission(#identifier.appId)")
    fun findId(identifier: BugIdentifier): BugId? = jooq.select(BUG_IDENTIFIER.BUG_ID).from(BUG_IDENTIFIER).where(BUG_IDENTIFIER.matches(identifier)).fetchValue()

    @PostAuthorize("returnObject == null || hasViewPermission(returnObject.appId)")
    fun find(bugId: BugId): Bug? = jooq.selectFrom(BUG).where(BUG.ID.eq(bugId)).fetchValueInto()

    fun findInRange(appId: AppId, range: ClosedRange<Instant>): List<Bug> =
        jooq.selectFrom(BUG).where(BUG.APP_ID.eq(appId), BUG.LATEST_REPORT.greaterOrEqual(range.start), BUG.LATEST_REPORT.lessOrEqual(range.endInclusive)).fetchListInto()

    fun getIdentifiers(bugId: BugId): List<BugIdentifier> = jooq.selectFrom(BUG_IDENTIFIER).where(BUG_IDENTIFIER.BUG_ID.eq(bugId)).fetchListInto()

    @PreAuthorize("hasViewPermission(#appId)")
    fun getAllIds(appId: AppId): List<BugId> = jooq.select(BUG.ID).from(BUG).where(BUG.APP_ID.eq(appId)).fetchList()


    @PreAuthorize("isReporter()")
    fun create(identifier: BugIdentifier, title: String): BugId {
        val bugId = jooq.insertInto(BUG)
            .set(BUG.APP_ID, identifier.appId)
            .set(BUG.TITLE, title)
            .returningResult(BUG.ID)
            .fetchValue()!!
        jooq.insertInto(BUG_IDENTIFIER)
            .set(BUG_IDENTIFIER.BUG_ID, bugId)
            .set(BUG_IDENTIFIER.APP_ID, identifier.appId)
            .set(BUG_IDENTIFIER.EXCEPTION_CLASS, identifier.exceptionClass)
            .set(BUG_IDENTIFIER.MESSAGE, identifier.message)
            .set(BUG_IDENTIFIER.CRASH_LINE, identifier.crashLine)
            .set(BUG_IDENTIFIER.CAUSE, identifier.cause)
            .execute()
        return bugId
    }

    @PreAuthorize("hasEditPermission(#appId)")
    fun setSolved(appId: AppId, bugId: BugId, solvedVersion: Pair<Int, String>?) {
        jooq.update(BUG)
            .set(BUG.SOLVED_VERSION_CODE, solvedVersion?.first)
            .set(BUG.SOLVED_VERSION_FLAVOR, solvedVersion?.second)
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
        val title = jooq.select(REPORT.EXCEPTION_CLASS, REPORT.MESSAGE).from(REPORT)
            .where(REPORT.hasBugIdentifier(identifier))
            .limit(1)
            .fetchOne {
                val exceptionClass: String = it[REPORT.EXCEPTION_CLASS]
                val message: String? = it[REPORT.MESSAGE]
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
        override fun fetch(filters: Set<BugStats.Filter>, sort: List<AcrariumSort<BugStats.Sort>>, offset: Int, limit: Int): Stream<BugStats> {
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

        override fun size(filters: Set<BugStats.Filter>) = jooq.selectCount().from(BUG).where(BUG.APP_ID.eq(appId).and(filters)).fetchValue() ?: 0

    }
}