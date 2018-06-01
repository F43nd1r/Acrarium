package com.faendir.acra.ui.view.bug;

import com.faendir.acra.model.App;
import com.faendir.acra.model.Bug;
import com.faendir.acra.model.Permission;
import com.faendir.acra.model.view.VBug;
import com.faendir.acra.service.data.DataService;
import com.faendir.acra.ui.annotation.RequiresAppPermission;
import com.faendir.acra.ui.navigation.MyNavigator;
import com.faendir.acra.ui.navigation.SingleParametrizedViewProvider;
import com.faendir.acra.ui.view.base.MyTabSheet;
import com.faendir.acra.ui.view.base.ParametrizedBaseView;
import com.faendir.acra.ui.view.bug.tabs.BugTab;
import com.faendir.acra.util.Style;
import com.vaadin.shared.ui.ContentMode;
import com.vaadin.spring.annotation.SpringComponent;
import com.vaadin.spring.annotation.UIScope;
import com.vaadin.spring.annotation.ViewScope;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.GridLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.Panel;
import com.vaadin.ui.VerticalLayout;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.data.util.Pair;
import org.springframework.lang.NonNull;
import org.xbib.time.pretty.PrettyTime;

import java.util.List;
import java.util.Locale;
import java.util.Optional;

/**
 * @author Lukas
 * @since 21.03.2018
 */
@SpringComponent
@ViewScope
@RequiresAppPermission(Permission.Level.VIEW)
public class BugView extends ParametrizedBaseView<Pair<VBug, String>> {
    private final List<BugTab> tabs;

    @Autowired
    public BugView(@NonNull @Lazy List<BugTab> tabs) {
        this.tabs = tabs;
    }

    @Override
    protected void enter(@NonNull Pair<VBug, String> parameter) {
        VBug bug = parameter.getFirst();
        GridLayout summaryGrid = new GridLayout(2, 1);
        Style.BORDERED_GRIDLAYOUT.apply(summaryGrid);
        summaryGrid.addComponents(new Label("Title", ContentMode.PREFORMATTED), new Label(bug.getBug().getTitle(), ContentMode.PREFORMATTED));
        summaryGrid.addComponents(new Label("Version", ContentMode.PREFORMATTED), new Label(String.valueOf(bug.getBug().getVersionCode()), ContentMode.PREFORMATTED));
        summaryGrid.addComponents(new Label("Last Report", ContentMode.PREFORMATTED), new Label(new PrettyTime(Locale.US).format(bug.getLastReport()), ContentMode.PREFORMATTED));
        summaryGrid.setDefaultComponentAlignment(Alignment.MIDDLE_LEFT);
        summaryGrid.setSizeFull();
        Panel summary = new Panel(summaryGrid);
        summary.setCaption("Summary");
        MyTabSheet<Bug> tabSheet = new MyTabSheet<>(bug.getBug(), getNavigationManager(), tabs);
        tabSheet.setSizeFull();
        tabSheet.addSelectedTabChangeListener(e -> getNavigationManager().updatePageParameters(bug.getBug().getId() + "/" + e.getTabSheet().getSelectedTab().getCaption()));
        if (tabSheet.getCaptions().contains(parameter.getSecond())) tabSheet.setInitialTab(parameter.getSecond());
        else tabSheet.setFirstTabAsInitialTab();
        VerticalLayout layout = new VerticalLayout(summary, tabSheet);
        layout.setSizeFull();
        layout.setExpandRatio(tabSheet, 1);
        Style.NO_PADDING.apply(layout);
        Panel root = new Panel(layout);
        root.setSizeFull();
        Style.apply(root, Style.NO_BACKGROUND, Style.NO_BORDER);
        setCompositionRoot(root);
        Style.apply(this, Style.PADDING_LEFT, Style.PADDING_RIGHT, Style.PADDING_BOTTOM);
    }

    @SpringComponent
    @UIScope
    public static class Provider extends SingleParametrizedViewProvider<Pair<VBug, String>, BugView> {
        @NonNull private final DataService dataService;

        @Autowired
        public Provider(@NonNull DataService dataService) {
            super(BugView.class);
            this.dataService = dataService;
        }

        @Override
        protected String getTitle(Pair<VBug, String> parameter) {
            return parameter.getFirst().getBug().getTitle();
        }

        @Override
        protected boolean isValidParameter(Pair<VBug, String> parameter) {
            return parameter != null;
        }

        @Override
        protected Pair<VBug, String> parseParameter(String parameter) {
            String[] parameters = parameter.split(MyNavigator.SEPARATOR);
            if (parameters.length > 0) {
                Optional<VBug> bug = dataService.findBug(parameters[0]);
                if (bug.isPresent()) {
                    return Pair.of(bug.get(), parameters.length == 1 ? "" : parameters[1]);
                }
            }
            return null;
        }

        @Override
        protected App toApp(Pair<VBug, String> parameter) {
            return parameter.getFirst().getBug().getApp();
        }

        @Override
        public String getId() {
            return "bug";
        }
    }
}
