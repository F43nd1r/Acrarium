package com.faendir.acra.ui;

import com.faendir.acra.mongod.user.UserManager;
import com.faendir.acra.security.SecurityUtils;
import com.faendir.acra.ui.view.user.ChangePasswordView;
import com.faendir.acra.ui.view.user.UserManagerView;
import com.faendir.acra.util.Style;
import com.vaadin.annotations.Theme;
import com.vaadin.server.VaadinRequest;
import com.vaadin.server.VaadinService;
import com.vaadin.shared.communication.PushMode;
import com.vaadin.spring.annotation.SpringUI;
import com.vaadin.spring.annotation.UIScope;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Button;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.LoginForm;
import com.vaadin.ui.Notification;
import com.vaadin.ui.UI;
import com.vaadin.ui.VerticalLayout;
import org.jetbrains.annotations.NotNull;
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
    @NotNull private final AuthenticationManager authenticationManager;
    @NotNull private final ApplicationContext applicationContext;
    @NotNull private final VerticalLayout content;

    @Autowired
    public BackendUI(@NotNull AuthenticationManager authenticationManager, @NotNull ApplicationContext applicationContext) {
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
            showLogin();
        }
        //applicationContext.getBean(ToSqlMigrator.class).migrate();
    }

    private void login(@NotNull String username, @NotNull String password) {
        try {
            Authentication token = authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(username.toLowerCase(), password));
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
        Button up = new Button("Up", e -> navigationManager.navigateBack());
        HorizontalLayout header = new HorizontalLayout(up);
        header.setExpandRatio(up, 1);
        header.setWidth(100, Unit.PERCENTAGE);

        if(SecurityUtils.hasRole(UserManager.ROLE_ADMIN)){
            Button userManager = new Button("User Manager", e -> navigationManager.navigateTo(UserManagerView.class, ""));
            header.addComponent(userManager);
            header.setComponentAlignment(userManager, Alignment.MIDDLE_RIGHT);
        }

        Button changePassword = new Button("Change Password", e-> navigationManager.navigateTo(ChangePasswordView.class, ""));
        header.addComponent(changePassword);
        header.setComponentAlignment(changePassword, Alignment.MIDDLE_RIGHT);

        Button logout = new Button("Logout", e -> logout());
        header.addComponent(logout);
        header.setComponentAlignment(logout, Alignment.MIDDLE_RIGHT);

        Style.apply(header, Style.PADDING_TOP, Style.PADDING_LEFT, Style.PADDING_RIGHT);
        VerticalLayout root = new VerticalLayout(header, content);
        root.setExpandRatio(content, 1);
        root.setSizeFull();
        Style.NO_PADDING.apply(root);
        setContent(root);
    }

    @NotNull
    @UIScope
    @Bean
    public VerticalLayout mainView() {
        return content;
    }

}
