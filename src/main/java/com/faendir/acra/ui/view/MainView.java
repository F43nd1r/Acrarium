package com.faendir.acra.ui.view;

import com.faendir.acra.model.User;
import com.faendir.acra.security.SecurityUtils;
import com.faendir.acra.ui.base.ParentLayout;
import com.vaadin.flow.component.Text;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.html.Anchor;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.PasswordField;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.router.BeforeEnterEvent;
import com.vaadin.flow.router.BeforeEnterObserver;
import com.vaadin.flow.router.NavigationTrigger;
import com.vaadin.flow.server.VaadinService;
import com.vaadin.flow.spring.annotation.SpringComponent;
import com.vaadin.flow.spring.annotation.UIScope;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.InsufficientAuthenticationException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContextHolder;

/**
 * @author lukas
 * @since 13.07.18
 */
@UIScope
@SpringComponent
public class MainView extends ParentLayout {
    private final AuthenticationManager authenticationManager;
    private ParentLayout layout;

    @Autowired
    public MainView(AuthenticationManager authenticationManager) {
        this.authenticationManager = authenticationManager;
        setSizeFull();
        setAlignItems(Alignment.CENTER);
        setJustifyContentMode(JustifyContentMode.CENTER);
        layout = new ParentLayout();
        setRouterRoot(layout);
        if (SecurityUtils.isLoggedIn()) {
            showMain();
        } else {
            showLogin();
        }
    }

    private void showMain() {
        HorizontalLayout header = new HorizontalLayout();
        HorizontalLayout footer = new HorizontalLayout(new Text("Acrarium is developed by"),
                new Anchor("https://github.com/F43nd1r", "F43nd1r"),
                new Span(". "),
                new Anchor("https://github.com/F43nd1r/acra-backend", "Code"),
                new Span(" is licensed under "),
                new Anchor("https://github.com/F43nd1r/acra-backend/blob/master/LICENSE", "Apache License v2"),
                new Span("."));
        footer.setAlignItems(Alignment.CENTER);
        footer.setSpacing(true);
        VerticalLayout root = new VerticalLayout(header, layout, footer);
        if (layout.getContent() instanceof BeforeEnterObserver) {
            ((BeforeEnterObserver) layout.getContent()).beforeEnter(new BeforeEnterEvent(getUI().get().getRouter(),
                    NavigationTrigger.PROGRAMMATIC,
                    null,
                    layout.getContent().getClass(),
                    getUI().get()));
        }
        root.expand(layout);
        root.setSizeFull();
        setContent(root);
    }

    private void showLogin() {
        VerticalLayout loginForm = new VerticalLayout();
        loginForm.setSizeUndefined();
        TextField username = new TextField();
        PasswordField password = new PasswordField();
        Button login = new Button("Login", event -> login(username.getValue(), password.getValue()));
        login.setWidth("100%");
        loginForm.add(username, password, login);
        setContent(loginForm);
    }

    private void login(@NonNull String username, @NonNull String password) {
        try {
            Authentication token = authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(username.toLowerCase(), password));
            if (!token.getAuthorities().contains(User.Role.USER)) {
                throw new InsufficientAuthenticationException("Missing required role");
            }
            VaadinService.reinitializeSession(VaadinService.getCurrentRequest());
            SecurityContextHolder.getContext().setAuthentication(token);
            UI.getCurrent().getPage().reload();
        } catch (AuthenticationException ex) {
            Notification.show("Unknown username/password combination");
        }
    }

    public void logout() {
        SecurityContextHolder.clearContext();
        getUI().ifPresent(ui -> {
            ui.getPage().reload();
            ui.getSession().close();
        });
    }
}
