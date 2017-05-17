package com.faendir.acra.data;

import org.springframework.data.mongodb.repository.MongoRepository;

/**
 * @author Lukas
 * @since 22.03.2017
 */
public interface ReportRepository extends MongoRepository<Report, String> {
}
