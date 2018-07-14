package com.faendir.acra.ui.view.app;

import com.faendir.acra.ui.base.ParentLayout;
import com.faendir.acra.ui.view.MainView;
import com.faendir.acra.ui.view.app.tabs.AppTab;
import com.faendir.acra.ui.view.app.tabs.BugTab;
import com.faendir.acra.ui.view.app.tabs.ReportTab;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.tabs.Tab;
import com.vaadin.flow.component.tabs.Tabs;
import com.vaadin.flow.router.BeforeEnterEvent;
import com.vaadin.flow.router.BeforeEnterObserver;
import com.vaadin.flow.router.RoutePrefix;
import com.vaadin.flow.spring.annotation.SpringComponent;
import com.vaadin.flow.spring.annotation.UIScope;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;
import java.util.stream.Stream;

/**
 * @author lukas
 * @since 13.07.18
 */
@UIScope
@SpringComponent
@RoutePrefix("app")
@com.vaadin.flow.router.ParentLayout(MainView.class)
public class AppView extends ParentLayout implements BeforeEnterObserver {
    private int appId;

    @Autowired
    public AppView() {
        Tabs tabs = new Tabs(Stream.of(TabDef.values()).map(def -> new Tab(def.label)).toArray(Tab[]::new));
        tabs.addSelectedChangeListener(e -> getUI().ifPresent(ui -> ui.navigate(TabDef.values()[e.getSource().getSelectedIndex()].tabClass, appId)));
        ParentLayout content = new ParentLayout();
        setRouterRoot(content);
        setContent(new VerticalLayout(tabs, content));
    }

    @Override
    public void beforeEnter(BeforeEnterEvent event) {
        List<String> segments = event.getLocation().getSegments();
        try{
            appId = Integer.parseInt(segments.get(segments.size() -1));
        }catch (NumberFormatException ignored){
        }
    }

    private enum TabDef {
        BUG("Bug", BugTab.class),
        REPORT("Report", ReportTab.class);
        private String label;
        private Class<? extends AppTab<?>> tabClass;

        TabDef(String label, Class<? extends AppTab<?>> tabClass) {
            this.label = label;
            this.tabClass = tabClass;
        }
    }
}
