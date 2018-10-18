package com.faendir.acra.ui.view.app.tabs;

import com.faendir.acra.model.App;
import com.faendir.acra.service.DataService;
import com.faendir.acra.ui.base.statistics.Statistics;
import com.faendir.acra.ui.view.app.AppView;
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
@SpringComponent
@Route(value = "statistics", layout = AppView.class)
public class StatisticsTab extends AppTab<Div> {
    @Autowired
    public StatisticsTab(@NonNull DataService dataService) {
        super(dataService);
        getContent().setSizeFull();
    }

    @Override
    void init(App app) {
        getContent().removeAll();
        getContent().add(new Statistics(app, null, getDataService()));
    }
}
