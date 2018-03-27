package com.faendir.acra.ui.view.bug;

import com.faendir.acra.sql.data.BugRepository;
import com.faendir.acra.sql.model.App;
import com.faendir.acra.sql.model.Bug;
import com.faendir.acra.sql.model.Permission;
import com.faendir.acra.ui.annotation.RequiresAppPermission;
import com.faendir.acra.ui.view.base.MyTabSheet;
import com.faendir.acra.ui.view.base.ParametrizedBaseView;
import com.faendir.acra.ui.view.bug.tabs.BugTab;
import com.faendir.acra.ui.navigation.MyNavigator;
import com.faendir.acra.ui.navigation.SingleParametrizedViewProvider;
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
import org.ocpsoft.prettytime.PrettyTime;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Lazy;
import org.springframework.data.util.Pair;
import org.springframework.lang.NonNull;

import java.util.List;
import java.util.Optional;

/**
 * @author Lukas
 * @since 21.03.2018
 */
@SpringComponent
@ViewScope
@RequiresAppPermission(Permission.Level.VIEW)
public class BugView extends ParametrizedBaseView<Pair<Bug, String>> {
    private final List<BugTab> tabs;

    @Autowired
    public BugView(@Lazy List<BugTab> tabs) {
        this.tabs = tabs;
    }

    @Override
    protected void enter(@NonNull Pair<Bug, String> parameter) {
        Bug bug = parameter.getFirst();
        GridLayout summaryGrid = new GridLayout(2, 1);
        summaryGrid.addComponents(new Label("Title", ContentMode.PREFORMATTED), new Label(bug.getTitle(), ContentMode.PREFORMATTED));
        summaryGrid.addComponents(new Label("Version", ContentMode.PREFORMATTED), new Label(String.valueOf(bug.getVersionCode()), ContentMode.PREFORMATTED));
        summaryGrid.addComponents(new Label("Last Report", ContentMode.PREFORMATTED), new Label(new PrettyTime().format(bug.getLastReport()), ContentMode.PREFORMATTED));
        summaryGrid.setDefaultComponentAlignment(Alignment.MIDDLE_LEFT);
        summaryGrid.setSizeFull();
        Panel summary = new Panel(summaryGrid);
        summary.setCaption("Summary");
        MyTabSheet<Bug> tabSheet = new MyTabSheet<>(bug, getNavigationManager(), tabs);
        tabSheet.setSizeFull();
        tabSheet.addSelectedTabChangeListener(e -> getNavigationManager().updatePageParameters(bug.getId() + "/" + e.getTabSheet().getSelectedTab().getCaption()));
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
    public static class Provider extends SingleParametrizedViewProvider<Pair<Bug, String>, BugView> {
        @NonNull private final BugRepository bugRepository;
        @NonNull private final ApplicationContext applicationContext;

        @Autowired
        public Provider(@NonNull BugRepository bugRepository, @NonNull ApplicationContext applicationContext) {
            super(BugView.class);
            this.bugRepository = bugRepository;
            this.applicationContext = applicationContext;
        }

        @Override
        protected String getTitle(Pair<Bug, String> parameter) {
            String title = parameter.getFirst().getTitle();
            if (title.length() > 100) title = title.substring(0, 95) + " …";
            return title;
        }

        @Override
        protected boolean isValidParameter(Pair<Bug, String> parameter) {
            return parameter != null;
        }

        @Override
        protected Pair<Bug, String> parseParameter(String parameter) {
            try {
                String[] parameters = parameter.split(MyNavigator.SEPARATOR);
                if (parameters.length > 0) {
                    Optional<Bug> bug = bugRepository.findByIdEager(Integer.parseInt(parameters[0]));
                    if (bug.isPresent()) {
                        return Pair.of(bug.get(), parameters.length == 1 ? "" : parameters[1]);
                    }
                }
            } catch (IllegalArgumentException ignored) {
            }
            return null;
        }

        @Override
        protected App toApp(Pair<Bug, String> parameter) {
            return parameter.getFirst().getApp();
        }

        @Override
        public String getId() {
            return "bug";
        }
    }
}
