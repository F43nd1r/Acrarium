package com.faendir.acra.mongod.data;

import com.faendir.acra.mongod.model.Report;
import org.jetbrains.annotations.NotNull;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.stream.Stream;

/**
 * @author Lukas
 * @since 22.03.2017
 */
interface ReportRepository extends MongoRepository<Report, String> {
    @NotNull
    Stream<Report> streamAllByApp(String app);

    int countByApp(String app);

    @NotNull
    Stream<Report> streamAllByIdIn(@NotNull Iterable<String> ids);

    @NotNull
    Page<Report> findAllByApp(String app, Pageable pageable);

    @NotNull
    Page<Report> findAllByIdIn(@NotNull Iterable<String> ids, Pageable pageable);


}
