package com.faendir.acra.ui.base;

import com.faendir.acra.dataprovider.QueryDslDataProvider;
import com.faendir.acra.model.App;
import com.faendir.acra.model.Permission;
import com.faendir.acra.model.QReport;
import com.faendir.acra.model.Report;
import com.faendir.acra.security.SecurityUtils;
import com.faendir.acra.service.AvatarService;
import com.faendir.acra.ui.base.popup.Popup;
import com.faendir.acra.ui.view.report.ReportView;
import com.faendir.acra.util.TimeSpanRenderer;
import com.vaadin.flow.component.ClickEvent;
import com.vaadin.flow.component.ComponentEventListener;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.Label;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.data.renderer.ComponentRenderer;
import org.springframework.lang.NonNull;

import java.util.function.Consumer;

/**
 * @author lukas
 * @since 17.09.18
 */
public class ReportList extends MyGrid<Report>{
    public ReportList(@NonNull App app, @NonNull QueryDslDataProvider<Report> dataProvider, @NonNull AvatarService avatarService, @NonNull Consumer<Report> reportDeleter) {
        super(dataProvider);
        setSelectionMode(Grid.SelectionMode.NONE);
        addColumn(new ComponentRenderer<>(avatarService::getAvatar) , QReport.report.installationId, "User");
        addColumn(new TimeSpanRenderer<>(Report::getDate), QReport.report.date, "Date");
        addColumn(report -> report.getStacktrace().getVersion().getCode(), QReport.report.stacktrace.version.code, "App Version");
        addColumn(Report::getAndroidVersion, QReport.report.androidVersion, "Android Version");
        addColumn(Report::getPhoneModel, QReport.report.phoneModel, "Device");
        addColumn(report -> report.getStacktrace().getStacktrace().split("\n", 2)[0], QReport.report.stacktrace.stacktrace, "Stacktrace").setFlexGrow(1);
        if (SecurityUtils.hasPermission(app, Permission.Level.EDIT)) {
            addColumn(new ComponentRenderer<>(report -> new Button(new Icon(VaadinIcon.TRASH),
                    (ComponentEventListener<ClickEvent<Button>>) event -> new Popup().addComponent(new Label("Confirm Delete")).addYesNoButtons(p -> {
                        reportDeleter.accept(report);
                        getDataProvider().refreshAll();
                    }, true).show())), "");
        }
        addOnClickNavigation(ReportView.class, Report::getId);
    }
}
