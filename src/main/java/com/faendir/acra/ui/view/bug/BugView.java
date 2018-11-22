package com.faendir.acra.ui.view.bug;

import com.faendir.acra.i18n.Messages;
import com.faendir.acra.model.Bug;
import com.faendir.acra.ui.base.ActiveChildAware;
import com.faendir.acra.ui.base.ParentLayout;
import com.faendir.acra.ui.component.Tab;
import com.faendir.acra.ui.view.MainView;
import com.faendir.acra.ui.view.bug.tabs.AdminTab;
import com.faendir.acra.ui.view.bug.tabs.BugTab;
import com.faendir.acra.ui.view.bug.tabs.ReportTab;
import com.faendir.acra.ui.view.bug.tabs.StacktraceTab;
import com.faendir.acra.ui.view.bug.tabs.StatisticsTab;
import com.vaadin.flow.component.tabs.Tabs;
import com.vaadin.flow.router.RoutePrefix;
import com.vaadin.flow.spring.annotation.SpringComponent;
import com.vaadin.flow.spring.annotation.UIScope;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.stream.Stream;

/**
 * @author lukas
 * @since 08.09.18
 */
@UIScope
@SpringComponent
@RoutePrefix("bug")
@com.vaadin.flow.router.ParentLayout(MainView.class)
public class BugView extends ParentLayout implements ActiveChildAware<BugTab<?>, Bug> {
    private Bug bug;
    private final Tabs tabs;

    @Autowired
    public BugView() {
        tabs = new Tabs(Stream.of(TabDef.values()).map(def -> new Tab(def.labelId)).toArray(Tab[]::new));
        tabs.addSelectedChangeListener(e -> getUI().ifPresent(ui -> ui.navigate(TabDef.values()[e.getSource().getSelectedIndex()].tabClass, bug.getId())));
        setSizeFull();
        ParentLayout content = new ParentLayout();
        content.setWidthFull();
        expand(content);
        content.getStyle().set("overflow", "auto");
        setRouterRoot(content);
        getStyle().set("flex-direction", "column");
        removeAll();
        add(tabs, content);
    }

    @Override
    public void setActiveChild(BugTab<?> child, Bug parameter) {
        bug = parameter;
        Stream.of(TabDef.values()).filter(def -> def.tabClass.equals(child.getClass())).findAny().ifPresent(def -> tabs.setSelectedIndex(def.ordinal()));
    }

    private enum TabDef {
        REPORT(Messages.REPORTS, ReportTab.class),
        STACKTRACE(Messages.STACKTRACES, StacktraceTab.class),
        STATISTICS(Messages.STATISTICS, StatisticsTab.class),
        ADMIN(Messages.ADMIN, AdminTab.class);
        private String labelId;
        private Class<? extends BugTab<?>> tabClass;

        TabDef(String labelId, Class<? extends BugTab<?>> tabClass) {
            this.labelId = labelId;
            this.tabClass = tabClass;
        }
    }
}
