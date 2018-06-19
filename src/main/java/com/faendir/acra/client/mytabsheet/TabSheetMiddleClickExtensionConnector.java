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
package com.faendir.acra.client.mytabsheet;

import com.faendir.acra.ui.view.base.layout.MyTabSheet;
import com.google.gwt.dom.client.Element;
import com.google.gwt.dom.client.NativeEvent;
import com.google.gwt.event.dom.client.MouseDownEvent;
import com.google.gwt.regexp.shared.MatchResult;
import com.google.gwt.regexp.shared.RegExp;
import com.google.gwt.user.client.DOM;
import com.vaadin.client.MouseEventDetailsBuilder;
import com.vaadin.client.ServerConnector;
import com.vaadin.client.extensions.AbstractExtensionConnector;
import com.vaadin.client.ui.tabsheet.TabsheetConnector;
import com.vaadin.shared.MouseEventDetails;
import com.vaadin.shared.communication.ServerRpc;
import com.vaadin.shared.ui.Connect;

/**
 * @author lukas
 * @since 19.06.18
 */
@Connect(MyTabSheet.MiddleClickExtension.class)
public class TabSheetMiddleClickExtensionConnector extends AbstractExtensionConnector {
    @Override
    protected void extend(ServerConnector target) {
        getParent().getWidget().addDomHandler(event -> {
            if (event.getNativeButton() == NativeEvent.BUTTON_MIDDLE) {
                event.preventDefault();
                Element element = Element.as(event.getNativeEvent().getEventTarget());
                String id = getParent().getWidget().getSubPartName(DOM.asOld(element));
                if (id != null) {
                    MatchResult result = RegExp.compile("tab\\[(\\d)\\]").exec(id);
                    if (result != null) {
                        getRpcProxy(Rpc.class).middleClick(Integer.parseInt(result.getGroup(1)),
                                MouseEventDetailsBuilder.buildMouseEventDetails(event.getNativeEvent(), event.getRelativeElement()));
                    }
                }
            }
        }, MouseDownEvent.getType());
    }

    @Override
    public TabsheetConnector getParent() {
        return (TabsheetConnector) super.getParent();
    }

    public interface Rpc extends ServerRpc {
        void middleClick(int tabIndex, MouseEventDetails details);
    }
}
