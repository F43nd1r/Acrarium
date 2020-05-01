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

package com.faendir.acra.ui.component.dialog;

import com.faendir.acra.i18n.Messages;
import com.faendir.acra.model.App;
import com.faendir.acra.model.Version;
import com.faendir.acra.service.DataService;
import com.faendir.acra.ui.component.FlexLayout;
import com.faendir.acra.ui.component.Translatable;
import com.faendir.acra.ui.component.UploadField;
import com.vaadin.flow.component.textfield.NumberField;
import com.vaadin.flow.component.textfield.TextField;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;

/**
 * @author lukas
 * @since 24.04.19
 */
public class VersionEditorDialog extends AcrariumDialog {
    public VersionEditorDialog(@NonNull DataService dataService, @NonNull App app, @Nullable Runnable onUpdate, @Nullable Version old) {
        boolean isNew = old == null;
        setHeader(isNew ? Messages.NEW_VERSION : Messages.EDIT_VERSION);
        Translatable.ValidatedValue<NumberField, ?, Double> code = Translatable.createNumberField(isNew ? dataService.getMaxVersion(app).map(i -> i + 1d).orElse(1d) : old.getCode(), Messages.VERSION_CODE).with(n -> {
            n.setStep(1d);
            n.setMin(1d);
            n.setPreventInvalidInput(true);
            n.setHasControls(true);
            n.setWidthFull();
            if (!isNew) {
                n.setEnabled(false);
            }
        });
        Translatable.ValidatedValue<TextField, ?, String> name = Translatable.createTextField(isNew ? "" : old.getName(), Messages.VERSION_NAME).with(n -> {
            if (isNew) {
                n.setRequired(true);
            }
        });
        Translatable.ValidatedValue<UploadField, ?, String> upload = Translatable.createUploadField(Messages.MAPPING_FILE).with(n -> {
            n.setWidthFull();
            if(!isNew) {
                n.setValue(old.getMappings());
            }
        });
        setPositive(e -> {
            if(isNew) {
                dataService.store(new Version(app, code.getValue().intValue(), name.getValue(), upload.getValue()));
            } else  {
                old.setName(name.getValue());
                old.setMappings(upload.getValue());
                dataService.store(old);
            }
            close();
            if(onUpdate != null) {
                onUpdate.run();
            }
        },  isNew ? Messages.CREATE : Messages.SAVE);
        setNegative(Messages.CANCEL);
        FlexLayout layout = new FlexLayout(code, name, upload);
        layout.setFlexDirection(FlexLayout.FlexDirection.COLUMN);
        add(layout);
    }
}
