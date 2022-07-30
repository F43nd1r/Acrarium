package com.faendir.acra.ui.view.app.tabs

import com.faendir.acra.i18n.Messages
import com.faendir.acra.model.App
import com.faendir.acra.model.QReport
import com.faendir.acra.model.view.VInstallation
import com.faendir.acra.navigation.ParseAppParameter
import com.faendir.acra.navigation.View
import com.faendir.acra.service.AvatarService
import com.faendir.acra.service.DataService
import com.faendir.acra.settings.LocalSettings
import com.faendir.acra.ui.component.InstallationView
import com.faendir.acra.ui.component.grid.AcrariumGridView
import com.faendir.acra.ui.component.grid.TimeSpanRenderer
import com.faendir.acra.ui.component.grid.column
import com.faendir.acra.ui.component.grid.grid
import com.faendir.acra.ui.ext.content
import com.faendir.acra.ui.view.app.AppView
import com.faendir.acra.ui.view.installation.tabs.InstallationTab
import com.faendir.acra.ui.view.installation.tabs.ReportTab
import com.vaadin.flow.component.grid.Grid
import com.vaadin.flow.component.grid.GridSortOrder
import com.vaadin.flow.data.renderer.ComponentRenderer
import com.vaadin.flow.router.Route

@View
@Route(value = "installation", layout = AppView::class)
class InstallationTab(
    private val dataService: DataService,
    private val avatarService: AvatarService,
    private val localSettings: LocalSettings,
    @ParseAppParameter
    private val app: App
) : AppTab<AcrariumGridView<VInstallation>>(app) {
    init {
        content {
            grid {
                setSelectionMode(Grid.SelectionMode.NONE)
                column(ComponentRenderer { installation -> InstallationView(avatarService).apply { setInstallationId(installation.id) } }) {
                    setSortable(QReport.report.installationId)
                    setFilterable(QReport.report.installationId, Messages.INSTALLATION)
                    setCaption(Messages.INSTALLATION)
                    width = "50px"
                    isAutoWidth = false
                    flexGrow = 1
                }
                column({ it.reportCount }) {
                    setSortable(QReport.report.count())
                    setCaption(Messages.REPORTS)
                }
                column(TimeSpanRenderer { it.lastReport }) {
                    setSortable(QReport.report.date.max())
                    setCaption(Messages.LATEST_REPORT)
                    sort(GridSortOrder.desc(this).build())
                }
                addOnClickNavigation(ReportTab::class.java) { InstallationTab.getNavigationParams(app, it.id) }
            }
        }
    }

    override fun initContent() = AcrariumGridView(dataService.getInstallationProvider(app), localSettings::installationGridSettings)
}