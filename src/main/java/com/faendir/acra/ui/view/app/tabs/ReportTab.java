package com.faendir.acra.ui.view.app.tabs;

import com.faendir.acra.model.App;
import com.faendir.acra.service.AvatarService;
import com.faendir.acra.service.DataService;
import com.faendir.acra.ui.base.ReportList;
import com.faendir.acra.ui.view.app.AppView;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.spring.annotation.SpringComponent;
import com.vaadin.flow.spring.annotation.UIScope;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.lang.NonNull;

/**
 * @author lukas
 * @since 14.07.18
 */
@UIScope
@SpringComponent
@Route(value = "report", layout = AppView.class)
public class ReportTab extends AppTab<Div> {
    @NonNull private final AvatarService avatarService;

    @Autowired
    public ReportTab(@NonNull DataService dataService, @NonNull AvatarService avatarService) {
        super(dataService);
        this.avatarService = avatarService;
        getContent().setSizeFull();
    }

    @Override
    void init(App app) {
        getContent().removeAll();
        getContent().add(new ReportList(app, getDataService().getReportProvider(app), avatarService, getDataService()::delete));
    }
}
