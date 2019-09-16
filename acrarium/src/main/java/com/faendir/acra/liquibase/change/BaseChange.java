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
import liquibase.changelog.ChangeSet;
import org.hibernate.Session;
import org.hibernate.dialect.Dialect;
import org.hibernate.engine.jdbc.spi.JdbcServices;
import org.hibernate.engine.spi.SessionFactoryImplementor;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManager;
import javax.persistence.Query;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Supplier;

/**
 * @author lukas
 * @since 01.06.18
 */
public abstract class BaseChange implements LiquibaseChangePostProcessor {
    private final String changeId;
    private final EntityManager entityManager;
    private Dialect dialect;

    protected BaseChange(@NonNull String changeId, @NonNull EntityManager entityManager) {
        this.changeId = changeId;
        this.entityManager = entityManager;
    }

    @NonNull
    private Dialect getDialect() {
        if (dialect == null) {
            dialect = ((SessionFactoryImplementor) entityManager.unwrap(Session.class).getSessionFactory()).getServiceRegistry().getService(JdbcServices.class).getDialect();
        }
        return dialect;
    }

    @NonNull
    protected EntityManager getEntityManager() {
        return entityManager;
    }

    @Nullable
    protected String quote(@Nullable String s) {
        return getDialect().openQuote() + s + getDialect().closeQuote();
    }

    @Transactional
    protected void iterate(@NonNull Supplier<Query> supplier, @NonNull Consumer<Object> consumer) {
        int offset = 0;
        List<?> list;
        while (!(list = supplier.get().setFirstResult(offset).setMaxResults(64).getResultList()).isEmpty()) {
            list.forEach(consumer);
            offset += list.size();
        }
    }

    @Override
    public void handle(@NonNull ChangeSet changeSet) {
        if (changeId.equals(changeSet.getId())) {
            afterChange();
        }
    }

    protected abstract void afterChange();
}
