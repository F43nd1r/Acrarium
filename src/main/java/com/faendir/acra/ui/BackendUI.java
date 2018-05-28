package com.faendir.acra.ui;

import com.faendir.acra.model.User;
import com.faendir.acra.security.SecurityUtils;
import com.faendir.acra.ui.navigation.NavigationManager;
import com.faendir.acra.ui.view.base.Path;
import com.faendir.acra.ui.view.user.ChangePasswordView;
import com.faendir.acra.ui.view.user.UserManagerView;
import com.faendir.acra.util.Style;
import com.vaadin.annotations.Theme;
import com.vaadin.annotations.Viewport;
import com.vaadin.annotations.Widgetset;
import com.vaadin.icons.VaadinIcons;
import com.vaadin.server.VaadinRequest;
import com.vaadin.server.VaadinService;
import com.vaadin.shared.communication.PushMode;
import com.vaadin.shared.ui.ContentMode;
import com.vaadin.spring.annotation.SpringUI;
import com.vaadin.spring.annotation.UIScope;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.LoginForm;
import com.vaadin.ui.MenuBar;
import com.vaadin.ui.Notification;
import com.vaadin.ui.Panel;
import com.vaadin.ui.UI;
import com.vaadin.ui.VerticalLayout;
import org.apache.http.client.utils.URLEncodedUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.InsufficientAuthenticationException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.util.UriComponentsBuilder;

import java.nio.charset.StandardCharsets;

/**
 * @author Lukas
 * @since 22.03.2017
 */
@SpringUI
@Theme("acratheme")
@Widgetset("com.faendir.acra.AppWidgetset")
@Viewport("width=device-width, initial-scale=1")
public class BackendUI extends UI {
    private static final String DARK_THEME = "dark";
    @NonNull private final AuthenticationManager authenticationManager;
    @NonNull private final ApplicationContext applicationContext;
    @NonNull private final Panel content;
    @NonNull private final Path path;

    @Autowired
    public BackendUI(@NonNull AuthenticationManager authenticationManager, @NonNull ApplicationContext applicationContext) {
        this.authenticationManager = authenticationManager;
        this.applicationContext = applicationContext;
        content = new Panel();
        content.setSizeFull();
        Style.apply(content, Style.NO_PADDING, Style.NO_BACKGROUND, Style.NO_BORDER);
        path = new Path();
    }

    @Override
    protected void init(VaadinRequest request) {
        if (URLEncodedUtils.parse(getPage().getLocation(), StandardCharsets.UTF_8)
                .stream()
                .anyMatch(pair -> pair.getName().equals(DARK_THEME) && Boolean.parseBoolean(pair.getValue()))) {
            setTheme("acratheme-dark");
        }
        if (SecurityUtils.isLoggedIn()) {
            showMain();
        } else {
            showLogin();
        }
    }

    private void login(@NonNull String username, @NonNull String password) {
        try {
            Authentication token = authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(username.toLowerCase(), password));
            if (!token.getAuthorities().contains(User.Role.USER)) {
                throw new InsufficientAuthenticationException("Missing required role");
            }
            VaadinService.reinitializeSession(VaadinService.getCurrentRequest());
            SecurityContextHolder.getContext().setAuthentication(token);
            showMain();
            getPushConfiguration().setPushMode(PushMode.AUTOMATIC);
        } catch (AuthenticationException ex) {
            Notification.show("Unknown username/password combination", Notification.Type.ERROR_MESSAGE);
        }
    }

    public void logout() {
        getPushConfiguration().setPushMode(PushMode.DISABLED);
        SecurityContextHolder.clearContext();
        getPage().reload();
        getSession().close();
    }

    private void showLogin() {
        LoginForm loginForm = new LoginForm();
        loginForm.addLoginListener(event -> login(event.getLoginParameter("username"), event.getLoginParameter("password")));
        VerticalLayout layout = new VerticalLayout(loginForm);
        layout.setSizeFull();
        layout.setComponentAlignment(loginForm, Alignment.MIDDLE_CENTER);
        setContent(layout);
    }

    private void showMain() {
        NavigationManager navigationManager = applicationContext.getBean(NavigationManager.class);

        MenuBar menuBar = new MenuBar();
        MenuBar.MenuItem user = menuBar.addItem("", VaadinIcons.USER, null);
        user.addItem(SecurityUtils.getUsername()).setEnabled(false);
        user.addSeparator();
        MenuBar.MenuItem theme = user.addItem("Dark Theme", e -> {
            getPage().setLocation(UriComponentsBuilder.fromUri(getPage().getLocation()).replaceQueryParam(DARK_THEME, e.isChecked()).build().toUri());
        });
        theme.setCheckable(true);
        theme.setChecked(URLEncodedUtils.parse(getPage().getLocation(), StandardCharsets.UTF_8)
                .stream()
                .anyMatch(pair -> pair.getName().equals(DARK_THEME) && Boolean.parseBoolean(pair.getValue())));
        user.addSeparator();
        if (SecurityUtils.hasRole(User.Role.ADMIN)) {
            user.addItem("User Manager", e -> navigationManager.cleanNavigateTo(UserManagerView.class));
            user.addSeparator();
        }
        user.addItem("Change Password", e -> navigationManager.cleanNavigateTo(ChangePasswordView.class));
        user.addItem("Logout", e -> logout());

        HorizontalLayout header = new HorizontalLayout(path, menuBar);
        header.setExpandRatio(path, 1);
        header.setWidth(100, Unit.PERCENTAGE);
        Style.apply(header, Style.PADDING_TOP, Style.PADDING_LEFT, Style.PADDING_RIGHT, Style.PADDING_BOTTOM, Style.BACKGROUND_HEADER);

        HorizontalLayout footer = new HorizontalLayout();
        footer.setDefaultComponentAlignment(Alignment.MIDDLE_CENTER);
        Label footerLabel = new Label("Acrarium is developed by <a href=https://github.com/F43nd1r>F43nd1r</a>."
                                      + " <a href=https://github.com/F43nd1r/acra-backend>Code</a> is licensed under"
                                      + " <a href=https://github.com/F43nd1r/acra-backend/blob/master/LICENSE>Apache License v2</a>.", ContentMode.HTML);
        footerLabel.setWidth(100, Unit.PERCENTAGE);
        Style.CENTER_TEXT.apply(footerLabel);
        footer.addComponent(footerLabel);
        footer.setSpacing(false);
        footer.setWidth(100, Unit.PERCENTAGE);
        Style.apply(footer, Style.BACKGROUND_FOOTER, Style.PADDING_LEFT, Style.PADDING_TOP, Style.PADDING_RIGHT, Style.PADDING_BOTTOM);
        VerticalLayout root = new VerticalLayout(header, content, footer);
        root.setExpandRatio(content, 1);
        root.setSpacing(false);
        root.setSizeFull();
        Style.NO_PADDING.apply(root);
        setContent(root);
    }

    @NonNull
    @UIScope
    @Bean
    public Panel mainView() {
        return content;
    }

    @NonNull
    @UIScope
    @Bean
    public Path mainPath() {
        return path;
    }
}
