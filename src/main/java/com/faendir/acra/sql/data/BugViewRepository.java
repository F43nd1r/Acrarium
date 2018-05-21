package com.faendir.acra.sql.data;

import com.faendir.acra.model.App;
import com.faendir.acra.model.view.VBug;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.lang.NonNull;

/**
 * @author lukas
 * @since 17.05.18
 */
public interface BugViewRepository extends JpaRepository<VBug, Integer> {
    Slice<VBug> findAllByApp(@NonNull App app, @NonNull Pageable pageable);

    Slice<VBug> findAllByAppAndSolvedFalse(@NonNull App app, @NonNull Pageable pageable);

    int countAllByApp(@NonNull App app);

    int countAllByAppAndSolvedFalse(@NonNull App app);
}
