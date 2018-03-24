package com.faendir.acra.ui.view.user;

import com.faendir.acra.security.SecurityUtils;
import com.faendir.acra.sql.model.User;
import com.faendir.acra.sql.user.UserManager;
import com.faendir.acra.ui.BackendUI;
import com.faendir.acra.ui.view.base.NamedView;
import com.vaadin.navigator.ViewChangeListener;
import com.vaadin.server.UserError;
import com.vaadin.spring.annotation.SpringView;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Button;
import com.vaadin.ui.Notification;
import com.vaadin.ui.PasswordField;
import com.vaadin.ui.VerticalLayout;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;

/**
 * @author Lukas
 * @since 21.05.2017
 */
@SpringView(name = "password-editor")
public class ChangePasswordView extends NamedView {
    @NonNull private final UserManager userManager;
    @NonNull private final BackendUI backendUI;

    @Autowired
    public ChangePasswordView(@NonNull UserManager userManager, @NonNull BackendUI backendUI) {
        this.userManager = userManager;
        this.backendUI = backendUI;
    }

    @Override
    public void enter(ViewChangeListener.ViewChangeEvent event) {
        PasswordField oldPassword = new PasswordField("Old Password");
        PasswordField newPassword = new PasswordField("New Password");
        PasswordField repeatPassword = new PasswordField("Repeat Password");
        Button confirm = new Button("Confirm", e -> {
            User user = userManager.getUser(SecurityUtils.getUsername());
            assert user != null;
            if (newPassword.getValue().equals(repeatPassword.getValue())) {
                if (userManager.changePassword(user, oldPassword.getValue(), newPassword.getValue())) {
                    Notification.show("Successful!");
                    getNavigationManager().navigateBack();
                    backendUI.logout();
                } else {
                    oldPassword.setComponentError(new UserError("Incorrect password"));
                }
            } else {
                repeatPassword.setComponentError(new UserError("Passwords do not match"));
            }
        });
        confirm.setSizeFull();
        VerticalLayout layout = new VerticalLayout(oldPassword, newPassword, repeatPassword, confirm);
        layout.setSizeUndefined();
        VerticalLayout root = new VerticalLayout(layout);
        root.setSizeFull();
        root.setComponentAlignment(layout, Alignment.MIDDLE_CENTER);
        setCompositionRoot(root);
    }

    public boolean validate(@Nullable String fragment) {
        return true;
    }

    @Override
    public String getTitle() {
        return "Change Password";
    }
}
