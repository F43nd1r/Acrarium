package com.faendir.acra.ui.view.bug.tabs;

import com.faendir.acra.model.QReport;
import com.faendir.acra.model.view.VBug;
import com.faendir.acra.service.data.DataService;
import com.faendir.acra.ui.navigation.NavigationManager;
import com.faendir.acra.ui.view.base.Statistics;
import com.faendir.acra.util.Style;
import com.vaadin.spring.annotation.SpringComponent;
import com.vaadin.spring.annotation.ViewScope;
import com.vaadin.ui.Component;
import com.vaadin.ui.Panel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.lang.NonNull;

/**
 * @author lukas
 * @since 21.05.18
 */
@SpringComponent("bugStatisticsTab")
@ViewScope
public class StatisticsTab implements BugTab {
    @NonNull private final DataService dataService;

    @Autowired
    public StatisticsTab(@NonNull DataService dataService) {
        this.dataService = dataService;
    }

    @Override
    public Component createContent(@NonNull VBug bug, @NonNull NavigationManager navigationManager) {
        Panel root = new Panel(new Statistics(QReport.report.bug.id.eq(bug.getId()), dataService));
        root.setSizeFull();
        Style.apply(root, Style.NO_BACKGROUND, Style.NO_BORDER);
        return root;
    }

    @Override
    public String getCaption() {
        return "Statistics";
    }

    @Override
    public int getOrder() {
        return 2;
    }
}
