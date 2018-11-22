package com.faendir.acra.ui.view.bug.tabs;

import com.faendir.acra.model.Bug;
import com.faendir.acra.model.QReport;
import com.faendir.acra.service.DataService;
import com.faendir.acra.ui.base.statistics.Statistics;
import com.faendir.acra.ui.view.bug.BugView;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.spring.annotation.SpringComponent;
import com.vaadin.flow.spring.annotation.UIScope;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.lang.NonNull;

/**
 * @author lukas
 * @since 11.10.18
 */
@UIScope
@SpringComponent("bugStatisticsTab")
@Route(value = "statistics", layout = BugView.class)
public class StatisticsTab extends BugTab<Div> {
    @Autowired
    public StatisticsTab(@NonNull DataService dataService) {
        super(dataService);
        getContent().setSizeFull();
    }

    @Override
    void init(Bug bug) {
        getContent().removeAll();
        getContent().add(new Statistics(bug.getApp(), QReport.report.stacktrace.bug.eq(bug), getDataService()));
    }
}
