package com.faendir.acra.mongod.data;

import com.faendir.acra.mongod.model.ProguardMapping;
import org.jetbrains.annotations.NotNull;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

/**
 * @author Lukas
 * @since 19.05.2017
 */
interface MappingRepository extends MongoRepository<ProguardMapping, ProguardMapping.MetaData> {
    @NotNull
    List<ProguardMapping> findAllByIdApp(String app);
}
