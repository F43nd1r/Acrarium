package com.faendir.acra.ui.view.tabs;

import com.faendir.acra.mongod.data.DataManager;
import com.faendir.acra.ui.NavigationManager;
import com.faendir.acra.ui.view.base.ReportList;
import org.jetbrains.annotations.NotNull;

/**
 * @author Lukas
 * @since 29.05.2017
 */
public class ReportTab extends ReportList {
    public ReportTab(@NotNull String app, @NotNull NavigationManager navigationManager, @NotNull DataManager dataManager) {
        super(app, navigationManager, dataManager, dataManager.getLazyReportsForApp(app), reportInfo -> reportInfo.getApp().equals(app));
    }
}
