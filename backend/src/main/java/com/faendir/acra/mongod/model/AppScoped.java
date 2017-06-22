package com.faendir.acra.mongod.model;

import org.jetbrains.annotations.NotNull;

/**
 * @author Lukas
 * @since 16.06.2017
 */
public interface AppScoped {
    @NotNull
    String getApp();
}
