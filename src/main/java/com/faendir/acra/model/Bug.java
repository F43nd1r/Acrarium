package com.faendir.acra.model;

import com.faendir.acra.model.base.BaseBug;
import org.springframework.data.annotation.PersistenceConstructor;
import org.springframework.lang.NonNull;

import javax.persistence.Entity;

/**
 * @author Lukas
 * @since 08.12.2017
 */
@Entity
public class Bug extends BaseBug {
    @PersistenceConstructor
    Bug() {
    }

    public Bug(@NonNull App app, @NonNull String stacktrace, int versionCode) {
        super(app, versionCode);
        getStacktraces().add(stacktrace);
        setTitle(stacktrace.split("\n", 2)[0]);
    }
}
