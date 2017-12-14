package com.faendir.acra.ui.view.base;

import com.faendir.acra.sql.data.DataManager;
import com.faendir.acra.sql.model.App;
import com.faendir.acra.ui.NavigationManager;
import com.vaadin.ui.Component;
import com.vaadin.ui.CustomComponent;
import com.vaadin.ui.TabSheet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.lang.NonNull;

import java.util.List;
import java.util.Spliterator;
import java.util.Spliterators;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

/**
 * @author Lukas
 * @since 12.12.2017
 */
public class MyTabSheet extends TabSheet {
    private final App app;
    private final DataManager dataManager;
    private final NavigationManager navigationManager;

    public MyTabSheet(@NonNull App app, @NonNull DataManager dataManager, @NonNull NavigationManager navigationManager, Tab... tabs) {
        this.app = app;
        this.dataManager = dataManager;
        this.navigationManager = navigationManager;
        for (Tab component : tabs) {
            addTab(component);
        }
    }

    public List<String> getCaptions() {
        return StreamSupport.stream(Spliterators.spliterator(iterator(), getComponentCount(), Spliterator.ORDERED), false).map(Component::getCaption).collect(Collectors.toList());
    }

    public void addTab(Tab tab) {
        TabWrapper wrapper = new TabWrapper(app, dataManager, navigationManager, tab);
        addComponent(wrapper);
        addSelectedTabChangeListener(wrapper);
    }

    public void setInitialTab(String caption) {
        StreamSupport.stream(Spliterators.spliterator(iterator(), getComponentCount(), Spliterator.ORDERED), false).filter(c -> c.getCaption().equals(caption)).findAny()
                .ifPresent(component -> {
                    if (getSelectedTab() == component && component instanceof SelectedTabChangeListener) {
                        ((SelectedTabChangeListener) component).selectedTabChange(new SelectedTabChangeEvent(this, true));
                    }
                    setSelectedTab(component);
                });
    }

    public interface Tab {
        Component createContent(@NonNull App app, @NonNull DataManager dataManager, @NonNull NavigationManager navigationManager);

        String getCaption();
    }

    private static class TabWrapper extends CustomComponent implements SelectedTabChangeListener {
        private final App app;
        private final DataManager dataManager;
        private final NavigationManager navigationManager;
        private final Tab tab;

        private TabWrapper(@NonNull App app, @NonNull DataManager dataManager, @NonNull NavigationManager navigationManager, @NonNull Tab tab) {
            this.app = app;
            this.dataManager = dataManager;
            this.navigationManager = navigationManager;
            this.tab = tab;
            setSizeFull();
        }

        @Override
        public void selectedTabChange(@NonNull SelectedTabChangeEvent event) {
            if (this == event.getTabSheet().getSelectedTab()) {
                Logger logger = LoggerFactory.getLogger(TabWrapper.class);
                long start = System.currentTimeMillis();
                setCompositionRoot(tab.createContent(app, dataManager, navigationManager));
                long end = System.currentTimeMillis();
                logger.info("{} spent {} ms creating its content", tab.getClass().getName(), end - start);
            } else {
                setCompositionRoot(null);
            }
        }

        @Override
        public String getCaption() {
            return tab.getCaption();
        }
    }
}
