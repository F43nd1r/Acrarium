package com.faendir.acra.ui;

import com.faendir.acra.security.SecurityUtils;
import com.faendir.acra.util.Style;
import com.vaadin.annotations.Theme;
import com.vaadin.server.VaadinRequest;
import com.vaadin.server.VaadinService;
import com.vaadin.spring.annotation.SpringUI;
import com.vaadin.spring.annotation.UIScope;
import com.vaadin.ui.Button;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.LoginForm;
import com.vaadin.ui.UI;
import com.vaadin.ui.VerticalLayout;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContextHolder;

/**
 * @author Lukas
 * @since 22.03.2017
 */
@SpringUI
@Theme("acratheme")
public class BackendUI extends UI {
    private final AuthenticationManager authenticationManager;
    private final ApplicationContext applicationContext;
    private final VerticalLayout content;

    @Autowired
    public BackendUI(AuthenticationManager authenticationManager, ApplicationContext applicationContext) {
        this.authenticationManager = authenticationManager;
        this.applicationContext = applicationContext;
        content = new VerticalLayout();
        content.setSizeFull();
        Style.NO_PADDING.apply(content);
    }

    @Override
    protected void init(VaadinRequest request) {
        if (SecurityUtils.isLoggedIn()) {
            showMain();
        } else {
            LoginForm loginForm = new LoginForm();
            loginForm.addLoginListener(event -> login(event.getLoginParameter("username"), event.getLoginParameter("password")));
            setContent(loginForm);
        }
    }

    private boolean login(String username, String password) {
        try {
            Authentication token = authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(username, password));
            VaadinService.reinitializeSession(VaadinService.getCurrentRequest());
            SecurityContextHolder.getContext().setAuthentication(token);
            showMain();
            return true;
        } catch (AuthenticationException ex) {
            return false;
        }
    }

    private void showMain() {
        NavigationManager navigationManager = applicationContext.getBean(NavigationManager.class);
        HorizontalLayout header = new HorizontalLayout(new Button("Up", e -> navigationManager.navigateBack()));
        Style.apply(header, Style.MARGIN_TOP, Style.MARGIN_LEFT, Style.MARGIN_RIGHT);
        VerticalLayout root = new VerticalLayout(header, content);
        root.setExpandRatio(content, 1);
        root.setSizeFull();
        Style.NO_PADDING.apply(root);
        setContent(root);
    }

    @UIScope
    @Bean
    public VerticalLayout mainView() {
        return content;
    }

}
