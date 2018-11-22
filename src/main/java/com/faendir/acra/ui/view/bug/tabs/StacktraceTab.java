package com.faendir.acra.ui.view.bug.tabs;

import com.faendir.acra.i18n.Messages;
import com.faendir.acra.model.Bug;
import com.faendir.acra.model.ProguardMapping;
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

import java.util.Optional;

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
    void init(Bug bug) {
        getContent().removeAll();
        for (Stacktrace stacktrace : getDataService().getStacktraces(bug)) {
            Optional<ProguardMapping> mapping = getDataService().findMapping(bug.getApp(), stacktrace.getVersion().getCode());
            String trace = stacktrace.getStacktrace();
            if (mapping.isPresent()) {
                trace = Utils.retrace(trace, mapping.get().getMappings());
            }
            Card card = new Card(new Label(trace).honorWhitespaces());
            card.setAllowCollapse(true);
            card.setHeader(Translatable.createText( Messages.STACKTRACE_TITLE, stacktrace.getVersion().getName(), trace.split("\n", 2)[0]));
            if(getContent().getChildren().findAny().isPresent()){
                card.collapse();
            }
            getContent().add(card);
        }
    }
}
