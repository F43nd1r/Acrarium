package com.faendir.acra.ui.view.app.tabs;

import com.faendir.acra.model.App;
import com.faendir.acra.service.DataService;
import com.faendir.acra.ui.view.app.AppView;
import com.vaadin.flow.component.Text;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.spring.annotation.SpringComponent;
import com.vaadin.flow.spring.annotation.UIScope;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * @author lukas
 * @since 14.07.18
 */
@UIScope
@SpringComponent
@Route(value = "report", layout = AppView.class)
public class ReportTab extends AppTab<VerticalLayout> {

    @Autowired
    public ReportTab(DataService dataService) {
        super(dataService);
    }

    @Override
    void init(App app) {
        getContent().add(new Text("Hello2"));
    }
}
