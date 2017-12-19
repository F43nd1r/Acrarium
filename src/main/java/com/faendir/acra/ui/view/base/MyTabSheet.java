package com.faendir.acra.ui.view.base;

import com.faendir.acra.security.SecurityUtils;
import com.faendir.acra.sql.model.App;
import com.faendir.acra.ui.NavigationManager;
import com.faendir.acra.ui.annotation.RequiresAppPermission;
import com.vaadin.ui.Component;
import com.vaadin.ui.CustomComponent;
import com.vaadin.ui.TabSheet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;
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
    private final NavigationManager navigationManager;

    @SafeVarargs
    public MyTabSheet(@NonNull App app, @NonNull NavigationManager navigationManager, @NonNull ApplicationContext applicationContext, Class<? extends Tab>... tabs) {
        this.app = app;
        this.navigationManager = navigationManager;
        for (Class<? extends Tab> tabClass : tabs) {
            RequiresAppPermission annotation = tabClass.getAnnotation(RequiresAppPermission.class);
            if(annotation == null || SecurityUtils.hasPermission(app, annotation.value())) {
                addTab(applicationContext.getBean(tabClass));
            }
        }
    }

    public List<String> getCaptions() {
        return StreamSupport.stream(Spliterators.spliterator(iterator(), getComponentCount(), Spliterator.ORDERED), false).map(Component::getCaption).collect(Collectors.toList());
    }

    public void addTab(Tab tab) {
        TabWrapper wrapper = new TabWrapper(app, navigationManager, tab);
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
        Component createContent(@NonNull App app, @NonNull NavigationManager navigationManager);

        String getCaption();
    }

    private static class TabWrapper extends CustomComponent implements SelectedTabChangeListener {
        private final App app;
        private final NavigationManager navigationManager;
        private final Tab tab;

        private TabWrapper(@NonNull App app, @NonNull NavigationManager navigationManager, @NonNull Tab tab) {
            this.app = app;
            this.navigationManager = navigationManager;
            this.tab = tab;
            setSizeFull();
        }

        @Override
        public void selectedTabChange(@NonNull SelectedTabChangeEvent event) {
            if (this == event.getTabSheet().getSelectedTab()) {
                Logger logger = LoggerFactory.getLogger(TabWrapper.class);
                long start = System.currentTimeMillis();
                setCompositionRoot(tab.createContent(app, navigationManager));
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
