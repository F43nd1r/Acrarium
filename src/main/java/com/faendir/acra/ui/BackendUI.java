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
package com.faendir.acra.ui;

import com.faendir.acra.i18n.I18nLabel;
import com.faendir.acra.i18n.I18nMenuBar;
import com.faendir.acra.i18n.Messages;
import com.faendir.acra.model.User;
import com.faendir.acra.security.SecurityUtils;
import com.faendir.acra.ui.navigation.NavigationManager;
import com.faendir.acra.ui.view.base.navigation.Path;
import com.faendir.acra.ui.view.user.ChangePasswordView;
import com.faendir.acra.ui.view.user.UserManagerView;
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
import com.vaadin.ui.Notification;
import com.vaadin.ui.Panel;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.themes.AcraTheme;
import com.vaadin.ui.themes.DarkAcraTheme;
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
import org.vaadin.spring.i18n.I18N;
import org.vaadin.spring.i18n.support.TranslatableUI;

import java.util.Optional;

/**
 * @author Lukas
 * @since 22.03.2017
 */
@SpringUI
@Theme(AcraTheme.THEME_NAME)
@Widgetset("com.faendir.acra.AppWidgetset")
@Viewport("width=device-width, initial-scale=1")
public class BackendUI extends TranslatableUI {
    private static final String DARK_THEME = "dark";
    @NonNull private final AuthenticationManager authenticationManager;
    @NonNull private final ApplicationContext applicationContext;
    @NonNull private final I18N i18n;
    @NonNull private final Panel content;
    @NonNull private final Path path;

    @Autowired
    public BackendUI(@NonNull AuthenticationManager authenticationManager, @NonNull ApplicationContext applicationContext, @NonNull I18N i18n) {
        this.authenticationManager = authenticationManager;
        this.applicationContext = applicationContext;
        this.i18n = i18n;
        content = new Panel();
        content.setSizeFull();
        content.addStyleNames(AcraTheme.NO_PADDING, AcraTheme.NO_BACKGROUND, AcraTheme.NO_BORDER);
        path = new Path();
    }

    @Override
    protected void initUI(VaadinRequest request) {
        if (isDarkTheme()) {
            setTheme(DarkAcraTheme.THEME_NAME);
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
                throw new InsufficientAuthenticationException(i18n.get(Messages.MISSING_ROLE));
            }
            VaadinService.reinitializeSession(VaadinService.getCurrentRequest());
            SecurityContextHolder.getContext().setAuthentication(token);
            showMain();
            getPushConfiguration().setPushMode(PushMode.AUTOMATIC);
        } catch (AuthenticationException ex) {
            Notification.show(i18n.get(Messages.LOGIN_FAILED), Notification.Type.ERROR_MESSAGE);
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

        I18nMenuBar menuBar = new I18nMenuBar(i18n);
        I18nMenuBar.I18nMenuItem user = menuBar.addItem(VaadinIcons.USER);
        user.addItem(SecurityUtils.getUsername()).setEnabled(false);
        user.addSeparator();
        I18nMenuBar.I18nMenuItem theme = user.addItem(e -> getPage().setLocation(UriComponentsBuilder.fromUri(getPage().getLocation())
                .replaceQueryParam(DARK_THEME, e.isChecked())
                .build()
                .toUri()), Messages.DARK_THEME);
        theme.setCheckable(true);
        theme.setChecked(isDarkTheme());
        user.addSeparator();
        if (SecurityUtils.hasRole(User.Role.ADMIN)) {
            user.addItem(e -> navigationManager.cleanNavigateTo(UserManagerView.class), Messages.USER_MANAGER);
            user.addSeparator();
        }
        user.addItem(e -> navigationManager.cleanNavigateTo(ChangePasswordView.class), Messages.CHANGE_PASSWORD);
        user.addItem(e -> logout(), Messages.LOGOUT);

        HorizontalLayout header = new HorizontalLayout(path, menuBar);
        header.setExpandRatio(path, 1);
        header.setWidth(100, Unit.PERCENTAGE);
        header.addStyleNames(AcraTheme.PADDING_TOP, AcraTheme.PADDING_LEFT, AcraTheme.PADDING_RIGHT, AcraTheme.PADDING_BOTTOM, AcraTheme.BACKGROUND_HEADER);

        HorizontalLayout footer = new HorizontalLayout();
        footer.setDefaultComponentAlignment(Alignment.MIDDLE_CENTER);
        Label footerLabel = new I18nLabel(ContentMode.HTML, i18n, Messages.FOOTER);
        footerLabel.setWidth(100, Unit.PERCENTAGE);
        footerLabel.addStyleName(AcraTheme.CENTER_TEXT);
        footer.addComponent(footerLabel);
        footer.setSpacing(false);
        footer.setWidth(100, Unit.PERCENTAGE);
        footer.addStyleNames(AcraTheme.BACKGROUND_FOOTER, AcraTheme.PADDING_LEFT, AcraTheme.PADDING_TOP, AcraTheme.PADDING_RIGHT, AcraTheme.PADDING_BOTTOM);
        VerticalLayout root = new VerticalLayout(header, content, footer);
        root.setExpandRatio(content, 1);
        root.setSpacing(false);
        root.setSizeFull();
        root.addStyleName(AcraTheme.NO_PADDING);
        setContent(root);
    }

    private boolean isDarkTheme() {
        return Optional.ofNullable(UriComponentsBuilder.fromUri(getPage().getLocation()).build().getQueryParams().get(DARK_THEME)).map(list -> list.contains("true")).orElse(false);
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
