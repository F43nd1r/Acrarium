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
import com.faendir.acra.ui.base.ParentLayout;
import com.faendir.acra.ui.base.Path;
import com.faendir.acra.ui.base.popup.Popup;
import com.faendir.acra.ui.component.DropdownMenu;
import com.faendir.acra.ui.component.FlexLayout;
import com.faendir.acra.ui.component.Translatable;
import com.faendir.acra.ui.view.user.ChangePasswordView;
import com.faendir.acra.ui.view.user.UserManager;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.checkbox.Checkbox;
import com.vaadin.flow.component.dependency.HtmlImport;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.Image;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.textfield.PasswordField;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.router.RouterLink;
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

/**
 * @author lukas
 * @since 13.07.18
 */
@HtmlImport("frontend://styles/shared-styles.html")
@UIScope
@SpringComponent
public class MainView extends ParentLayout {
    private final AuthenticationManager authenticationManager;
    private final ApplicationContext applicationContext;
    private ParentLayout layout;

    @Autowired
    public MainView(AuthenticationManager authenticationManager, ApplicationContext applicationContext) {
        this.authenticationManager = authenticationManager;
        this.applicationContext = applicationContext;
        setAlignItems(Alignment.CENTER);
        setJustifyContentMode(JustifyContentMode.CENTER);
        setSizeFull();
        getStyle().set("flex-direction", "column");
        layout = new ParentLayout();
        layout.setWidth("100%");
        expand(layout);
        layout.getStyle().set("overflow", "hidden");
        setRouterRoot(layout);
        if (SecurityUtils.isLoggedIn()) {
            showMain();
        } else {
            showLogin();
        }
    }

    private void showMain() {
        Translatable<Checkbox> darkTheme = Translatable.createCheckbox(false, Messages.DARK_THEME);
        darkTheme.getContent().addValueChangeListener(e -> {
            UI.getCurrent().getElement().setAttribute("theme", e.getValue() ? Lumo.DARK : Lumo.LIGHT);
        });
        Translatable<RouterLink> userManager = Translatable.createRouterLink(UserManager.class, Messages.USER_MANAGER);
        userManager.setDefaultTextStyle();
        Translatable<RouterLink> changePassword = Translatable.createRouterLink(ChangePasswordView.class, Messages.CHANGE_PASSWORD);
        changePassword.setDefaultTextStyle();
        Translatable<Button> logout = Translatable.createButton(e -> logout(), Messages.LOGOUT);
        logout.getElement().setAttribute("theme", "tertiary");
        logout.setDefaultTextStyle();
        Translatable<Button> about = Translatable.createButton(e -> {
            Div content = new Div();
            content.getElement().setProperty("innerHTML", getTranslation(Messages.FOOTER));
            new Popup().addComponent(content).addCloseButton().show();
        }, Messages.ABOUT);
        about.getElement().setAttribute("theme", "tertiary");
        about.setDefaultTextStyle();
        FormLayout formLayout = new FormLayout(darkTheme, userManager, changePassword, logout, about);
        formLayout.getStyle().set("background","var(--lumo-contrast-5pct");
        DropdownMenu menu = new DropdownMenu(formLayout);
        menu.getStyle().set("margin", "1rem");
        menu.setLabel(SecurityUtils.getUsername());
        menu.setMinWidth(130, Unit.PIXEL);
        menu.setOrigin(DropdownMenu.Origin.RIGHT);
        Path path = new Path(applicationContext);
        FlexLayout header = new FlexLayout(path, menu);
        header.expand(path);
        header.setWidthFull();
        header.getStyle().set("box-shadow", "0 3px 6px rgba(0,0,0,0.16), 0 3px 6px rgba(0,0,0,0.23)");
        header.getStyle().set("border-radius", "2px");
        removeAll();
        add(header, layout);
    }

    private void showLogin() {
        Translatable<Image> logo = Translatable.createImage("frontend/logo.png", Messages.ACRARIUM);
        logo.setWidth(0, Unit.PIXEL);
        logo.setPadding(1, Unit.REM);
        FlexLayout logoWrapper = new FlexLayout(logo);
        logoWrapper.expand(logo);
        TextField username = new TextField();
        PasswordField password = new PasswordField();
        Translatable<Button> login = Translatable.createButton(event -> login(username.getValue(), password.getValue()), Messages.LOGIN);
        login.setWidthFull();
        FlexLayout loginForm = new FlexLayout(logoWrapper, username, password, login);
        loginForm.setFlexDirection(FlexDirection.COLUMN);
        loginForm.setSizeUndefined();
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
            Notification.show(getTranslation(Messages.LOGIN_FAILED), 5000, Notification.Position.MIDDLE);
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
