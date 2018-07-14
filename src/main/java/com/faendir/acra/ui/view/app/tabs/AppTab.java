package com.faendir.acra.ui.view.app.tabs;

import com.faendir.acra.model.App;
import com.faendir.acra.service.DataService;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.Composite;
import com.vaadin.flow.router.BeforeEvent;
import com.vaadin.flow.router.HasUrlParameter;

import java.util.Optional;

/**
 * @author lukas
 * @since 14.07.18
 */
public abstract class AppTab<T extends Component> extends Composite<T> implements HasUrlParameter<Integer> {

    private final DataService dataService;

    public AppTab(DataService dataService) {
        this.dataService = dataService;
    }

    public DataService getDataService() {
        return dataService;
    }

    @Override
    public void setParameter(BeforeEvent event, Integer parameter){
        Optional<App> app = dataService.findApp(parameter);
        if (app.isPresent()) {
            init(app.get());
        } else {
            event.rerouteToError(IllegalArgumentException.class);
        }
    }

    abstract void init(App app);


}
