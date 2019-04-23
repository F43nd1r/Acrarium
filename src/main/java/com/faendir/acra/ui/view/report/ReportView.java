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
import com.faendir.acra.model.Report;
import com.faendir.acra.service.AvatarService;
import com.faendir.acra.service.DataService;
import com.faendir.acra.ui.base.HasRoute;
import com.faendir.acra.ui.base.HasSecureStringParameter;
import com.faendir.acra.ui.base.Path;
import com.faendir.acra.ui.component.Card;
import com.faendir.acra.ui.component.CssGrid;
import com.faendir.acra.ui.component.HasSize;
import com.faendir.acra.ui.component.InstallationView;
import com.faendir.acra.ui.component.Label;
import com.faendir.acra.ui.component.Translatable;
import com.faendir.acra.ui.view.MainView;
import com.faendir.acra.ui.view.bug.tabs.ReportTab;
import com.faendir.acra.util.Utils;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.Composite;
import com.vaadin.flow.component.html.Anchor;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.router.BeforeEvent;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.server.InputStreamFactory;
import com.vaadin.flow.server.StreamResource;
import com.vaadin.flow.spring.annotation.SpringComponent;
import com.vaadin.flow.spring.annotation.UIScope;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.lang.NonNull;
import org.xbib.time.pretty.PrettyTime;

import java.sql.SQLException;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
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
    private final Label version;
    private final Label date;
    private final InstallationView installation;
    private final Label email;
    private final Label comment;
    private final Translatable<Label> mappedStacktraceLabel;
    private final Translatable<Label> unmappedStacktraceLabel;
    private final Label stacktrace;
    private final Div attachments;
    private final Card details;
    private Report report;
    private PrettyTime prettyTime;

    @Autowired
    public ReportView(@NonNull DataService dataService, @NonNull AvatarService avatarService) {
        this.dataService = dataService;
        prettyTime = new PrettyTime(Locale.US);
        getElement().getStyle().set("overflow", "auto");
        CssGrid summaryLayout = new CssGrid();
        summaryLayout.setTemplateColumns("auto auto");
        summaryLayout.setColumnGap(1, HasSize.Unit.EM);
        summaryLayout.setJustifyItems(CssGrid.JustifyMode.START);
        summaryLayout.setAlignItems(CssGrid.AlignMode.FIRST_BASELINE);
        Translatable<Label> installationLabel = Translatable.createLabel(Messages.USER).with(Label::secondary);
        version = new Label();
        date = new Label();
        installation = new InstallationView(avatarService);
        email = new Label();
        comment = new Label();
        mappedStacktraceLabel = Translatable.createLabel(Messages.DE_OBFUSCATED_STACKTRACE).with(Label::secondary);
        unmappedStacktraceLabel = Translatable.createLabel(Messages.NO_MAPPING_STACKTRACE).with(Label::secondary);
        stacktrace = new Label().honorWhitespaces();
        attachments = new Div();
        summaryLayout.add(Translatable.createLabel(Messages.VERSION).with(Label::secondary), version, Translatable.createLabel(Messages.DATE).with(Label::secondary), date, installationLabel, installation, Translatable.createLabel(Messages.EMAIL).with(Label::secondary), email, Translatable.createLabel(Messages.COMMENT).with(Label::secondary), comment, mappedStacktraceLabel, unmappedStacktraceLabel, stacktrace, Translatable.createLabel(Messages.ATTACHMENTS).with(Label::secondary));
        summaryLayout.alignItems(CssGrid.AlignMode.CENTER, installationLabel);
        Card summary = new Card(summaryLayout);
        summary.setHeader(Translatable.createText(Messages.SUMMARY));
        details = new Card();
        details.setHeader(Translatable.createText(Messages.DETAILS));
        getContent().add(summary, details);
    }

    @Override
    public void setParameterSecure(BeforeEvent event, String parameter) {
        Optional<Report> r = dataService.findReport(parameter);
        if (r.isPresent()) {
            report = r.get();
            version.setText(report.getStacktrace().getVersion().getName());
            date.setText(prettyTime.formatUnrounded(report.getDate().toLocalDateTime()));
            installation.setInstallationId(report.getInstallationId());
            email.setText(report.getUserEmail());
            comment.setText(report.getUserComment());
            Optional<String> mapping = Optional.ofNullable(report.getStacktrace().getVersion().getMappings());
            stacktrace.setText(mapping.map(m -> Utils.retrace(report.getStacktrace().getStacktrace(), m)).orElse(report.getStacktrace().getStacktrace()));
            mappedStacktraceLabel.getStyle().set("display", mapping.isPresent() ? null : "none");
            unmappedStacktraceLabel.getStyle().set("display", mapping.isPresent() ? "none" : null);
            attachments.removeAll();
            attachments.add(dataService.findAttachments(report).stream().map(attachment -> {
                Anchor anchor = new Anchor(new StreamResource(attachment.getFilename(), (InputStreamFactory) () -> {
                    try {
                        return attachment.getContent().getBinaryStream();
                    } catch (SQLException e) {
                        throw new RuntimeException(e); //TODO
                    }
                }), attachment.getFilename());
                anchor.getElement().setAttribute("download", true);
                return anchor;
            }).toArray(Component[]::new));
            details.removeAll();
            details.add(getLayoutForMap(report.getJsonObject().toMap()));
        } else {
            event.rerouteToError(IllegalArgumentException.class);
        }
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
                .forEach(entry -> layout.add(new Label(entry.getKey()).secondary(), getComponentForContent(entry.getValue())));
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
            String format = "%0" + ((int) Math.max(Math.log10(values.size() - 1), 0) + 1) + "d";
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
        return new Path.ParametrizedTextElement<>(getClass(), report.getId(), Messages.REPORT_FROM, prettyTime.formatUnrounded(report.getDate().toLocalDateTime()));
    }

    @Override
    public Parent<?> getLogicalParent() {
        return new ParametrizedParent<>(ReportTab.class, report.getStacktrace().getBug().getId());
    }
}
