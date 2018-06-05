/*
 * (C) Copyright 2018 Lukas Morawietz (https://github.com/F43nd1r)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.faendir.acra.liquibase.change;

import com.faendir.acra.liquibase.LiquibaseChangePostProcessor;
import com.faendir.acra.model.Stacktrace;
import com.faendir.acra.service.DataService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;

import javax.persistence.EntityManager;

/**
 * @author lukas
 * @since 04.06.18
 */
@Component
public class StacktraceChange extends LiquibaseChangePostProcessor {
    @NonNull private final DataService dataService;
    @NonNull private final EntityManager entityManager;

    @Autowired
    public StacktraceChange(@NonNull @Lazy DataService dataService, @NonNull @Lazy EntityManager entityManager) {
        super("2018-06-04-add-stacktrace-table");
        this.dataService = dataService;
        this.entityManager = entityManager;
    }

    @Override
    protected void afterChange() {
        dataService.transformAllReports(report -> {
            Object[] result = (Object[]) entityManager.createNativeQuery(
                    "SELECT report.stacktrace, report.version_code, report.version_name, report.bug_id FROM report where report.id = ?1")
                    .setParameter(1, report.getId())
                    .getSingleResult();
            String trace = (String) result[0];
            int versionCode = (int) result[1];
            String versionName = (String) result[2];
            int bugId = (int) result[3];
            Stacktrace stacktrace = dataService.findStacktrace(trace)
                    .orElseGet(() -> new Stacktrace(dataService.findBug(bugId).orElseThrow(IllegalStateException::new), trace, versionCode, versionName));
            report.setStacktrace(stacktrace);
        });
    }
}
