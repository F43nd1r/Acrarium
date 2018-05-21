package com.faendir.acra.sql.data;

import com.faendir.acra.model.App;
import com.faendir.acra.model.Bug;
import com.faendir.acra.model.Report;
import com.faendir.acra.model.base.BaseBug;
import com.faendir.acra.sql.util.CountResult;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.lang.NonNull;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

/**
 * @author Lukas
 * @since 11.12.2017
 */
public interface ReportRepository extends JpaRepository<Report, String> {
    @Transactional
    @Modifying
    void deleteAllByBugAppAndDateBefore(@NonNull App app, @NonNull Date date);

    Slice<Report> findAllByBugApp(@NonNull App app, @NonNull Pageable pageable);

    @Query("select report from Report report join fetch report.bug bug join fetch bug.app app where app = ?1")
    Stream<Report> streamAllByAppEager(@NonNull App app);

    @Query("select report from Report report join fetch report.bug bug join fetch bug.app app where report.id = ?1")
    Optional<Report> findByIdEager(@NonNull String id);

    int countByBugApp(@NonNull App app);

    @SuppressWarnings("SpringDataRepositoryMethodReturnTypeInspection")
    @Query("select new com.faendir.acra.sql.util.CountResult(function('date', report.date), count(report)) from Report report join Bug bug on report.bug = bug "
           + "where bug.app = ?1 and report.date > ?2 group by function('date', report.date)")
    List<CountResult<Date>> countAllByDayAfter(@NonNull App app, @NonNull Date date);

    Slice<Report> findAllByBug(@NonNull Bug bug, @NonNull Pageable pageable);

    Stream<Report> streamAllByBugIn(@NonNull List<? extends BaseBug> bugs);

    int countByBug(@NonNull Bug bug);

    @SuppressWarnings("SpringDataRepositoryMethodReturnTypeInspection")
    @Query("select new com.faendir.acra.sql.util.CountResult(report.androidVersion, count(report)) from Report report join Bug bug on report.bug = bug "
           + "where bug.app = ?1 group by report.androidVersion")
    List<CountResult<String>> countByAndroidVersion(@NonNull App app);

    @Query("select max(report.date) from Report report where report.bug = ?1")
    Optional<Date> maxDateByBug(@NonNull Bug bug);
}
