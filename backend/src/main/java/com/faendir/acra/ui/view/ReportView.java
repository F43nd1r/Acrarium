package com.faendir.acra.ui.view;

import com.faendir.acra.data.AttachmentManager;
import com.faendir.acra.data.MappingManager;
import com.faendir.acra.data.Report;
import com.faendir.acra.data.ReportManager;
import com.faendir.acra.util.Style;
import com.mongodb.gridfs.GridFSDBFile;
import com.vaadin.navigator.ViewChangeListener;
import com.vaadin.server.FileDownloader;
import com.vaadin.server.StreamResource;
import com.vaadin.shared.ui.ContentMode;
import com.vaadin.spring.annotation.UIScope;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Button;
import com.vaadin.ui.Component;
import com.vaadin.ui.GridLayout;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.Panel;
import com.vaadin.ui.VerticalLayout;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

/**
 * @author Lukas
 * @since 13.05.2017
 */
@UIScope
@org.springframework.stereotype.Component
public class ReportView extends NamedView {

    private final ReportManager reportManager;
    private final AttachmentManager attachmentManager;
    private final MappingManager mappingManager;

    @Autowired
    public ReportView(ReportManager reportManager, AttachmentManager attachmentManager, MappingManager mappingManager) {
        this.reportManager = reportManager;
        this.attachmentManager = attachmentManager;
        this.mappingManager = mappingManager;
    }

    private Stream<Component> getLayoutForEntry(String key, Object value) {
        return Stream.of(new Label(key, ContentMode.PREFORMATTED), getComponentForContent(value));
    }

    private GridLayout getLayoutForMap(Map<String, ?> map) {
        GridLayout layout = new GridLayout(2, 1, map.entrySet().stream().sorted(Comparator.comparing(Map.Entry::getKey)).flatMap(entry -> getLayoutForEntry(entry.getKey(), entry.getValue())).toArray(Component[]::new));
        layout.setDefaultComponentAlignment(Alignment.MIDDLE_LEFT);
        layout.setSpacing(false);
        layout.setMargin(false);
        return layout;
    }

    private Component getComponentForContent(Object value) {
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
        return new Label(value.toString(), ContentMode.PREFORMATTED);
    }

    @Override
    public String getName() {
        return "report";
    }

    @Override
    public void enter(ViewChangeListener.ViewChangeEvent event) {
        Report report = reportManager.getReport(event.getParameters());
        List<GridFSDBFile> attachmentList = attachmentManager.getAttachments(report.getId());
        HorizontalLayout attachments = new HorizontalLayout(attachmentList.stream().map(file -> {
            Button button = new Button(file.getFilename());
            new FileDownloader(new StreamResource(file::getInputStream, file.getFilename())).extend(button);
            return button;
        }).toArray(Component[]::new));
        GridLayout summaryGrid = new GridLayout(2, 1);
        summaryGrid.addComponents(new Label("Version", ContentMode.PREFORMATTED), new Label(report.getVersionName(), ContentMode.PREFORMATTED));
        summaryGrid.addComponents(new Label("Email", ContentMode.PREFORMATTED), new Label(report.getUserEmail(), ContentMode.PREFORMATTED));
        summaryGrid.addComponents(new Label("Comment", ContentMode.PREFORMATTED), new Label(report.getUserComment(), ContentMode.PREFORMATTED));
        summaryGrid.addComponents(new Label("De-obfuscated Stacktrace", ContentMode.PREFORMATTED), new Label(report.getDeObfuscatedStacktrace(mappingManager), ContentMode.PREFORMATTED));
        summaryGrid.addComponents(new Label("Attachments", ContentMode.PREFORMATTED), attachments);
        summaryGrid.setDefaultComponentAlignment(Alignment.MIDDLE_LEFT);
        summaryGrid.setSizeFull();
        Style.apply(attachments, Style.MARGIN_BOTTOM, Style.MARGIN_TOP, Style.MARGIN_LEFT, Style.MARGIN_RIGHT);
        Panel summary = new Panel(summaryGrid);
        summary.setCaption("Summary");
        Panel panel = new Panel(getLayoutForMap(report.getContent().toMap()));
        panel.setCaption("Details");
        Style.apply(this, Style.PADDING_LEFT, Style.PADDING_RIGHT, Style.PADDING_BOTTOM);
        VerticalLayout layout = new VerticalLayout(summary, panel);
        layout.setExpandRatio(panel, 1);
        Style.NO_PADDING.apply(layout);
        Panel root = new Panel(layout);
        root.setSizeFull();
        setCompositionRoot(root);
        setSizeFull();
    }
}
