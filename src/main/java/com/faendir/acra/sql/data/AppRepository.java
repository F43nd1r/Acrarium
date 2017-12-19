package com.faendir.acra.sql.data;

import com.faendir.acra.sql.model.App;
import com.faendir.acra.sql.model.Permission;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.lang.NonNull;

import java.util.Optional;

/**
 * @author Lukas
 * @since 11.12.2017
 */
public interface AppRepository extends JpaRepository<App, Integer> {
    Optional<App> findByReporterUsername(@NonNull String username);

    @Query("select app from App app where app not in (select permission.app from User user join user.permissions permission where user.username = ?1 and permission.level < ?2)")
    Slice<App> findAllByPermissionWithDefaultIncluded(@NonNull String username, @NonNull Permission.Level level, @NonNull Pageable pageable);

    @Query("select app from App app where app in (select permission.app from User user join user.permissions permission where user.username = ?1 and permission.level >= ?2)")
    Slice<App> findAllByPermissionWithDefaultExcluded(@NonNull String username, @NonNull Permission.Level level, @NonNull Pageable pageable);

    @Query("select count(app) from App app where app not in (select permission.app from User user join user.permissions permission where user.username = ?1 and permission.level < ?2)")
    int countByPermissionWithDefaultIncluded(@NonNull String username, @NonNull Permission.Level level);

    @Query("select count(app) from App app where app in (select permission.app from User user join user.permissions permission where user.username = ?1 and permission.level >= ?2)")
    int countByPermissionWithDefaultExcluded(@NonNull String username, @NonNull Permission.Level level);

    @SuppressWarnings("SpringDataMethodInconsistencyInspection")
    default Optional<App> findByEncodedId(String encodedId) {
        try {
            return findById(Integer.parseInt(encodedId));
        } catch (NumberFormatException e) {
            return Optional.empty();
        }
    }
}
