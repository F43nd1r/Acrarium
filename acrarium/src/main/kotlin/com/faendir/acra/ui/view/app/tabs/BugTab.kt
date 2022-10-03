/*
 * (C) Copyright 2018 Lukas Morawietz (https://github.com/F43nd1r)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.faendir.acra.ui.view.app.tabs

import com.faendir.acra.dataprovider.BugDataProvider
import com.faendir.acra.dataprovider.BugFilter
import com.faendir.acra.dataprovider.BugSort
import com.faendir.acra.i18n.Messages
import com.faendir.acra.model.App
import com.faendir.acra.model.Permission
import com.faendir.acra.model.Version
import com.faendir.acra.model.view.VBug
import com.faendir.acra.navigation.ParseAppParameter
import com.faendir.acra.navigation.View
import com.faendir.acra.security.SecurityUtils
import com.faendir.acra.service.BugMerger
import com.faendir.acra.service.DataService
import com.faendir.acra.settings.GridSettings
import com.faendir.acra.settings.LocalSettings
import com.faendir.acra.ui.component.Translatable
import com.faendir.acra.ui.component.dialog.createButton
import com.faendir.acra.ui.component.dialog.showFluentDialog
import com.faendir.acra.ui.component.grid.AcrariumGridView
import com.faendir.acra.ui.component.grid.BasicLayoutPersistingFilterableGrid
import com.faendir.acra.ui.component.grid.FilterableSortableLocalizedColumn
import com.faendir.acra.ui.component.grid.TimeSpanRenderer
import com.faendir.acra.ui.component.grid.column
import com.faendir.acra.ui.ext.content
import com.faendir.acra.ui.view.app.AppView
import com.faendir.acra.ui.view.bug.tabs.BugTab
import com.faendir.acra.ui.view.bug.tabs.ReportTab
import com.vaadin.flow.component.AbstractField.ComponentValueChangeEvent
import com.vaadin.flow.component.button.Button
import com.vaadin.flow.component.button.ButtonVariant
import com.vaadin.flow.component.grid.Grid
import com.vaadin.flow.component.grid.GridSortOrder
import com.vaadin.flow.component.notification.Notification
import com.vaadin.flow.component.radiobutton.RadioButtonGroup
import com.vaadin.flow.component.select.Select
import com.vaadin.flow.data.renderer.ComponentRenderer
import com.vaadin.flow.router.Route
import kotlin.reflect.KMutableProperty0

/**
 * @author lukas
 * @since 14.07.18
 */
@View
@Route(value = "bug", layout = AppView::class)
class BugTab(
    private val dataService: DataService,
    private val bugMerger: BugMerger,
    private val localSettings: LocalSettings,
    @ParseAppParameter
    private val app: App
) : AppTab<BugGridView>(app) {

    private val mergeButton: Translatable<Button>

    init {
        content {
            mergeButton = Translatable.createButton(Messages.MERGE_BUGS) {
                val selectedItems: List<VBug> = ArrayList(grid.selectedItems)
                if (selectedItems.size > 1) {
                    val titles = RadioButtonGroup<String>()
                    titles.setItems(selectedItems.map { bug: VBug -> bug.bug.title })
                    titles.value = selectedItems[0].bug.title
                    showFluentDialog {
                        header(Messages.CHOOSE_BUG_GROUP_TITLE)
                        add(titles)
                        createButton {
                            bugMerger.mergeBugs(selectedItems.map { it.bug }, titles.value)
                            grid.deselectAll()
                            grid.dataProvider.refreshAll()
                        }
                    }
                } else {
                    Notification.show(Messages.ONLY_ONE_BUG_SELECTED)
                }
            }.with {
                isEnabled = false
                removeThemeVariants(ButtonVariant.LUMO_PRIMARY)
            }
            header.addComponentAsFirst(mergeButton)
        }
    }

    override fun initContent() = BugGridView(dataService.getBugProvider(app), localSettings::bugGridSettings) {
        setSelectionMode(Grid.SelectionMode.MULTI)
        asMultiSelect().addSelectionListener { mergeButton.content.isEnabled = it.allSelectedItems.size >= 2 }
        column({ it.reportCount }) {
            setSortable(BugSort.REPORT_COUNT)
            setCaption(Messages.REPORTS)
        }
        column(TimeSpanRenderer { it.lastReport }) {
            setSortable(BugSort.MAX_REPORT_DATE)
            setCaption(Messages.LATEST_REPORT)
            sort(GridSortOrder.desc(this).build())
        }
        val versions = dataService.findAllVersions(app)
        val versionCodeNameMap = versions.associate { it.code to it.name }
        column({ versionCodeNameMap[it.highestVersionCode] }) {
            setSortable(BugSort.MAX_VERSION_CODE)
            setCaption(Messages.LATEST_VERSION)
        }
        column({ it.userCount }) {
            setSortable(BugSort.USER_COUNT)
            setCaption(Messages.AFFECTED_INSTALLATIONS)
        }
        column({ it.bug.title }) {
            setSortable(BugSort.TITLE)
            setFilterable({ BugFilter.TitleFilter(it) }, Messages.TITLE)
            setCaption(Messages.TITLE)
            isAutoWidth = false
            flexGrow = 1
        }
        column(ComponentRenderer { bug: VBug ->
            Select(*versions.toTypedArray()).apply {
                setTextRenderer { it.name }
                isEmptySelectionAllowed = true
                emptySelectionCaption = getTranslation(Messages.NOT_SOLVED)
                value = bug.bug.solvedVersion
                isEnabled = SecurityUtils.hasPermission(app, Permission.Level.EDIT)
                addValueChangeListener { e: ComponentValueChangeEvent<Select<Version?>?, Version?> ->
                    dataService.setBugSolved(bug.bug, e.value)
                    style["--select-background-color"] =
                        if (bug.highestVersionCode > (bug.bug.solvedVersion?.code ?: Int.MAX_VALUE)) "var(--lumo-error-color-50pct)" else null
                }
                if (bug.highestVersionCode > (bug.bug.solvedVersion?.code ?: Int.MAX_VALUE)) {
                    style["--select-background-color"] = "var(--lumo-error-color-50pct)"
                }
            }
        }) {
            setSortable(BugSort.SOLVED_VERSION)
            setFilterable(BugFilter.NotSolvedOrRegressionFilter, true, Messages.HIDE_SOLVED)
            setCaption(Messages.SOLVED)
        }
        addOnClickNavigation(ReportTab::class.java) { BugTab.getNavigationParams(it.bug) }
    }
}

class BugGridView(
    dataProvider: BugDataProvider,
    gridSettings: KMutableProperty0<GridSettings?>,
    initializer: BasicLayoutPersistingFilterableGrid<VBug, BugFilter, BugSort>.() -> Unit
) :
    AcrariumGridView<
            VBug,
            BugFilter,
            BugSort,
            FilterableSortableLocalizedColumn<VBug, BugFilter, BugSort>,
            BasicLayoutPersistingFilterableGrid<VBug, BugFilter, BugSort>>(
        BasicLayoutPersistingFilterableGrid(dataProvider, gridSettings.get()),
        gridSettings,
        initializer
    )