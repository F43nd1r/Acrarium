package com.faendir.acra.sql.data;

import com.faendir.acra.sql.model.App;
import com.faendir.acra.sql.model.Bug;
import com.faendir.acra.sql.model.Report;
import com.faendir.acra.sql.util.CountResult;
import com.faendir.acra.util.Utils;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.lang.NonNull;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

/**
 * @author Lukas
 * @since 11.12.2017
 */
public interface ReportRepository extends JpaRepository<Report, String> {
    void deleteAllByBugAppAndDateBefore(@NonNull App app, @NonNull Date date);

    Slice<Report> findAllByBugApp(@NonNull App app, @NonNull Pageable pageable);

    Stream<Report> findAllByBugApp(@NonNull App app);

    int countAllByBugApp(@NonNull App app);

    @SuppressWarnings("SpringDataRepositoryMethodReturnTypeInspection")
    @Query("select new com.faendir.acra.sql.util.CountResult(report.date, count(report)) from Report report join Bug bug on report.bug = bug "
           + "where bug.app = ?1 and report.date > ?2 group by function('year',report.date), function('month',report.date), function('day',report.date)")
    List<CountResult<Date>> countAllByDayAfter(@NonNull App app, @NonNull Date date);

    Slice<Report> findAllByBug(@NonNull Bug bug, @NonNull Pageable pageable);

    int countAllByBug(@NonNull Bug bug);

    @SuppressWarnings("SpringDataRepositoryMethodReturnTypeInspection")
    @Query("select new com.faendir.acra.sql.util.CountResult(report.androidVersion, count(report)) from Report report join Bug bug on report.bug = bug "
           + "where bug.app = ?1 group by report.androidVersion")
    List<CountResult<String>> countAllByAndroidVersion(@NonNull App app);

    @SuppressWarnings("SpringDataRepositoryMethodReturnTypeInspection")
    @Query("select new com.faendir.acra.sql.util.CountResult(bug.id, count(report)) from Report report group by report.bug")
    List<CountResult<Integer>> countAllByBug();

    @Transactional
    default void reassignBugs(App app) {
        Map<String, Bug> bugs = new HashMap<>();
        try(Stream<Report> stream = findAllByBugApp(app)) {
            stream.forEach(report -> {
                String stacktrace = Utils.generifyStacktrace(report.getStacktrace(), app.getConfiguration());
                Bug bug = bugs.get(stacktrace);
                if (bug == null) {
                    bug = new Bug(app, stacktrace, report.getVersionCode(), report.getDate());
                }
                report.setBug(bug);
                bugs.put(stacktrace, save(report).getBug());
            });
        }
    }
}
