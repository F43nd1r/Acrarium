package com.faendir.acra.sql.data;

import com.faendir.acra.sql.model.App;
import com.faendir.acra.sql.model.ProguardMapping;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.lang.NonNull;

import java.util.List;
import java.util.Optional;

/**
 * @author Lukas
 * @since 11.12.2017
 */
public interface ProguardMappingRepository extends JpaRepository<ProguardMapping, ProguardMapping.MetaData> {
    List<ProguardMapping> findAllByApp(@NonNull App app);

    default Optional<ProguardMapping> findById(@NonNull App app, int versionCode) {
        return findById(new ProguardMapping.MetaData(app, versionCode));
    }
}
