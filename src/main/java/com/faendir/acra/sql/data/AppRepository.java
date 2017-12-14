package com.faendir.acra.sql.data;

import com.faendir.acra.sql.model.App;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * @author Lukas
 * @since 11.12.2017
 */
public interface AppRepository extends JpaRepository<App, Integer> {
}
