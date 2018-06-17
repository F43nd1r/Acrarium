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

package com.faendir.acra.client.mygrid;

import com.faendir.acra.ui.view.base.layout.MyGrid;
import com.google.gwt.dom.client.NativeEvent;
import com.google.gwt.event.dom.client.MouseDownEvent;
import com.vaadin.client.MouseEventDetailsBuilder;
import com.vaadin.client.ServerConnector;
import com.vaadin.client.connectors.grid.GridConnector;
import com.vaadin.client.extensions.AbstractExtensionConnector;
import com.vaadin.client.widget.grid.CellReference;
import com.vaadin.shared.MouseEventDetails;
import com.vaadin.shared.communication.ServerRpc;
import com.vaadin.shared.data.DataCommunicatorConstants;
import com.vaadin.shared.ui.Connect;
import elemental.json.JsonObject;

/**
 * @author Lukas
 * @since 15.01.2018
 */
@Connect(MyGrid.MiddleClickExtension.class)
public class MiddleClickGridExtensionConnector extends AbstractExtensionConnector {
    @Override
    protected void extend(ServerConnector target) {
        getParent().getWidget().addDomHandler(event -> {
            if (event.getNativeButton() == NativeEvent.BUTTON_MIDDLE) {
                event.preventDefault();
                CellReference<JsonObject> cell = getParent().getWidget().getEventCell();
                getRpcProxy(Rpc.class).middleClick(cell.getRowIndex(), cell.getRow().getString(DataCommunicatorConstants.KEY), getParent().getColumnId(cell.getColumn()),
                        MouseEventDetailsBuilder.buildMouseEventDetails(event.getNativeEvent(), event.getRelativeElement()));
            }
        }, MouseDownEvent.getType());
    }

    @Override
    public GridConnector getParent() {
        return (GridConnector) super.getParent();
    }

    public interface Rpc extends ServerRpc {
        void middleClick(int rowIndex, String rowKey, String columnInternalId, MouseEventDetails details);
    }
}
