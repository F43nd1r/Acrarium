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
import com.faendir.acra.ui.view.user.AccountView;
import com.faendir.acra.ui.view.user.UserManager;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.checkbox.Checkbox;
import com.vaadin.flow.component.dependency.HtmlImport;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.Image;
import com.vaadin.flow.component.login.LoginForm;
import com.vaadin.flow.component.login.LoginI18n;
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
        darkTheme.getContent().addValueChangeListener(e -> UI.getCurrent().getElement().setAttribute("theme", e.getValue() ? Lumo.DARK : Lumo.LIGHT));
        Translatable<Button> userManager = Translatable.createButton(e -> UI.getCurrent().navigate(UserManager.class), Messages.USER_MANAGER);
        userManager.setDefaultTextStyle();
        userManager.getContent().addThemeVariants(ButtonVariant.LUMO_TERTIARY);
        Translatable<Button> changePassword = Translatable.createButton(e -> UI.getCurrent().navigate(AccountView.class), Messages.ACCOUNT);
        changePassword.setDefaultTextStyle();
        changePassword.getContent().addThemeVariants(ButtonVariant.LUMO_TERTIARY);
        Translatable<Button> logout = Translatable.createButton(e -> logout(), Messages.LOGOUT);
        logout.setDefaultTextStyle();
        logout.getContent().addThemeVariants(ButtonVariant.LUMO_TERTIARY);
        Translatable<Button> about = Translatable.createButton(e -> {
            Div content = new Div();
            content.getElement().setProperty("innerHTML", getTranslation(Messages.FOOTER));
            new Popup().addComponent(content).addCloseButton().show();
        }, Messages.ABOUT);
        about.getContent().addThemeVariants(ButtonVariant.LUMO_TERTIARY);
        about.setDefaultTextStyle();
        FlexLayout menuLayout = new FlexLayout(darkTheme, userManager, changePassword, logout, about);
        menuLayout.setFlexDirection(FlexDirection.COLUMN);
        //menuLayout.getChildren().forEach(c -> c.getElement().getStyle().set("margin", "0"));
        menuLayout.getStyle().set("background","var(--lumo-contrast-5pct");
        menuLayout.getStyle().set("padding","1rem");
        DropdownMenu menu = new DropdownMenu(menuLayout);
        menu.getStyle().set("padding", "1rem");
        menu.setLabel(SecurityUtils.getUsername());
        menu.setMinWidth(170, Unit.PIXEL);
        menu.setOrigin(DropdownMenu.Origin.RIGHT);
        Path path = new Path(applicationContext);
        FlexLayout header = new FlexLayout(path, menu);
        header.expand(path);
        path.getStyle().set("flex-shrink", "1");
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
        LoginI18n loginI18n = LoginI18n.createDefault();
        loginI18n.getForm().setTitle("");
        LoginForm loginForm = new LoginForm(loginI18n);
        loginForm.setForgotPasswordButtonVisible(false);
        loginForm.getElement().getStyle().set("padding", "0");
        loginForm.addLoginListener(event -> {
            if(!login(event.getUsername(), event.getPassword())){
                event.getSource().setError(true);
            }
        });
        FlexLayout layout = new FlexLayout(logoWrapper, loginForm);
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

    public void logout() {
        SecurityContextHolder.clearContext();
        getUI().ifPresent(ui -> {
            ui.getPage().reload();
            ui.getSession().close();
        });
    }
}
