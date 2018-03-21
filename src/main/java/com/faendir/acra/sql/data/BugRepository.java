package com.faendir.acra.sql.data;

import com.faendir.acra.sql.model.App;
import com.faendir.acra.sql.model.Bug;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
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
    Slice<Bug> findAllByApp(@NonNull App app, @NonNull Pageable pageable);

    Slice<Bug> findAllByAppAndSolvedFalse(@NonNull App app, @NonNull Pageable pageable);

    int countAllByApp(@NonNull App app);

    int countAllByAppAndSolvedFalse(@NonNull App app);

    Optional<Bug> findBugByAppAndStacktraces(@NonNull App app, @NonNull String stacktrace);

    @Query("select bug from Bug bug join fetch bug.stacktraces where bug in ?1")
    List<Bug> loadStacktraces(Collection<Bug> bugs);

    @Query("select bug from Bug bug join fetch bug.app join fetch bug.stacktraces where bug.id = ?1")
    Optional<Bug> findByIdEager(int id);

    @Transactional
    @Modifying
    @Query("delete from Bug bug where bug not in (select report.bug from Report report group by report.bug)")
    void deleteOrphans();
}
