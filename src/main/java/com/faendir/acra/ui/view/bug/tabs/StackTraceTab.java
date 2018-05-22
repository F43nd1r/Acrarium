package com.faendir.acra.ui.view.bug.tabs;

import com.faendir.acra.model.ProguardMapping;
import com.faendir.acra.model.view.VBug;
import com.faendir.acra.service.data.DataService;
import com.faendir.acra.ui.navigation.NavigationManager;
import com.faendir.acra.util.Utils;
import com.vaadin.shared.ui.ContentMode;
import com.vaadin.spring.annotation.SpringComponent;
import com.vaadin.spring.annotation.ViewScope;
import com.vaadin.ui.Accordion;
import com.vaadin.ui.Component;
import com.vaadin.ui.Label;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.lang.NonNull;

import java.util.Optional;

/**
 * @author Lukas
 * @since 21.03.2018
 */
@SpringComponent
@ViewScope
public class StackTraceTab implements BugTab {
    @NonNull private final DataService dataService;

    @Autowired
    public StackTraceTab(@NonNull DataService dataService) {
        this.dataService = dataService;
    }

    @Override
    public Component createContent(@NonNull VBug bug, @NonNull NavigationManager navigationManager) {
        Optional<ProguardMapping> mapping = dataService.findMapping(bug.getApp(), bug.getVersionCode());
        Accordion accordion = new Accordion();
        for (String stacktrace : bug.getStacktraces()) {
            if (mapping.isPresent()) {
                stacktrace = Utils.retrace(stacktrace, mapping.get().getMappings());
            }
            accordion.addTab(new Label(stacktrace, ContentMode.PREFORMATTED)).setCaption(stacktrace.split("\n", 2)[0]);
        }
        accordion.setSizeFull();
        return accordion;
    }

    @Override
    public String getCaption() {
        return "Stacktraces";
    }

    @Override
    public int getOrder() {
        return 1;
    }
}
