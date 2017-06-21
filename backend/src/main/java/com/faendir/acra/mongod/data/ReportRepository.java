package com.faendir.acra.mongod.data;

import com.faendir.acra.mongod.model.Report;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.stream.Stream;

/**
 * @author Lukas
 * @since 22.03.2017
 */
interface ReportRepository extends MongoRepository<Report, String> {
    Stream<Report> streamAllByApp(String app);

    long countByApp(String app);

    Stream<Report> streamAllByIdIn(Iterable<String> ids);


}
