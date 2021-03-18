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
package com.faendir.acra.ui.component.dialog

import com.faendir.acra.i18n.Messages
import com.faendir.acra.model.App
import com.faendir.acra.model.Version
import com.faendir.acra.service.DataService
import com.faendir.acra.ui.component.Translatable
import com.vaadin.flow.component.orderedlayout.FlexLayout

/**
 * @author lukas
 * @since 24.04.19
 */
class VersionEditorDialog(dataService: DataService, app: App, onUpdate: (() -> Unit) = {}, old: Version? = null) : AcrariumDialog() {

    init {
        setHeader(if (old == null) Messages.NEW_VERSION else Messages.EDIT_VERSION)
        val code = Translatable.createNumberField(Messages.VERSION_CODE).with {
            value = old?.code?.toDouble() ?: dataService.getMaxVersion(app)?.plus(1.0) ?: 1.0
            step = 1.0
            min = 1.0
            setHasControls(true)
            setWidthFull()
            if (old != null) isEnabled = false
        }
        val name = Translatable.createTextField(Messages.VERSION_NAME).with { if (old == null) isRequired = true else value = old.name }
        val upload = Translatable.createUploadField(Messages.MAPPING_FILE).with {
            setWidthFull()
            if (old != null) { value = old.mappings }
        }
        setPositive(if (old == null) Messages.CREATE else Messages.SAVE) {
            if (old == null) {
                dataService.storeVersion(Version(app, code.value.toInt(), name.value, upload.value))
            } else {
                old.name = name.value
                old.mappings = upload.value
                dataService.storeVersion(old)
            }
            close()
            onUpdate.invoke()
        }
        setNegative(Messages.CANCEL)
        val layout = FlexLayout(code, name, upload)
        layout.setFlexDirection(FlexLayout.FlexDirection.COLUMN)
        add(layout)
    }
}