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
        iterate(() -> entityManager.createNativeQuery("SELECT" + quote("version_code") + ", " + quote("version_name") + ", " + quote("app_id") + " FROM " + quote("stacktrace") + " JOIN " + quote("bug") + " ON " + quote("stacktrace") + "." + quote("bug_id") + " = " + quote("bug") + "." + quote("id") + " GROUP BY " + quote("version_code") + ", " + quote("version_name") + ", " + quote("app_id")), o -> {

        });
    }
}
