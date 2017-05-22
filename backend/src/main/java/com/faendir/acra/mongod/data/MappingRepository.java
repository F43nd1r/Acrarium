package com.faendir.acra.mongod.data;

import com.faendir.acra.mongod.model.ProguardMapping;
import org.springframework.data.mongodb.repository.MongoRepository;

/**
 * @author Lukas
 * @since 19.05.2017
 */
interface MappingRepository extends MongoRepository<ProguardMapping, ProguardMapping.MetaData> {
}
