package com.faendir.acra.data;

import org.springframework.data.mongodb.repository.MongoRepository;

/**
 * @author Lukas
 * @since 19.05.2017
 */
public interface MappingRepository extends MongoRepository<ProguardMapping, ProguardMapping.MetaData> {
}
