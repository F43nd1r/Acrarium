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

import com.faendir.acra.i18n.Messages;
import com.faendir.acra.model.Bug;
import com.faendir.acra.model.Stacktrace;
import com.faendir.acra.service.DataService;
import com.faendir.acra.ui.component.Card;
import com.faendir.acra.ui.component.HasSize;
import com.faendir.acra.ui.component.Label;
import com.faendir.acra.ui.component.Translatable;
import com.faendir.acra.ui.view.bug.BugView;
import com.faendir.acra.util.Utils;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.spring.annotation.SpringComponent;
import com.vaadin.flow.spring.annotation.UIScope;

/**
 * @author lukas
 * @since 19.11.18
 */
@UIScope
@SpringComponent
@Route(value = "stacktrace", layout = BugView.class)
public class StacktraceTab extends BugTab<Div> implements HasSize {
    public StacktraceTab(DataService dataService) {
        super(dataService);
        setWidthFull();
        getStyle().set("overflow","auto");
    }

    @Override
    public void init(Bug bug) {
        getContent().removeAll();
        for (Stacktrace stacktrace : getDataService().getStacktraces(bug)) {
            String mapping = stacktrace.getVersion().getMappings();
            String trace = stacktrace.getStacktrace();
            if (mapping != null) {
                trace = Utils.retrace(trace, mapping);
            }
            Card card = new Card(new Label(trace).honorWhitespaces());
            card.setAllowCollapse(true);
            card.setHeader(Translatable.createLabel( Messages.STACKTRACE_TITLE, stacktrace.getVersion().getName(), trace.split("\n", 2)[0]));
            if(getContent().getChildren().findAny().isPresent()){
                card.collapse();
            }
            getContent().add(card);
        }
    }
}
