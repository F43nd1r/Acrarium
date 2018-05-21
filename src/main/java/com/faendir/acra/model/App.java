package com.faendir.acra.model;

import com.faendir.acra.model.base.BaseApp;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;
import org.springframework.data.annotation.PersistenceConstructor;
import org.springframework.lang.NonNull;

import javax.persistence.CascadeType;
import javax.persistence.Embeddable;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.OneToOne;

/**
 * @author Lukas
 * @since 08.12.2017
 */
@Entity
public class App extends BaseApp {
    @OneToOne(cascade = CascadeType.ALL, optional = false, orphanRemoval = true, fetch = FetchType.LAZY)
    @OnDelete(action = OnDeleteAction.CASCADE)
    private User reporter;
    private Configuration configuration;

    @PersistenceConstructor
    App() {
    }

    public App(@NonNull String name, @NonNull User reporter) {
        super(name);
        this.reporter = reporter;
        this.configuration = new Configuration();
    }

    @NonNull
    public User getReporter() {
        return reporter;
    }

    public void setReporter(User reporter) {
        this.reporter = reporter;
    }

    public Configuration getConfiguration() {
        return configuration;
    }

    public void setConfiguration(Configuration configuration) {
        this.configuration = configuration;
    }

    @Embeddable
    public static class Configuration {
        private boolean matchByMessage;
        private boolean ignoreInstanceIds;
        private boolean ignoreAndroidLineNumbers;

        @PersistenceConstructor
        Configuration() {
            matchByMessage = true;
            ignoreInstanceIds = true;
            ignoreAndroidLineNumbers = true;
        }

        public Configuration(boolean matchByMessage, boolean ignoreInstanceIds, boolean ignoreAndroidLineNumbers) {
            this.matchByMessage = matchByMessage;
            this.ignoreInstanceIds = ignoreInstanceIds;
            this.ignoreAndroidLineNumbers = ignoreAndroidLineNumbers;
        }

        public boolean matchByMessage() {
            return matchByMessage;
        }

        public boolean ignoreInstanceIds() {
            return ignoreInstanceIds;
        }

        public boolean ignoreAndroidLineNumbers() {
            return ignoreAndroidLineNumbers;
        }
    }
}
