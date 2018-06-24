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
import org.acra.ReportField;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;

import javax.persistence.EntityManager;

/**
 * @author lukas
 * @since 01.06.18
 */
@Component
public class ReportColumnsChange extends LiquibaseChangePostProcessor {
    @NonNull private final EntityManager entityManager;

    @Autowired
    public ReportColumnsChange(@NonNull @Lazy EntityManager entityManager) {
        super("2018-06-01-add-report-columns");
        this.entityManager = entityManager;
    }

    @Override
    protected void afterChange() {
        iterate(() -> entityManager.createNativeQuery("SELECT content FROM report"), o -> {
            String content = (String) o;
            JSONObject json = new JSONObject(content);
            String id = json.getString(ReportField.REPORT_ID.name());
            String brand = json.optString(ReportField.BRAND.name());
            String installationId = json.optString(ReportField.INSTALLATION_ID.name());
            entityManager.createNativeQuery("UPDATE report SET brand = ?1, installation_id = ?2 WHERE id = ?3")
                    .setParameter(1, brand)
                    .setParameter(2, installationId)
                    .setParameter(3, id)
                    .executeUpdate();
        });
    }
}
