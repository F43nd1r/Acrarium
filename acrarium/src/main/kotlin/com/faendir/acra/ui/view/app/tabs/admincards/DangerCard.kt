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
import com.faendir.acra.jooq.generated.Tables.REPORT
import com.faendir.acra.navigation.RouteParams
import com.faendir.acra.navigation.View
import com.faendir.acra.persistence.app.AppRepository
import com.faendir.acra.persistence.report.ReportRepository
import com.faendir.acra.persistence.user.Permission
import com.faendir.acra.security.RequiresPermission
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
import com.faendir.acra.ui.ext.translatableText
import com.faendir.acra.ui.view.Overview
import com.vaadin.flow.component.UI
import java.time.Instant
import java.time.temporal.ChronoUnit

@View
@RequiresPermission(Permission.Level.ADMIN)
class DangerCard(
    appRepository: AppRepository,
    reportRepository: ReportRepository,
    routeParams: RouteParams,
) : AdminCard() {
    private val appId = routeParams.appId()

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
                            configurationLabel(appRepository.recreateReporter(appId))
                            closeButton()
                        }
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
                        isStepButtonsVisible = true
                        setWidthFull()
                        suffixComponent = Translatable.createLabel(Messages.REPORTS_OLDER_THAN2)
                    }
                    confirmButtons {
                        reportRepository.deleteBefore(appId, Instant.now().minus(age.getValue().toLong(), ChronoUnit.DAYS))
                    }
                }
            }
            box(Messages.PURGE_VERSION, Messages.PURGE_VERSION_DETAILS, Messages.PURGE) {
                showFluentDialog {
                    header(Messages.PURGE)
                    val versionBox = comboBox(reportRepository.get(appId, REPORT.VERSION_CODE), Messages.REPORTS_BEFORE_VERSION)
                    confirmButtons {
                        if (versionBox.value != null) {
                            reportRepository.deleteBefore(appId, versionBox.value!!)
                        }
                    }
                }
            }
            box(Messages.DELETE_APP, Messages.DELETE_APP_DETAILS, Messages.DELETE) {
                showFluentDialog {
                    translatableText(Messages.DELETE_APP_CONFIRM)
                    confirmButtons {
                        appRepository.delete(appId)
                        UI.getCurrent().navigate(Overview::class.java)
                    }
                }
            }
        }
    }
}