package com.faendir.acra.ui.view.bug;

import com.faendir.acra.sql.data.BugRepository;
import com.faendir.acra.sql.model.Bug;
import com.faendir.acra.sql.model.Permission;
import com.faendir.acra.ui.annotation.RequiresAppPermission;
import com.faendir.acra.ui.view.base.MyTabSheet;
import com.faendir.acra.ui.view.base.ParametrizedNamedView;
import com.faendir.acra.ui.view.bug.tabs.ReportTab;
import com.faendir.acra.ui.view.bug.tabs.StackTraceTab;
import com.faendir.acra.util.Style;
import com.vaadin.shared.ui.ContentMode;
import com.vaadin.spring.annotation.SpringView;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.GridLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.Panel;
import com.vaadin.ui.VerticalLayout;
import org.ocpsoft.prettytime.PrettyTime;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;

import java.util.Optional;

/**
 * @author Lukas
 * @since 21.03.2018
 */
@SpringView(name = "bug")
@RequiresAppPermission(Permission.Level.VIEW)
public class BugView extends ParametrizedNamedView<Bug> {
    @NonNull private final BugRepository bugRepository;
    @NonNull private final ApplicationContext applicationContext;
    private MyTabSheet<Bug> tabSheet;

    @Autowired
    public BugView(@NonNull BugRepository bugRepository, @NonNull ApplicationContext applicationContext) {
        super(Bug::getApp);
        this.bugRepository = bugRepository;
        this.applicationContext = applicationContext;
    }

    @Nullable
    @Override
    protected Bug validateAndParseFragment(@NonNull String fragment) {
        try {
            String[] parameters = fragment.split("/");
            if (parameters.length > 0) {
                Optional<Bug> bugOptional = bugRepository.findByIdEager(Integer.parseInt(parameters[0]));
                if (bugOptional.isPresent()) {
                    Bug bug = bugOptional.get();
                    tabSheet = new MyTabSheet<>(bug, getNavigationManager(), applicationContext.getBean(ReportTab.class), applicationContext.getBean(StackTraceTab.class));
                    if (parameters.length == 1) {
                        tabSheet.setInitialTab(tabSheet.getCaptions().get(0));
                    } else if (tabSheet.getCaptions().contains(parameters[1])) {
                        tabSheet.setInitialTab(parameters[1]);
                    }
                    return bug;
                }
            }
        } catch (IllegalArgumentException ignored) {
        }
        return null;
    }

    @Override
    protected void enter(@NonNull Bug bug) {
        GridLayout summaryGrid = new GridLayout(2, 1);
        summaryGrid.addComponents(new Label("Title", ContentMode.PREFORMATTED), new Label(bug.getTitle(), ContentMode.PREFORMATTED));
        summaryGrid.addComponents(new Label("Version", ContentMode.PREFORMATTED), new Label(String.valueOf(bug.getVersionCode()), ContentMode.PREFORMATTED));
        summaryGrid.addComponents(new Label("Last Report", ContentMode.PREFORMATTED), new Label(new PrettyTime().format(bug.getLastReport()), ContentMode.PREFORMATTED));
        summaryGrid.setDefaultComponentAlignment(Alignment.MIDDLE_LEFT);
        summaryGrid.setSizeFull();
        Panel summary = new Panel(summaryGrid);
        summary.setCaption("Summary");
        tabSheet.setSizeFull();
        tabSheet.addSelectedTabChangeListener(e -> getNavigationManager().updatePageParameters(bug.getId() + "/" + e.getTabSheet().getSelectedTab().getCaption()));
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

    @Override
    protected String getTitle(@NonNull Bug bug) {
        String title = bug.getTitle();
        if (title.length() > 100) title = title.substring(0, 95) + " …";
        return title;
    }
}
