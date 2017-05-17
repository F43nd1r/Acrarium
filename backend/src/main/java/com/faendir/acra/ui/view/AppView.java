package com.faendir.acra.ui.view;

import com.faendir.acra.data.App;
import com.faendir.acra.data.AppManager;
import com.faendir.acra.data.ReportManager;
import com.faendir.acra.util.Style;
import com.vaadin.data.ValueProvider;
import com.vaadin.navigator.ViewChangeListener;
import com.vaadin.shared.ui.ContentMode;
import com.vaadin.spring.annotation.UIScope;
import com.vaadin.ui.Grid;
import com.vaadin.ui.Label;
import com.vaadin.ui.TabSheet;
import com.vaadin.ui.UI;
import com.vaadin.ui.VerticalLayout;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * @author Lukas
 * @since 13.05.2017
 */
@UIScope
@Component
public class AppView extends NamedView {

    private final AppManager appManager;
    private final ReportManager reportManager;

    @Autowired
    public AppView(AppManager appManager, ReportManager reportManager) {
        this.appManager = appManager;
        this.reportManager = reportManager;
    }

    private <T> Grid.Column addColumn(Grid<T> grid, ValueProvider<T, String> valueProvider, String caption) {
        Grid.Column column = grid.addColumn(valueProvider);
        column.setId(caption);
        column.setCaption(caption);
        return column;
    }

    @Override
    public String getName() {
        return "app";
    }

    @Override
    public void enter(ViewChangeListener.ViewChangeEvent event) {
        App app = appManager.getApp(event.getParameters());
        VerticalLayout statistics = new VerticalLayout(new Label("Coming soon"));
        statistics.setCaption("Statistics");
        statistics.setSizeFull();
        String location = UI.getCurrent().getPage().getLocation().toASCIIString();
        location = location.substring(0, location.indexOf('#'));
        VerticalLayout properties = new VerticalLayout(new Label(String.format("Required ACRA configuration:<br><code>formUri = \"%sreport\",<br>" +
                        "formUriBasicAuthLogin = \"%s\",<br>formUriBasicAuthPassword = \"%s\",<br>httpMethod = HttpSender.Method.POST,<br>reportType = HttpSender.Type.JSON</code>",
                location, app.getId(), app.getPassword()), ContentMode.HTML));
        properties.setCaption("Properties");
        properties.setSizeFull();
        TabSheet tabSheet = new TabSheet(new BugTab(app.getId(), getNavigationManager(), reportManager), new ReportList(app.getId(), getNavigationManager(), reportManager), statistics, properties);
        tabSheet.setSizeFull();
        VerticalLayout content = new VerticalLayout(tabSheet);
        content.setSizeFull();
        Style.apply(content, Style.NO_PADDING, Style.PADDING_LEFT, Style.PADDING_RIGHT, Style.PADDING_BOTTOM);
        setCompositionRoot(content);
        setSizeFull();
    }
}
