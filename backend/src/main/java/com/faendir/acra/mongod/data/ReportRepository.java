package com.faendir.acra.mongod.data;

import com.faendir.acra.mongod.model.Report;
import org.springframework.data.mongodb.repository.MongoRepository;

/**
 * @author Lukas
 * @since 22.03.2017
 */
interface ReportRepository extends MongoRepository<Report, String> {
}
