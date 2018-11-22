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

import com.faendir.acra.i18n.Messages;
import com.faendir.acra.model.ProguardMapping;
import com.faendir.acra.model.Report;
import com.faendir.acra.service.AvatarService;
import com.faendir.acra.service.DataService;
import com.faendir.acra.ui.base.HasRoute;
import com.faendir.acra.ui.base.HasSecureStringParameter;
import com.faendir.acra.ui.base.Path;
import com.faendir.acra.ui.component.Card;
import com.faendir.acra.ui.component.CssGrid;
import com.faendir.acra.ui.component.FlexLayout;
import com.faendir.acra.ui.component.HasSize;
import com.faendir.acra.ui.component.Label;
import com.faendir.acra.ui.component.Translatable;
import com.faendir.acra.ui.view.MainView;
import com.faendir.acra.ui.view.bug.tabs.ReportTab;
import com.faendir.acra.util.Utils;
import com.vaadin.flow.component.AttachEvent;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.Composite;
import com.vaadin.flow.component.Text;
import com.vaadin.flow.component.html.Anchor;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.router.BeforeEvent;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.server.InputStreamFactory;
import com.vaadin.flow.server.StreamResource;
import com.vaadin.flow.spring.annotation.SpringComponent;
import com.vaadin.flow.spring.annotation.UIScope;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.lang.NonNull;

import java.sql.SQLException;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * @author lukas
 * @since 17.09.18
 */
@UIScope
@SpringComponent
@Route(value = "report", layout = MainView.class)
public class ReportView extends Composite<Div> implements HasSecureStringParameter, HasRoute {
    private final DataService dataService;
    private final AvatarService avatarService;
    private Report report;

    @Autowired
    public ReportView(@NonNull DataService dataService, @NonNull AvatarService avatarService) {
        this.dataService = dataService;
        this.avatarService = avatarService;
        getElement().getStyle().set("overflow", "auto");
    }

    @Override
    public void setParameterSecure(BeforeEvent event, String parameter) {
        Optional<Report> r = dataService.findReport(parameter);
        if (r.isPresent()) {
            report = r.get();
        } else {
            event.rerouteToError(IllegalArgumentException.class);
        }
    }

    @Override
    protected void onAttach(AttachEvent attachEvent) {
        super.onAttach(attachEvent);
        getContent().removeAll();
        CssGrid summaryLayout = new CssGrid();
        summaryLayout.setTemplateColumns("auto auto");
        summaryLayout.setColumnGap(1, HasSize.Unit.EM);
        summaryLayout.setJustifyItems(CssGrid.JustifyMode.START);
        summaryLayout.setAlignItems(CssGrid.AlignMode.FIRST_BASELINE);
        summaryLayout.add(Translatable.createLabel(Messages.VERSION).with(Label::secondary), new Label(report.getStacktrace().getVersion().getName()));
        FlexLayout userLayout = new FlexLayout(avatarService.getAvatar(report), new Text(report.getInstallationId()));
        userLayout.setAlignItems(FlexComponent.Alignment.CENTER);
        Translatable<Label> userLabel = Translatable.createLabel(Messages.USER).with(Label::secondary);
        summaryLayout.alignItems(CssGrid.AlignMode.CENTER, userLabel);
        summaryLayout.add(userLabel, userLayout);
        summaryLayout.add(Translatable.createLabel(Messages.EMAIL).with(Label::secondary), new Label(report.getUserEmail()));
        summaryLayout.add(Translatable.createLabel(Messages.COMMENT).with(Label::secondary), new Label(report.getUserComment()));
        Optional<ProguardMapping> mapping = dataService.findMapping(report.getStacktrace().getBug().getApp(), report.getStacktrace().getVersion().getCode());
        Label stacktrace = new Label(mapping.map(m -> Utils.retrace(report.getStacktrace().getStacktrace(), m.getMappings())).orElse(report.getStacktrace().getStacktrace()));
        stacktrace.honorWhitespaces();
        summaryLayout.add(Translatable.createLabel(mapping.isPresent() ? Messages.DE_OBFUSCATED_STACKTRACE : Messages.NO_MAPPING_STACKTRACE).with(Label::secondary), stacktrace);
        summaryLayout.add(Translatable.createLabel(Messages.ATTACHMENTS).with(Label::secondary), new Div(dataService.findAttachments(report).stream().map(attachment -> {
            Anchor anchor = new Anchor(new StreamResource(attachment.getFilename(), (InputStreamFactory) () -> {
                try {
                    return attachment.getContent().getBinaryStream();
                } catch (SQLException e) {
                    throw new RuntimeException(e); //TODO
                }
            }), attachment.getFilename());
            anchor.getElement().setAttribute("download", true);
            return anchor;
        }).toArray(Component[]::new)));
        Card summaryCard = new Card(summaryLayout);
        summaryCard.setHeader(Translatable.createText(Messages.SUMMARY));
        getContent().add(summaryCard);

        Card detailCard = new Card(getLayoutForMap(report.getJsonObject().toMap()));
        detailCard.setHeader(Translatable.createText(Messages.DETAILS));
        getContent().add(detailCard);
    }

    @NonNull
    private Div getLayoutForMap(@NonNull Map<String, ?> map) {
        CssGrid layout = new CssGrid();
        layout.setTemplateColumns("auto auto");
        layout.setColumnGap(1, HasSize.Unit.EM);
        layout.setJustifyItems(CssGrid.JustifyMode.START);
        map.entrySet()
                .stream()
                .sorted(Comparator.comparing(Map.Entry::getKey))
                .forEach(entry -> {
                    layout.add(new Label(entry.getKey()).secondary(), getComponentForContent(entry.getValue()));
                });
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
            String format = "%0"+ ((int)Math.log10(values.size()-1)+1) + "d";
            for (int i = 0; i < values.size(); i++) {
                map.put(String.format(format, i), values.get(0));
            }
            return getLayoutForMap(map);
        }
        return new Label(String.valueOf(value)).honorWhitespaces();
    }

    @NonNull
    @Override
    public Path.Element<?> getPathElement() {
        return new Path.ParametrizedTextElement<>(getClass(), report.getId(), Messages.ONE_ARG, report.getId());
    }

    @Override
    public Parent<?> getLogicalParent() {
        return new ParametrizedParent<>(ReportTab.class, report.getStacktrace().getBug().getId());
    }
}
