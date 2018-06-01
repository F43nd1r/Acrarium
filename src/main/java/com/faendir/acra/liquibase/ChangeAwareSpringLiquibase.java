package com.faendir.acra.liquibase;

import liquibase.Liquibase;
import liquibase.changelog.ChangeSet;
import liquibase.changelog.DatabaseChangeLog;
import liquibase.changelog.visitor.AbstractChangeExecListener;
import liquibase.database.Database;
import liquibase.exception.LiquibaseException;
import liquibase.integration.spring.SpringLiquibase;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.liquibase.LiquibaseProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;
import java.sql.Connection;
import java.util.List;

/**
 * @author lukas
 * @since 01.06.18
 */
@Component("liquibase")
@EnableConfigurationProperties(LiquibaseProperties.class)
public class ChangeAwareSpringLiquibase extends SpringLiquibase {
    @NonNull private final List<LiquibaseChangePostProcessor> processors;

    @Autowired
    public ChangeAwareSpringLiquibase(@NonNull LiquibaseProperties properties, @NonNull DataSource dataSource, @NonNull List<LiquibaseChangePostProcessor> processors) {
        this.processors = processors;
        setDataSource(dataSource);
        setChangeLog(properties.getChangeLog());
        setContexts(properties.getContexts());
        setDefaultSchema(properties.getDefaultSchema());
        setDropFirst(properties.isDropFirst());
        setShouldRun(properties.isEnabled());
        setLabels(properties.getLabels());
        setChangeLogParameters(properties.getParameters());
        setRollbackFile(properties.getRollbackFile());
    }

    @Override
    protected Liquibase createLiquibase(Connection c) throws LiquibaseException {
        Liquibase liquibase = super.createLiquibase(c);
        liquibase.setChangeExecListener(new AbstractChangeExecListener() {
            @Override
            public void ran(ChangeSet changeSet, DatabaseChangeLog databaseChangeLog, Database database, ChangeSet.ExecType execType) {
                processors.forEach(processor -> processor.handle(changeSet));
            }
        });
        return liquibase;
    }
}
