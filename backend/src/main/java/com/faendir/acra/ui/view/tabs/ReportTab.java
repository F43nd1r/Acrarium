package com.faendir.acra.ui.view.tabs;

import com.faendir.acra.mongod.data.DataManager;
import com.faendir.acra.ui.NavigationManager;
import com.faendir.acra.ui.view.base.ReportList;

/**
 * @author Lukas
 * @since 29.05.2017
 */
public class ReportTab extends ReportList {
    public ReportTab(String app, NavigationManager navigationManager, DataManager dataManager) {
        super(app, navigationManager, dataManager, () -> dataManager.getReportsForApp(app));
    }
}
