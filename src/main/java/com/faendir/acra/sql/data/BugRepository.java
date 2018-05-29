package com.faendir.acra.sql.data;

import com.faendir.acra.model.App;
import com.faendir.acra.model.Bug;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.lang.NonNull;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

/**
 * @author Lukas
 * @since 11.12.2017
 */
public interface BugRepository extends JpaRepository<Bug, Integer> {

    Optional<Bug> findBugByAppAndStacktraces(@NonNull App app, @NonNull String stacktrace);

    @SuppressWarnings("SpringDataRepositoryMethodReturnTypeInspection")
    @Query("select stacktrace from Bug bug join bug.stacktraces stacktrace where bug in ?1")
    List<String> loadStacktraces(Collection<Integer> ids);

    @Query("select bug from Bug bug join fetch bug.app join fetch bug.stacktraces where bug.id = ?1")
    Optional<Bug> findByIdEager(int id);

    @Transactional
    @Modifying
    @Query("delete from Bug bug where bug not in (select report.bug from Report report group by report.bug)")
    void deleteOrphans();

    @Transactional
    @Modifying
    void deleteAllByIdIn(@NonNull List<Integer> ids);
}
