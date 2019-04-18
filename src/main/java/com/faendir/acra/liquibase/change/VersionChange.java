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

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;

import javax.persistence.EntityManager;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * @author lukas
 * @since 09.12.18
 */
@Component
public class VersionChange extends BaseChange {
    @NonNull
    private final EntityManager entityManager;

    @Autowired
    public VersionChange(@NonNull @Lazy EntityManager entityManager) {
        super("2018-12-9-version-entity", entityManager);
        this.entityManager = entityManager;
    }

    @Override
    protected void afterChange() {
        iterate(() -> entityManager.createNativeQuery("SELECT " + quote("version_code") + ", " + quote("version_name") + ", " + quote("app_id") + ", GROUP_CONCAT(" + quote("stacktrace") + "." + quote("id") + " SEPARATOR ',') FROM " + quote("stacktrace") + " JOIN " + quote("bug") + " ON " + quote("stacktrace") + "." + quote("bug_id") + " = " + quote("bug") + "." + quote("id") + " GROUP BY " + quote("version_code") + ", " + quote("version_name") + ", " + quote("app_id")), o -> {
            Object[] result = (Object[]) o;
            int versionCode = (int) result[0];
            String versioName = (String) result[1];
            int appId = (int) result[2];
            List<Integer> stacktraces = Stream.of(((String) result[3]).split(",")).map(Integer::parseInt).collect(Collectors.toList());
            List<Object> list = entityManager.createNativeQuery("SELECT " + quote("mappings") + " FROM " + quote("proguard_mapping") + " WHERE " + quote("version_code") + " = ?1 AND " + quote("app_id") + " = ?2")
                    .setParameter(1, versionCode)
                    .setParameter(2, appId)
                    .getResultList();
            String mappings = list.isEmpty() ? null : (String) list.get(0);
            entityManager.createNativeQuery("INSERT INTO " + quote("version") + "(" + quote("code") + ", " + quote("name") + ", " + quote("app_id") + ", " + quote("mappings") + ") VALUES (?1, ?2, ?3, ?4)")
                    .setParameter(1, versionCode)
                    .setParameter(2, versioName)
                    .setParameter(3, appId)
                    .setParameter(4, mappings)
                    .executeUpdate();
            int id = (int) entityManager.createNativeQuery("SELECT " + quote("id") + " FROM " + quote("version") + " WHERE " + quote("code") + " = ?1 AND " + quote("app_id") + " = ?2")
                    .setParameter(1, versionCode)
                    .setParameter(2, appId)
                    .getSingleResult();
            entityManager.createNativeQuery("UPDATE " + quote("stacktrace") + " SET " + quote("version_id") + " = ?1 WHERE " + quote("id") + " IN ?2")
                    .setParameter(1, id)
                    .setParameter(2, stacktraces)
                    .executeUpdate();
        });
    }
}
