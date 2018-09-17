package com.faendir.acra.ui.view.report;

import com.faendir.acra.security.SecurityUtils;
import com.faendir.acra.ui.view.MainView;
import com.vaadin.flow.component.Composite;
import com.vaadin.flow.component.Text;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.router.BeforeEvent;
import com.vaadin.flow.router.HasUrlParameter;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.spring.annotation.SpringComponent;
import com.vaadin.flow.spring.annotation.UIScope;

/**
 * @author lukas
 * @since 17.09.18
 */
@UIScope
@SpringComponent
@Route(value = "report", layout = MainView.class)
public class ReportView extends Composite<Div> implements HasUrlParameter<String> {
    @Override
    public void setParameter(BeforeEvent event, String parameter) {
        if(SecurityUtils.isLoggedIn()) {
            getContent().add(new Text(parameter));
        }
    }
}
