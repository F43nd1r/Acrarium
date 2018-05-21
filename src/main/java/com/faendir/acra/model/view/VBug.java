package com.faendir.acra.model.view;

import com.faendir.acra.model.base.BaseBug;
import org.hibernate.annotations.Immutable;
import org.springframework.data.annotation.PersistenceConstructor;

import javax.persistence.Entity;
import javax.persistence.Table;
import java.util.Date;

/**
 * @author lukas
 * @since 17.05.18
 */
@Table(name = "bug_view")
@Immutable
@Entity
public class VBug extends BaseBug {
    private Date lastReport;
    private int reportCount;

    @PersistenceConstructor
    VBug() {
    }

    public int getReportCount() {
        return reportCount;
    }

    public Date getLastReport() {
        return lastReport;
    }
}
