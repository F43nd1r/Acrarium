package com.faendir.acra.ui.view.bug.tabs;

import com.faendir.acra.sql.data.ProguardMappingRepository;
import com.faendir.acra.sql.model.Bug;
import com.faendir.acra.sql.model.ProguardMapping;
import com.faendir.acra.ui.NavigationManager;
import com.faendir.acra.ui.view.base.MyTabSheet;
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
public class StackTraceTab implements MyTabSheet.Tab<Bug>{
    @NonNull private final ProguardMappingRepository mappingRepository;

    @Autowired
    public StackTraceTab(@NonNull ProguardMappingRepository mappingRepository) {
        this.mappingRepository = mappingRepository;
    }

    @Override
    public Component createContent(@NonNull Bug bug, @NonNull NavigationManager navigationManager) {
        Optional<ProguardMapping> mapping = mappingRepository.findById(bug.getApp(), bug.getVersionCode());
        Accordion accordion = new Accordion();
        for (String stacktrace : bug.getStacktraces()){
            if(mapping.isPresent()){
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
