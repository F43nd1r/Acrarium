package com.faendir.acra.sql.data;

import com.faendir.acra.model.Permission;
import com.faendir.acra.model.view.VApp;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.lang.NonNull;

/**
 * @author lukas
 * @since 20.05.18
 */
public interface AppViewRepository extends JpaRepository<VApp, Integer> {

    @Query("select app from VApp app where app not in (select permission.app from User user join user.permissions permission where user.username = ?1 and permission.level < ?2)")
    Slice<VApp> findAllByPermissionWithDefaultIncluded(@NonNull String username, @NonNull Permission.Level level, @NonNull Pageable pageable);

    @Query("select app from VApp app where app in (select permission.app from User user join user.permissions permission where user.username = ?1 and permission.level >= ?2)")
    Slice<VApp> findAllByPermissionWithDefaultExcluded(@NonNull String username, @NonNull Permission.Level level, @NonNull Pageable pageable);

    @Query("select count(app) from VApp app where app not in (select permission.app from User user join user.permissions permission where user.username = ?1 and permission.level < ?2)")
    int countByPermissionWithDefaultIncluded(@NonNull String username, @NonNull Permission.Level level);

    @Query("select count(app) from VApp app where app in (select permission.app from User user join user.permissions permission where user.username = ?1 and permission.level >= ?2)")
    int countByPermissionWithDefaultExcluded(@NonNull String username, @NonNull Permission.Level level);
}
