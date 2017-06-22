package com.faendir.acra.mongod.data;

import com.faendir.acra.mongod.model.Bug;
import org.jetbrains.annotations.NotNull;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

/**
 * @author Lukas
 * @since 31.05.2017
 */
interface BugRepository extends MongoRepository<Bug, String> {
    @NotNull
    List<Bug> findByApp(String app);
    @NotNull
    List<Bug> findByReportIdsContains(String reportId);
}
