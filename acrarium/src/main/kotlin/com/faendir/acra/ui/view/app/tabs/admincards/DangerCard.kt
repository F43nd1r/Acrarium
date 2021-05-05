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
import com.faendir.acra.model.Permission
import com.faendir.acra.model.QReport
import com.faendir.acra.model.User
import com.faendir.acra.navigation.ParseAppParameter
import com.faendir.acra.navigation.View
import com.faendir.acra.security.HasApp
import com.faendir.acra.security.RequiresPermission
import com.faendir.acra.security.RequiresRole
import com.faendir.acra.service.DataService
import com.faendir.acra.ui.component.AdminCard
import com.faendir.acra.ui.component.Translatable
import com.faendir.acra.ui.component.dialog.closeButton
import com.faendir.acra.ui.component.dialog.confirmButtons
import com.faendir.acra.ui.component.dialog.showFluentDialog
import com.faendir.acra.ui.ext.box
import com.faendir.acra.ui.ext.comboBox
import com.faendir.acra.ui.ext.configurationLabel
import com.faendir.acra.ui.ext.content
import com.faendir.acra.ui.ext.translatableNumberField
import com.faendir.acra.ui.ext.translatableRangeField
import com.faendir.acra.ui.ext.translatableText
import com.faendir.acra.ui.view.Overview
import com.vaadin.flow.component.UI

@View
@RequiresPermission(Permission.Level.ADMIN)
class DangerCard(dataService: DataService, @ParseAppParameter override val app: App) : AdminCard(dataService), HasApp {
    init {
        content {
            setHeader(Translatable.createLabel(Messages.DANGER_ZONE))
            setHeaderColor("var(--lumo-error-contrast-color)", "var(--lumo-error-color)")
            dividerEnabled = true
            box(Messages.NEW_ACRA_CONFIG, Messages.NEW_ACRA_CONFIG_DETAILS, Messages.CREATE) {
                showFluentDialog {
                    translatableText(Messages.NEW_ACRA_CONFIG_CONFIRM)
                    confirmButtons {
                        showFluentDialog {
                            configurationLabel(dataService.recreateReporterUser(app))
                            closeButton()
                        }
                    }
                }
            }
            box(Messages.NEW_BUG_CONFIG, Messages.NEW_BUG_CONFIG_DETAILS, Messages.CONFIGURE) {
                showFluentDialog {
                    val score = translatableRangeField(Messages.SCORE) {
                        min = 0.0
                        max = 100.0
                        value = app.configuration.minScore.toDouble()
                    }
                    translatableText(Messages.NEW_BUG_CONFIG_CONFIRM)
                    confirmButtons {
                        dataService.changeConfiguration(app, App.Configuration(score.value.toInt()))
                    }
                }
            }
            box(Messages.PURGE_OLD, Messages.PURGE_OLD_DETAILS, Messages.PURGE) {
                showFluentDialog {
                    header(Messages.PURGE)
                    val age = translatableNumberField(Messages.REPORTS_OLDER_THAN1) {
                        value = 30.0
                        step = 1.0
                        min = 1.0
                        setHasControls(true)
                        setWidthFull()
                        suffixComponent = Translatable.createLabel(Messages.REPORTS_OLDER_THAN2)
                    }
                    confirmButtons {
                        dataService.deleteReportsOlderThanDays(app, age.value.toInt())
                    }
                }
            }
            box(Messages.PURGE_VERSION, Messages.PURGE_VERSION_DETAILS, Messages.PURGE) {
                showFluentDialog {
                    header(Messages.PURGE)
                    val versionBox = comboBox(dataService.getFromReports(app, QReport.report.stacktrace.version.code), Messages.REPORTS_BEFORE_VERSION)
                    confirmButtons {
                        if (versionBox.value != null) {
                            dataService.deleteReportsBeforeVersion(app, versionBox.value!!)
                        }
                    }
                }
            }
            box(Messages.DELETE_APP, Messages.DELETE_APP_DETAILS, Messages.DELETE) {
                showFluentDialog {
                    translatableText(Messages.DELETE_APP_CONFIRM)
                    confirmButtons {
                        dataService.deleteApp(app)
                        UI.getCurrent().navigate(Overview::class.java)
                    }
                }
            }
        }
    }
}