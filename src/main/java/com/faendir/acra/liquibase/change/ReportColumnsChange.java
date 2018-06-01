package com.faendir.acra.liquibase.change;

import com.faendir.acra.liquibase.LiquibaseChangePostProcessor;
import com.faendir.acra.model.Report;
import com.faendir.acra.service.data.DataService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;

/**
 * @author lukas
 * @since 01.06.18
 */
@Component
public class ReportColumnsChange extends LiquibaseChangePostProcessor {

    @NonNull private final DataService dataService;

    @Autowired
    public ReportColumnsChange(@NonNull @Lazy DataService dataService) {
        super("2018-06-01-add-report-columns");
        this.dataService = dataService;
    }

    @Override
    protected void afterChange() {
        dataService.transformAllReports(Report::initFields);
    }
}
