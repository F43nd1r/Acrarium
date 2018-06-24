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
    @NonNull private final EntityManager entityManager;

    @Autowired
    public StacktraceChange(@NonNull @Lazy EntityManager entityManager) {
        super("2018-06-04-add-stacktrace-table");
        this.entityManager = entityManager;
    }

    @Override
    protected void afterChange() {
        iterate(() -> entityManager.createNativeQuery(
                "SELECT \"stacktrace\", \"version_code\", \"version_name\", \"bug_id\", \"id\" FROM \"report\""), o -> {
            Object[] result = (Object[]) o;
            String trace = (String) result[0];
            int versionCode = (int) result[1];
            String versionName = (String) result[2];
            int bugId = (int) result[3];
            int reportId = (int) result[4];
            Long stacktraceId = (Long) entityManager.createNativeQuery("SELECT \"id\" FROM \"stacktrace\" WHERE \"stacktrace\" = ?1 AND \"version_code\" = ?2")
                    .setParameter(1, trace)
                    .setParameter(2, versionCode)
                    .setMaxResults(1)
                    .getSingleResult();
            if(stacktraceId == null) {
                entityManager.createNativeQuery("INSERT INTO \"stacktrace\" (\"bug_id\", \"stacktrace\", \"version_code\", \"version_name\") VALUES(?1, ?2, ?3, ?4)")
                        .setParameter(1, bugId)
                        .setParameter(2, trace)
                        .setParameter(3, versionCode)
                        .setParameter(4, versionName)
                        .executeUpdate();
                stacktraceId = (Long) entityManager.createNativeQuery("SELECT \"id\" FROM \"stacktrace\" ORDER BY \"id\" DESC").setMaxResults(1).getSingleResult();
            }
            entityManager.createNativeQuery("UPDATE \"report\" SET \"stacktrace_id\" = ?1 WHERE \"id\" = ?2")
                    .setParameter(1, stacktraceId)
                    .setParameter(2, reportId)
                    .executeUpdate();
        });
    }
}
