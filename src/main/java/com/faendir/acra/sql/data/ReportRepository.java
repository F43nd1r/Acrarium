package com.faendir.acra.sql.data;

import com.faendir.acra.sql.model.App;
import com.faendir.acra.sql.model.Bug;
import com.faendir.acra.sql.model.Report;
import com.faendir.acra.sql.util.AndroidVersionCount;
import com.faendir.acra.sql.util.DayCount;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.lang.NonNull;

import java.util.Date;
import java.util.List;

/**
 * @author Lukas
 * @since 11.12.2017
 */
public interface ReportRepository extends JpaRepository<Report, String> {
    void deleteAllByBugAppAndDateBefore(@NonNull App app, @NonNull Date date);

    Slice<Report> findAllByBugApp(@NonNull App app, @NonNull Pageable pageable);

    int countAllByBugApp(@NonNull App app);

    @SuppressWarnings("SpringDataRepositoryMethodReturnTypeInspection")
    @Query("select new com.faendir.acra.sql.util.DayCount(report.date, count(report)) from Report report join Bug bug on report.bug = bug "
           + "where bug.app = ?1 and report.date > ?2 group by function('year',report.date), function('month',report.date), function('day',report.date)")
    List<DayCount> countAllByDayAfter(@NonNull App app, @NonNull Date date);

    Slice<Report> findAllByBug(@NonNull Bug bug, @NonNull Pageable pageable);

    int countAllByBug(@NonNull Bug bug);

    @SuppressWarnings("SpringDataRepositoryMethodReturnTypeInspection")
    @Query("select new com.faendir.acra.sql.util.AndroidVersionCount(report.androidVersion, count(report)) from Report report join Bug bug on report.bug = bug "
           + "where bug.app = ?1 group by report.androidVersion")
    List<AndroidVersionCount> countAllByAndroidVersion(@NonNull App app);
}
