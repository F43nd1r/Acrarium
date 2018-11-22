package com.faendir.acra.ui.view.bug.tabs;

import com.faendir.acra.model.Bug;
import com.faendir.acra.service.AvatarService;
import com.faendir.acra.service.DataService;
import com.faendir.acra.ui.base.ReportList;
import com.faendir.acra.ui.view.bug.BugView;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.spring.annotation.SpringComponent;
import com.vaadin.flow.spring.annotation.UIScope;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.lang.NonNull;

/**
 * @author lukas
 * @since 19.11.18
 */
@UIScope
@SpringComponent("bugReportTab")
@Route(value = "report", layout = BugView.class)
public class ReportTab extends BugTab<Div> {
    @NonNull
    private final AvatarService avatarService;

    @Autowired
    public ReportTab(@NonNull DataService dataService, @NonNull AvatarService avatarService) {
        super(dataService);
        this.avatarService = avatarService;
        getContent().setSizeFull();
    }

    @Override
    void init(Bug bug) {
        getContent().removeAll();
        getContent().add(new ReportList(bug.getApp(), getDataService().getReportProvider(bug), avatarService, getDataService()::delete));
    }
}
