package com.faendir.acra.mongod.data;

import com.faendir.acra.mongod.model.Report;
import org.springframework.data.mongodb.repository.CountQuery;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;

import java.util.stream.Stream;

/**
 * @author Lukas
 * @since 22.03.2017
 */
interface ReportRepository extends MongoRepository<Report, String> {
    @Query("{app:?0}")
    Stream<Report> findByApp(String app);

    long countByApp(String app);

    @Query("{content.map.STACK_TRACE:?0,content.map.APP_VERSION_CODE:?1}")
    Stream<Report> findByBug(String stacktrace, int versionCode);

    @CountQuery("{content.map.STACK_TRACE:?0,content.map.APP_VERSION_CODE:?1}")
    long countByBug(String stacktrace, int versionCode);


}
