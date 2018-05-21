package com.faendir.acra.model.view;

import com.faendir.acra.model.base.BaseApp;
import org.hibernate.annotations.Immutable;
import org.springframework.data.annotation.PersistenceConstructor;

import javax.persistence.Entity;
import javax.persistence.Table;

/**
 * @author lukas
 * @since 20.05.18
 */
@Table(name = "app_view")
@Immutable
@Entity
public class VApp extends BaseApp {
    private int reportCount;

    @PersistenceConstructor
    VApp() {
    }

    public int getReportCount() {
        return reportCount;
    }
}
