package com.faendir.acra.mongod.data;

import com.faendir.acra.mongod.model.ProguardMapping;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;

import java.util.List;

/**
 * @author Lukas
 * @since 19.05.2017
 */
interface MappingRepository extends MongoRepository<ProguardMapping, ProguardMapping.MetaData> {
    @Query("{id.app:?0}")
    List<ProguardMapping> findByApp(String app);
}
