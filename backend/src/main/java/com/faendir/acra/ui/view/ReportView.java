package com.faendir.acra.ui.view;

import com.faendir.acra.data.Report;
import com.faendir.acra.data.ReportManager;
import com.faendir.acra.util.Style;
import com.vaadin.navigator.ViewChangeListener;
import com.vaadin.shared.ui.ContentMode;
import com.vaadin.spring.annotation.UIScope;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Component;
import com.vaadin.ui.GridLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.Panel;
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

    @Autowired
    public ReportView(ReportManager reportManager) {
        this.reportManager = reportManager;
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
        Component content = getLayoutForMap(report.getContent().toMap());
        Panel panel = new Panel(content);
        panel.setSizeFull();
        Style.apply(this, Style.PADDING_LEFT, Style.PADDING_RIGHT, Style.PADDING_BOTTOM);
        setCompositionRoot(panel);
        setSizeFull();
    }
}
