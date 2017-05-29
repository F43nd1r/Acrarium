package com.faendir.acra.ui.view.user;

import com.faendir.acra.mongod.data.DataManager;
import com.faendir.acra.mongod.model.User;
import com.faendir.acra.mongod.user.UserManager;
import com.faendir.acra.security.SecurityUtils;
import com.faendir.acra.ui.view.base.MyGrid;
import com.faendir.acra.ui.view.base.NamedView;
import com.faendir.acra.util.Style;
import com.vaadin.data.Binder;
import com.vaadin.navigator.ViewChangeListener;
import com.vaadin.server.UserError;
import com.vaadin.spring.annotation.UIScope;
import com.vaadin.ui.Button;
import com.vaadin.ui.CheckBox;
import com.vaadin.ui.Grid;
import com.vaadin.ui.PasswordField;
import com.vaadin.ui.TextField;
import com.vaadin.ui.UI;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.Window;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.stream.Collectors;

/**
 * @author Lukas
 * @since 20.05.2017
 */
@UIScope
@Component
public class UserManagerView extends NamedView {
    private final UserManager userManager;
    private final DataManager dataManager;
    private MyGrid<User> userGrid;

    @Autowired
    public UserManagerView(UserManager userManager, DataManager dataManager) {
        this.userManager = userManager;
        this.dataManager = dataManager;
    }

    @Override
    public String getName() {
        return "user-manager";
    }

    @Override
    public String requiredRole() {
        return UserManager.ROLE_ADMIN;
    }

    @Override
    public void enter(ViewChangeListener.ViewChangeEvent event) {
        userGrid = new MyGrid<>("Users", userManager.getUsers());
        userGrid.getEditor().setEnabled(true).setBuffered(false);
        userGrid.setSelectionMode(Grid.SelectionMode.NONE);
        userGrid.addColumn(User::getUsername, "Username");
        Binder<User> binder = userGrid.getEditor().getBinder();
        userGrid.addColumn(user -> user.getRoles().contains(UserManager.ROLE_ADMIN) ? "Yes" : "No").setCaption("Admin")
                .setEditorBinding(binder.forField(new CheckBox())
                        .withValidator(bool -> bool || !binder.getBean().getUsername().equals(SecurityUtils.getUsername()), "Cannot revoke own admin privileges")
                        .bind(user -> user.getRoles().contains(UserManager.ROLE_ADMIN), userManager::setAdmin));
        userGrid.addColumn(this::getPermissionString, "App Permissions")
                .setEditorBinding(userGrid.getEditor().getBinder().bind(new PermissionEditor(dataManager), User::getPermissions,
                        ((user, permissions) -> permissions.forEach(p -> userManager.setPermission(user, p.getApp(), p.getLevel())))));
        Button newUser = new Button("New User", e -> newUser());
        VerticalLayout layout = new VerticalLayout(userGrid, newUser);
        layout.setExpandRatio(userGrid, 1);
        layout.setSizeFull();
        Style.NO_PADDING.apply(layout);
        setCompositionRoot(layout);
        userGrid.setSizeFull();
        setSizeFull();
        Style.apply(this, Style.PADDING_LEFT, Style.PADDING_RIGHT, Style.PADDING_BOTTOM);
    }

    private String getPermissionString(User user) {
        return user.getPermissions().stream()
                .map(permission -> dataManager.getApp(permission.getApp()).getName() + ": " + permission.getLevel().name())
                .collect(Collectors.joining(", "));
    }

    private void newUser() {
        Window window = new Window("New User");
        TextField name = new TextField("Username");
        PasswordField password = new PasswordField("Password");
        PasswordField repeatPassword = new PasswordField("Repeat Password");
        Button create = new Button("Create");
        create.addClickListener(e -> {
            if (password.getValue().equals(repeatPassword.getValue())) {
                userManager.createUser(name.getValue().toLowerCase(), password.getValue());
                userGrid.setItems(userManager.getUsers());
                window.close();
            } else {
                repeatPassword.setComponentError(new UserError("Passwords do not match"));
            }

        });
        VerticalLayout layout = new VerticalLayout(name, password, repeatPassword, create);
        window.setContent(layout);
        window.center();
        UI.getCurrent().addWindow(window);
    }

}
