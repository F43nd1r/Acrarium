package com.faendir.acra.sql.data;

import com.faendir.acra.sql.model.App;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.lang.NonNull;

import java.util.Optional;

/**
 * @author Lukas
 * @since 11.12.2017
 */
public interface AppRepository extends JpaRepository<App, Integer> {

    Optional<App> findByReporterUsername(@NonNull String username);

    @SuppressWarnings("SpringDataMethodInconsistencyInspection")
    default Optional<App> findByEncodedId(String encodedId) {
        try {
            return findById(Integer.parseInt(encodedId));
        } catch (NumberFormatException e) {
            return Optional.empty();
        }
    }
}
