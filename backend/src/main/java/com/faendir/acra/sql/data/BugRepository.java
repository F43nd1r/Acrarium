package com.faendir.acra.sql.data;

import com.faendir.acra.sql.model.App;
import com.faendir.acra.sql.model.Bug;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.lang.NonNull;

import java.util.List;
import java.util.Optional;

/**
 * @author Lukas
 * @since 11.12.2017
 */
public interface BugRepository extends JpaRepository<Bug, Integer> {
    List<Bug> findAllByApp(@NonNull App app);
    Slice<Bug> findAllByApp(@NonNull App app, @NonNull Pageable pageable);
    Slice<Bug> findAllByAppAndSolvedFalse(@NonNull App app, @NonNull Pageable pageable);
    int countAllByApp(@NonNull App app);
    int countAllByAppAndSolvedFalse(@NonNull App app);
    Optional<Bug> findBugByAppAndStacktrace(@NonNull App app, @NonNull String stacktrace);
}
