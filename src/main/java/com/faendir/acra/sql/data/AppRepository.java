package com.faendir.acra.sql.data;

import com.faendir.acra.model.App;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.lang.NonNull;

import java.util.Optional;

/**
 * @author Lukas
 * @since 11.12.2017
 */
public interface AppRepository extends JpaRepository<App, Integer> {
    Optional<App> findByReporterUsername(@NonNull String username);
}
