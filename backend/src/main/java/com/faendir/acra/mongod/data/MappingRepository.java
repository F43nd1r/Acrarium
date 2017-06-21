package com.faendir.acra.mongod.data;

import com.faendir.acra.mongod.model.ProguardMapping;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

/**
 * @author Lukas
 * @since 19.05.2017
 */
interface MappingRepository extends MongoRepository<ProguardMapping, ProguardMapping.MetaData> {
    List<ProguardMapping> findAllByIdApp(String app);
}
