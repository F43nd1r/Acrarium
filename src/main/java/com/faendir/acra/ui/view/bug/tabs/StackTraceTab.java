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
package com.faendir.acra.ui.view.bug.tabs;

import com.faendir.acra.i18n.I18nAccordion;
import com.faendir.acra.i18n.Messages;
import com.faendir.acra.model.Bug;
import com.faendir.acra.model.ProguardMapping;
import com.faendir.acra.model.Stacktrace;
import com.faendir.acra.service.DataService;
import com.faendir.acra.ui.navigation.NavigationManager;
import com.faendir.acra.util.Utils;
import com.vaadin.shared.ui.ContentMode;
import com.vaadin.spring.annotation.SpringComponent;
import com.vaadin.spring.annotation.ViewScope;
import com.vaadin.ui.Component;
import com.vaadin.ui.Label;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.lang.NonNull;
import org.vaadin.spring.i18n.I18N;

import java.util.Optional;

/**
 * @author Lukas
 * @since 21.03.2018
 */
@SpringComponent
@ViewScope
public class StackTraceTab implements BugTab {
    @NonNull private final DataService dataService;
    @NonNull private final I18N i18n;

    @Autowired
    public StackTraceTab(@NonNull DataService dataService, @NonNull I18N i18n) {
        this.dataService = dataService;
        this.i18n = i18n;
    }

    @Override
    public Component createContent(@NonNull Bug bug, @NonNull NavigationManager navigationManager) {
        I18nAccordion accordion = new I18nAccordion(i18n);
        for (Stacktrace stacktrace : dataService.getStacktraces(bug)) {
            Optional<ProguardMapping> mapping = dataService.findMapping(bug.getApp(), stacktrace.getVersion().getCode());
            String trace = stacktrace.getStacktrace();
            if (mapping.isPresent()) {
                trace = Utils.retrace(trace, mapping.get().getMappings());
            }
            accordion.addTab(new Label(trace, ContentMode.PREFORMATTED), Messages.STACKTRACE_TITLE, stacktrace.getVersion().getName(), trace.split("\n", 2)[0]);
        }
        accordion.setSizeFull();
        return accordion;
    }

    @Override
    public String getCaption() {
        return i18n.get(Messages.STACKTRACES);
    }

    @Override
    public String getId() {
        return "stacktrace";
    }

    @Override
    public int getOrder() {
        return 1;
    }
}
