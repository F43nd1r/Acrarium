/*
 * (C) Copyright 2018 Lukas Morawietz (https://github.com/F43nd1r)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.faendir.acra.ui.view;

import com.faendir.acra.i18n.Messages;
import com.faendir.acra.model.User;
import com.faendir.acra.security.SecurityUtils;
import com.faendir.acra.service.UserService;
import com.faendir.acra.ui.base.ParentLayout;
import com.faendir.acra.ui.component.Path;
import com.faendir.acra.ui.component.FlexLayout;
import com.faendir.acra.ui.component.Label;
import com.faendir.acra.ui.component.Tab;
import com.faendir.acra.ui.component.Translatable;
import com.faendir.acra.ui.component.UserEditor;
import com.faendir.acra.ui.view.user.AccountView;
import com.faendir.acra.ui.view.user.UserManager;
import com.faendir.acra.util.LocalSettings;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.HasElement;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.applayout.AppLayout;
import com.vaadin.flow.component.applayout.DrawerToggle;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.dependency.JsModule;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.Image;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.login.LoginForm;
import com.vaadin.flow.component.login.LoginI18n;
import com.vaadin.flow.component.tabs.Tabs;
import com.vaadin.flow.server.VaadinService;
import com.vaadin.flow.spring.annotation.SpringComponent;
import com.vaadin.flow.spring.annotation.UIScope;
import com.vaadin.flow.theme.lumo.Lumo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.InsufficientAuthenticationException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * @author lukas
 * @since 13.07.18
 */
@JsModule("./styles/shared-styles.js")
@UIScope
@SpringComponent
public class MainView extends ParentLayout {
    private final AuthenticationManager authenticationManager;
    private final ApplicationContext applicationContext;
    private final UserService userService;
    private AppLayout layout;
    private Map<com.vaadin.flow.component.tabs.Tab, Class<? extends Component>> targets;
    private Tabs tabs;

    @Autowired
    public MainView(AuthenticationManager authenticationManager, ApplicationContext applicationContext, UserService userService, LocalSettings localSettings) {
        this.authenticationManager = authenticationManager;
        this.applicationContext = applicationContext;
        this.userService = userService;
        setAlignItems(Alignment.CENTER);
        setJustifyContentMode(JustifyContentMode.CENTER);
        setSizeFull();
        layout = new AppLayout();
        layout.getElement().getStyle().set("width", "100%");
        layout.getElement().getStyle().set("height", "100%");
        setRouterRoot(layout);
        UI.getCurrent().getElement().setAttribute("theme", localSettings.getDarkTheme() ? Lumo.DARK : Lumo.LIGHT);
        UI.getCurrent().setLocale(localSettings.getLocale());
        if (SecurityUtils.isLoggedIn()) {
            showMain();
        } else if (userService.hasAdmin()) {
            showLogin();
        } else {
            showFirstTimeSetup();
        }
    }

    @Override
    public void showRouterLayoutContent(HasElement content) {
        super.showRouterLayoutContent(content);
        if (targets != null) {
            Class<?> clazz = content.getClass();
            targets.entrySet().stream().filter(e -> clazz.equals(e.getValue())).findAny().ifPresent(e -> tabs.setSelectedTab(e.getKey()));
        }
    }

    private void showMain() {
        layout.setPrimarySection(AppLayout.Section.DRAWER);
        targets = new LinkedHashMap<>();
        targets.put(new Tab(Messages.HOME), Overview.class);
        targets.put(new Path(applicationContext), Component.class);
        targets.put(new Tab(Messages.ACCOUNT), AccountView.class);
        if (SecurityUtils.hasRole(User.Role.ADMIN)) {
            targets.put(new Tab(Messages.USER_MANAGER), UserManager.class);
        }
        targets.put(new Tab(Messages.SETTINGS), SettingsView.class);
        targets.put(new Tab(Messages.ABOUT), AboutView.class);
        tabs = new Tabs(targets.keySet().toArray(new com.vaadin.flow.component.tabs.Tab[0]));
        tabs.setOrientation(Tabs.Orientation.VERTICAL);
        tabs.addSelectedChangeListener(e -> {
            Class<? extends Component> target = targets.get(e.getSelectedTab());
            if (target != null && target != Component.class) {
                getUI().ifPresent(ui -> ui.navigate(target));
            }
        });
        layout.addToDrawer(tabs);
        DrawerToggle drawerToggle = new DrawerToggle();
        Translatable<Button> button = Translatable.createButton(e -> logout(), Messages.LOGOUT).with(b -> {
            b.setIcon(VaadinIcon.POWER_OFF.create());
            b.removeThemeVariants(ButtonVariant.LUMO_PRIMARY);
            b.addThemeVariants(ButtonVariant.LUMO_TERTIARY);
        });
        button.setPaddingRight(10, Unit.PIXEL);
        Div spacer = new Div();
        expand(spacer);
        Translatable<Image> image = Translatable.createImage("frontend/logo.png", Messages.ACRARIUM);
        image.setHeight(32, Unit.PIXEL);
        layout.addToNavbar(drawerToggle, image, spacer, button);
        removeAll();
        add(layout);
    }

    private void showLogin() {
        Translatable<Image> logo = Translatable.createImage("frontend/logo.png", Messages.ACRARIUM);
        logo.setWidth(0, Unit.PIXEL);
        FlexLayout logoWrapper = new FlexLayout(logo);
        logoWrapper.expand(logo);
        LoginI18n loginI18n = LoginI18n.createDefault();
        loginI18n.getForm().setTitle("");
        LoginForm loginForm = new LoginForm(loginI18n);
        loginForm.setForgotPasswordButtonVisible(false);
        loginForm.getElement().getStyle().set("padding", "0");
        loginForm.addLoginListener(event -> {
            if (!login(event.getUsername(), event.getPassword())) {
                event.getSource().setError(true);
            }
        });
        FlexLayout layout = new FlexLayout(logoWrapper, loginForm);
        layout.setFlexDirection(FlexDirection.COLUMN);
        layout.setSizeUndefined();
        setContent(layout);
    }

    private void showFirstTimeSetup() {
        Translatable<Image> logo = Translatable.createImage("frontend/logo.png", Messages.ACRARIUM);
        logo.setWidthFull();
        logo.setPaddingTop(0.5, Unit.REM);
        logo.setPaddingBottom(1, Unit.REM);
        Translatable<Label> welcomeLabel = Translatable.createLabel(Messages.WELCOME);
        welcomeLabel.getStyle().set("font-size", "var(--lumo-font-size-xxl");
        FlexLayout header = new FlexLayout(welcomeLabel, logo, Translatable.createLabel(Messages.CREATE_ADMIN));
        header.setFlexDirection(FlexDirection.COLUMN);
        header.setAlignSelf(Alignment.CENTER, welcomeLabel);
        header.setWidth(0, Unit.PIXEL);
        FlexLayout wrapper = new FlexLayout(header);
        wrapper.expand(header);
        UserEditor userEditor = new UserEditor(userService, null, () -> UI.getCurrent().getPage().reload());
        FlexLayout layout = new FlexLayout(wrapper, userEditor);
        layout.setFlexDirection(FlexDirection.COLUMN);
        layout.setSizeUndefined();
        setContent(layout);
    }

    private boolean login(@NonNull String username, @NonNull String password) {
        try {
            Authentication token = authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(username.toLowerCase(), password));
            if (!token.getAuthorities().contains(User.Role.USER)) {
                throw new InsufficientAuthenticationException("Missing required role");
            }
            VaadinService.reinitializeSession(VaadinService.getCurrentRequest());
            SecurityContextHolder.getContext().setAuthentication(token);
            UI.getCurrent().getPage().reload();
            return true;
        } catch (AuthenticationException ex) {
            return false;
        }
    }

    private void logout() {
        SecurityContextHolder.clearContext();
        getUI().ifPresent(ui -> {
            ui.getPage().reload();
            ui.getSession().close();
        });
    }
}
