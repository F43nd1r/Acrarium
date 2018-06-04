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

package com.faendir.acra.ui.view.report;

import com.diffplug.common.base.Errors;
import com.diffplug.common.base.Throwing;
import com.faendir.acra.model.App;
import com.faendir.acra.model.Attachment;
import com.faendir.acra.model.Permission;
import com.faendir.acra.model.ProguardMapping;
import com.faendir.acra.model.Report;
import com.faendir.acra.service.data.DataService;
import com.faendir.acra.ui.annotation.RequiresAppPermission;
import com.faendir.acra.ui.navigation.SingleParametrizedViewProvider;
import com.faendir.acra.ui.view.base.ParametrizedBaseView;
import com.faendir.acra.util.Style;
import com.faendir.acra.util.Utils;
import com.vaadin.server.FileDownloader;
import com.vaadin.server.StreamResource;
import com.vaadin.shared.ui.ContentMode;
import com.vaadin.spring.annotation.SpringComponent;
import com.vaadin.spring.annotation.UIScope;
import com.vaadin.spring.annotation.ViewScope;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Button;
import com.vaadin.ui.Component;
import com.vaadin.ui.GridLayout;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.Panel;
import com.vaadin.ui.VerticalLayout;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.lang.NonNull;

import java.io.InputStream;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Stream;

/**
 * @author Lukas
 * @since 13.05.2017
 */
@SpringComponent
@ViewScope
@RequiresAppPermission(Permission.Level.VIEW)
public class ReportView extends ParametrizedBaseView<Report> {
    @NonNull private final DataService dataService;

    @Autowired
    public ReportView(@NonNull DataService dataService) {
        this.dataService = dataService;
    }

    @Override
    protected void enter(@NonNull Report parameter) {
        HorizontalLayout attachments = new HorizontalLayout();
        for (Attachment file : dataService.findAttachments(parameter)) {
            Button button = new Button(file.getFilename());
            new FileDownloader(new StreamResource(new ExceptionAwareStreamSource(file.getContent()::getBinaryStream), file.getFilename())).extend(button);
            attachments.addComponent(button);
        }
        Style.apply(attachments, Style.MARGIN_BOTTOM, Style.MARGIN_TOP, Style.MARGIN_LEFT, Style.MARGIN_RIGHT);
        GridLayout summaryGrid = new GridLayout(2, 1);
        Style.BORDERED_GRIDLAYOUT.apply(summaryGrid);
        summaryGrid.addComponents(new Label("Version", ContentMode.PREFORMATTED), new Label(parameter.getVersionName(), ContentMode.PREFORMATTED));
        summaryGrid.addComponents(new Label("Email", ContentMode.PREFORMATTED), new Label(parameter.getUserEmail(), ContentMode.PREFORMATTED));
        summaryGrid.addComponents(new Label("Comment", ContentMode.PREFORMATTED), new Label(parameter.getUserComment(), ContentMode.PREFORMATTED));
        Optional<ProguardMapping> mapping = dataService.findMapping(parameter.getBug().getApp(), parameter.getVersionCode());
        if (mapping.isPresent()) {
            summaryGrid.addComponents(new Label("De-obfuscated Stacktrace", ContentMode.PREFORMATTED),
                    new Label(Utils.retrace(parameter.getStacktrace(), mapping.get().getMappings()), ContentMode.PREFORMATTED));
        } else {
            summaryGrid.addComponents(new Label("Stacktrace (No mapping found)", ContentMode.PREFORMATTED), new Label(parameter.getStacktrace(), ContentMode.PREFORMATTED));
        }
        summaryGrid.addComponents(new Label("Attachments", ContentMode.PREFORMATTED), attachments);
        summaryGrid.setDefaultComponentAlignment(Alignment.MIDDLE_LEFT);
        summaryGrid.setSizeFull();
        Panel summary = new Panel(summaryGrid);
        summary.setCaption("Summary");
        Panel details = new Panel(getLayoutForMap(parameter.getJsonObject().toMap()));
        details.setCaption("Details");
        VerticalLayout layout = new VerticalLayout(summary, details);
        layout.setSizeUndefined();
        layout.setExpandRatio(details, 1);
        Style.apply(layout, Style.NO_PADDING, Style.PADDING_LEFT, Style.PADDING_RIGHT, Style.PADDING_BOTTOM);
        Panel root = new Panel(layout);
        root.setSizeFull();
        Style.apply(root, Style.NO_BACKGROUND, Style.NO_BORDER);
        setCompositionRoot(root);
    }

    @NonNull
    private Stream<Component> getLayoutForEntry(@NonNull String key, @NonNull Object value) {
        return Stream.of(new Label(key, ContentMode.PREFORMATTED), getComponentForContent(value));
    }

    @NonNull
    private GridLayout getLayoutForMap(@NonNull Map<String, ?> map) {
        GridLayout layout = new GridLayout(2, 1, map.entrySet()
                .stream()
                .sorted(Comparator.comparing(Map.Entry::getKey))
                .flatMap(entry -> getLayoutForEntry(entry.getKey(), entry.getValue()))
                .toArray(Component[]::new));
        layout.setDefaultComponentAlignment(Alignment.MIDDLE_LEFT);
        layout.setSpacing(false);
        layout.setMargin(false);
        Style.BORDERED_GRIDLAYOUT.apply(layout);
        return layout;
    }

    @NonNull
    private Component getComponentForContent(@NonNull Object value) {
        if (value instanceof Map) {
            //noinspection unchecked
            return getLayoutForMap((Map<String, ?>) value);
        } else if (value instanceof List) {
            //noinspection unchecked
            List<Object> values = (List<Object>) value;
            Map<String, Object> map = new HashMap<>();
            for (int i = 0; i < values.size(); i++) {
                map.put(String.valueOf(i), values.get(0));
            }
            return getLayoutForMap(map);
        }
        return new Label(String.valueOf(value), ContentMode.PREFORMATTED);
    }

    private static class ExceptionAwareStreamSource implements StreamResource.StreamSource {
        private final Throwing.Supplier<InputStream> supplier;

        ExceptionAwareStreamSource(Throwing.Supplier<InputStream> supplier) {
            this.supplier = supplier;
        }

        @Override
        public InputStream getStream() {
            return Errors.log().getWithDefault(supplier, null);
        }
    }

    @SpringComponent
    @UIScope
    public static class Provider extends SingleParametrizedViewProvider<Report, ReportView> {
        @NonNull private final DataService dataService;

        @Autowired
        public Provider(@NonNull DataService dataService) {
            super(ReportView.class);
            this.dataService = dataService;
        }

        @Override
        protected String getTitle(Report parameter) {
            return parameter.getId();
        }

        @Override
        protected boolean isValidParameter(Report parameter) {
            return parameter != null;
        }

        @Override
        protected Report parseParameter(String parameter) {
            return dataService.findReport(parameter).orElse(null);
        }

        @Override
        protected App toApp(Report parameter) {
            return parameter.getBug().getApp();
        }

        @Override
        public String getId() {
            return "report";
        }
    }
}
