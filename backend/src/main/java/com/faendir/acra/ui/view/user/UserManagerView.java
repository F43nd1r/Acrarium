package com.faendir.acra.ui.view.user;

import com.faendir.acra.mongod.data.DataManager;
import com.faendir.acra.mongod.model.App;
import com.faendir.acra.mongod.model.Permission;
import com.faendir.acra.mongod.model.User;
import com.faendir.acra.mongod.user.UserManager;
import com.faendir.acra.security.SecurityUtils;
import com.faendir.acra.ui.view.annotation.RequiresRole;
import com.faendir.acra.ui.view.base.MyCheckBox;
import com.faendir.acra.ui.view.base.MyGrid;
import com.faendir.acra.ui.view.base.NamedView;
import com.faendir.acra.ui.view.base.ValidatedField;
import com.faendir.acra.util.Style;
import com.vaadin.navigator.ViewChangeListener;
import com.vaadin.spring.annotation.SpringView;
import com.vaadin.ui.Button;
import com.vaadin.ui.ComboBox;
import com.vaadin.ui.FormLayout;
import com.vaadin.ui.Grid;
import com.vaadin.ui.PasswordField;
import com.vaadin.ui.TextField;
import com.vaadin.ui.UI;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.Window;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Arrays;
import java.util.Collections;

/**
 * @author Lukas
 * @since 20.05.2017
 */
@SpringView(name = "user-manager")
@RequiresRole(UserManager.ROLE_ADMIN)
public class UserManagerView extends NamedView {
    @NotNull private final UserManager userManager;
    @NotNull private final DataManager dataManager;
    @NotNull private final MyGrid<User> userGrid;

    @Autowired
    public UserManagerView(@NotNull UserManager userManager, @NotNull DataManager dataManager) {
        this.userManager = userManager;
        this.dataManager = dataManager;
        this.userGrid = new MyGrid<>("Users", Collections.emptyList());
    }

    @Override
    public void enter(ViewChangeListener.ViewChangeEvent event) {
        userGrid.setItems(userManager.getUsers());
        userGrid.setSelectionMode(Grid.SelectionMode.NONE);
        userGrid.addColumn(User::getUsername, "Username");
        userGrid.addComponentColumn(user -> new MyCheckBox(user.getRoles().contains(UserManager.ROLE_ADMIN), !user.getUsername().equals(SecurityUtils.getUsername()),
                                                           e -> userManager.setAdmin(user, e.getValue()))).setCaption("Admin");
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
        Style.NO_PADDING.apply(layout);
        setCompositionRoot(layout);
        userGrid.setWidth(100, Unit.PERCENTAGE);
        setSizeFull();
        Style.apply(this, Style.PADDING_LEFT, Style.PADDING_RIGHT, Style.PADDING_BOTTOM);
    }

    private void newUser() {
        Window window = new Window("New User");
        ValidatedField<String, TextField> name = new ValidatedField<>(new TextField("Username")).addValidator(s -> !s.isEmpty(), "Username cannot be empty")
                .addValidator(s -> userManager.getUser(s) == null, "User already exists");
        ValidatedField<String, PasswordField> password = new ValidatedField<>(new PasswordField("Password")).addValidator(s -> !s.isEmpty(), "Password cannot be empty");
        ValidatedField<String, PasswordField> repeatPassword = new ValidatedField<>(new PasswordField("Repeat Password"))
                .addValidator(s -> s.equals(password.getValue()), "Passwords do not match");
        Button create = new Button("Create");
        create.addClickListener(e -> {
            if (name.isValid() && password.isValid() && repeatPassword.isValid()) {
                userManager.createUser(name.getValue().toLowerCase(), password.getValue());
                userGrid.setItems(userManager.getUsers());
                window.close();
            }
        });
        create.setWidth(100, Unit.PERCENTAGE);
        FormLayout layout = new FormLayout(name.getField(), password.getField(), repeatPassword.getField(), create);
        window.setContent(layout);
        window.center();
        UI.getCurrent().addWindow(window);
    }
}
