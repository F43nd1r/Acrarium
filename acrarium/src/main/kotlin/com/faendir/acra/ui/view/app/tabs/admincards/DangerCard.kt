/*
 * (C) Copyright 2019 Lukas Morawietz (https://github.com/F43nd1r)
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
package com.faendir.acra.ui.view.app.tabs.admincards

import com.faendir.acra.i18n.Messages
import com.faendir.acra.model.App
import com.faendir.acra.model.QReport
import com.faendir.acra.service.DataService
import com.faendir.acra.ui.component.Box
import com.faendir.acra.ui.component.ConfigurationLabel
import com.faendir.acra.ui.component.Translatable
import com.faendir.acra.ui.component.dialog.FluentDialog
import com.faendir.acra.ui.view.Overview
import com.vaadin.flow.component.UI
import com.vaadin.flow.spring.annotation.SpringComponent
import com.vaadin.flow.spring.annotation.UIScope
import org.springframework.beans.factory.annotation.Value

@UIScope
@SpringComponent
class DangerCard(dataService: DataService) : AdminCard(dataService) {

    @Value("\${server.context-path}")
    private val baseUrl: String? = null

    init {
        setHeader(Translatable.createLabel(Messages.DANGER_ZONE))
        enableDivider()
        setHeaderColor("var(--lumo-error-contrast-color)", "var(--lumo-error-color)")
    }

    override fun init(app: App) {
        removeContent()
        val configBox = Box(Translatable.createLabel(Messages.NEW_ACRA_CONFIG), Translatable.createLabel(Messages.NEW_ACRA_CONFIG_DETAILS), Translatable.createButton(Messages.CREATE) {
            FluentDialog().addText(Messages.NEW_ACRA_CONFIG_CONFIRM)
                    .addConfirmButtons { FluentDialog().addComponent(ConfigurationLabel(baseUrl, dataService.recreateReporterUser(app))).addCloseButton().show() }
                    .show()
        })
        val matchingBox = Box(Translatable.createLabel(Messages.NEW_BUG_CONFIG), Translatable.createLabel(Messages.NEW_BUG_CONFIG_DETAILS), Translatable.createButton(Messages.CONFIGURE) {
            val score = Translatable.createRangeField(Messages.SCORE).with {
                min = 0.0
                max = 100.0
                value = app.configuration.minScore.toDouble()
            }
            FluentDialog().addComponent(score)
                    .addText(Messages.NEW_BUG_CONFIG_CONFIRM)
                    .addConfirmButtons { dataService.changeConfiguration(app, App.Configuration(score.value.toInt())) }
                    .show()
        })
        val purgeAgeBox = Box(Translatable.createLabel(Messages.PURGE_OLD), Translatable.createLabel(Messages.PURGE_OLD_DETAILS), Translatable.createButton(Messages.PURGE) {
            val age = Translatable.createNumberField(Messages.REPORTS_OLDER_THAN1).with {
                value = 30.0
                step = 1.0
                min = 1.0
                setHasControls(true)
                setWidthFull()
                suffixComponent = Translatable.createLabel(Messages.REPORTS_OLDER_THAN2)
            }
            FluentDialog().addComponent(age)
                    .setTitle(Messages.PURGE)
                    .addConfirmButtons { dataService.deleteReportsOlderThanDays(app, age.value.toInt()) }.show()
        })
        val purgeVersionBox = Box(Translatable.createLabel(Messages.PURGE_VERSION), Translatable.createLabel(Messages.PURGE_VERSION_DETAILS), Translatable.createButton(Messages.PURGE) {
            val versionBox = Translatable.createComboBox(dataService.getFromReports(app, null, QReport.report.stacktrace.version.code), Messages.REPORTS_BEFORE_VERSION)
            FluentDialog().addComponent(versionBox)
                    .setTitle(Messages.PURGE)
                    .addConfirmButtons {
                        if (versionBox.value != null) {
                            dataService.deleteReportsBeforeVersion(app, versionBox.value!!)
                        }
                    }.show()
        })
        val deleteBox = Box(Translatable.createLabel(Messages.DELETE_APP), Translatable.createLabel(Messages.DELETE_APP_DETAILS), Translatable.createButton(Messages.DELETE) {
            FluentDialog().addText(Messages.DELETE_APP_CONFIRM).addConfirmButtons {
                dataService.deleteApp(app)
                UI.getCurrent().navigate(Overview::class.java)
            }.show()
        })
        add(configBox, matchingBox, purgeAgeBox, purgeVersionBox, deleteBox)
    }
}