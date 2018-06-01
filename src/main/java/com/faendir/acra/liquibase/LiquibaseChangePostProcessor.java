package com.faendir.acra.liquibase;

import liquibase.changelog.ChangeSet;

/**
 * @author lukas
 * @since 01.06.18
 */
public abstract class LiquibaseChangePostProcessor {
    private final String changeId;

    protected LiquibaseChangePostProcessor(String changeId) {
        this.changeId = changeId;
    }

    void handle(ChangeSet changeSet) {
        if (changeId.equals(changeSet.getId())) {
            afterChange();
        }
    }

    protected abstract void afterChange();
}
