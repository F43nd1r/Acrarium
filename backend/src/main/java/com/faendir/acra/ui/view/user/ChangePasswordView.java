package com.faendir.acra.ui.view.user;

import com.faendir.acra.mongod.model.User;
import com.faendir.acra.mongod.user.UserManager;
import com.faendir.acra.security.SecurityUtils;
import com.faendir.acra.ui.BackendUI;
import com.faendir.acra.ui.view.base.NamedView;
import com.vaadin.navigator.ViewChangeListener;
import com.vaadin.server.UserError;
import com.vaadin.spring.annotation.UIScope;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Button;
import com.vaadin.ui.Notification;
import com.vaadin.ui.PasswordField;
import com.vaadin.ui.VerticalLayout;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * @author Lukas
 * @since 21.05.2017
 */
@UIScope
@Component
public class ChangePasswordView extends NamedView {

    private final UserManager userManager;
    private final BackendUI backendUI;

    @Autowired
    public ChangePasswordView(UserManager userManager, BackendUI backendUI) {

        this.userManager = userManager;
        this.backendUI = backendUI;
    }

    @Override
    public String getName() {
        return "password-editor";
    }

    @Override
    public void enter(ViewChangeListener.ViewChangeEvent event) {
        PasswordField oldPassword = new PasswordField("Old Password");
        PasswordField newPassword = new PasswordField("New Password");
        PasswordField repeatPassword = new PasswordField("Repeat Password");
        Button confirm = new Button("Confirm", e -> {
            User user = userManager.getUser(SecurityUtils.getUsername());
            if(userManager.checkPassword(user, oldPassword.getValue())) {
                if (newPassword.getValue().equals(repeatPassword.getValue())) {
                    user.setPassword(newPassword.getValue());
                    Notification.show("Successful!");
                    getNavigationManager().navigateBack();
                    backendUI.logout();
                } else {
                    repeatPassword.setComponentError(new UserError("Passwords do not match"));
                }
            }else {
                oldPassword.setComponentError(new UserError("Incorrect password"));
            }

        });
        confirm.setSizeFull();
        VerticalLayout layout = new VerticalLayout(oldPassword, newPassword, repeatPassword, confirm);
        layout.setSizeUndefined();
        VerticalLayout root = new VerticalLayout(layout);
        root.setSizeFull();
        root.setComponentAlignment(layout, Alignment.MIDDLE_CENTER);
        setSizeFull();
        setCompositionRoot(root);
    }
}
