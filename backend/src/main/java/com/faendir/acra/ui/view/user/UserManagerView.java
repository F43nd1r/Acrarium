package com.faendir.acra.ui.view.user;

import com.faendir.acra.mongod.data.DataManager;
import com.faendir.acra.mongod.model.App;
import com.faendir.acra.mongod.model.Permission;
import com.faendir.acra.mongod.model.User;
import com.faendir.acra.mongod.user.UserManager;
import com.faendir.acra.security.SecurityUtils;
import com.faendir.acra.ui.view.base.MyCheckBox;
import com.faendir.acra.ui.view.base.MyGrid;
import com.faendir.acra.ui.view.base.NamedView;
import com.faendir.acra.util.Style;
import com.vaadin.navigator.ViewChangeListener;
import com.vaadin.server.UserError;
import com.vaadin.spring.annotation.UIScope;
import com.vaadin.ui.Button;
import com.vaadin.ui.ComboBox;
import com.vaadin.ui.Grid;
import com.vaadin.ui.PasswordField;
import com.vaadin.ui.TextField;
import com.vaadin.ui.UI;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.Window;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Arrays;

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
        userGrid.setSelectionMode(Grid.SelectionMode.NONE);
        userGrid.addColumn(User::getUsername, "Username");
        userGrid.addComponentColumn(user -> new MyCheckBox(user.getRoles().contains(UserManager.ROLE_ADMIN), e -> {
            if (!e.getValue() && user.getUsername().equals(SecurityUtils.getUsername())) {
                MyCheckBox checkBox = ((MyCheckBox) e.getComponent());
                checkBox.setComponentError(new UserError("Cannot revoke own admin privileges"));
                checkBox.setValue(true);
            } else {
                userManager.setAdmin(user, e.getValue());
            }
        })).setCaption("Admin");
        for (App app : dataManager.getApps()) {
            userGrid.addComponentColumn(user -> {
                Permission permission = user.getPermissions().stream().filter(p -> p.getApp().equals(app.getId())).findAny().orElseThrow(IllegalStateException::new);
                ComboBox<Permission.Level> levelComboBox = new ComboBox<>(null, Arrays.asList(Permission.Level.values()));
                levelComboBox.setEmptySelectionAllowed(false);
                levelComboBox.setValue(permission.getLevel());
                levelComboBox.addValueChangeListener(e -> userManager.setPermission(user, permission.getApp(), e.getValue()));
                return levelComboBox;
            }).setCaption("Access Permission for " + app.getName());
        }
        userGrid.setRowHeight(42);
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
