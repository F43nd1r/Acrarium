package com.faendir.acra.ui.view.tabs;

import com.faendir.acra.sql.data.DataManager;
import com.faendir.acra.sql.model.App;
import com.faendir.acra.ui.NavigationManager;
import com.faendir.acra.ui.view.base.MyTabSheet;
import com.faendir.acra.ui.view.base.ReportList;
import com.vaadin.ui.Component;
import org.springframework.lang.NonNull;

/**
 * @author Lukas
 * @since 29.05.2017
 */
public class ReportTab implements MyTabSheet.Tab {
    public ReportTab() {
    }

    @Override
    public Component createContent(@NonNull App app, @NonNull DataManager dataManager, @NonNull NavigationManager navigationManager) {
        Component content = new ReportList(app, navigationManager, dataManager, dataManager.lazyGetReportsForApp(app));
        content.setSizeFull();
        return content;
    }

    @Override
    public String getCaption() {
        return ReportList.CAPTION;
    }
}
