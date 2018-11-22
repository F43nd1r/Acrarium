package com.faendir.acra.ui.view.user;

import com.faendir.acra.i18n.Messages;
import com.faendir.acra.model.User;
import com.faendir.acra.security.SecurityUtils;
import com.faendir.acra.service.UserService;
import com.faendir.acra.ui.base.HasRoute;
import com.faendir.acra.ui.base.Path;
import com.faendir.acra.ui.component.FlexLayout;
import com.faendir.acra.ui.component.Translatable;
import com.faendir.acra.ui.view.MainView;
import com.faendir.acra.ui.view.Overview;
import com.vaadin.flow.component.Composite;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.textfield.PasswordField;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.spring.annotation.SpringComponent;
import com.vaadin.flow.spring.annotation.UIScope;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.lang.NonNull;

/**
 * @author lukas
 * @since 16.11.18
 */
@UIScope
@SpringComponent
@Route(value = "password", layout = MainView.class)
public class ChangePasswordView extends Composite<FlexLayout> implements HasRoute {
    @Autowired
    public ChangePasswordView(UserService userService) {
        Translatable<PasswordField> oldPassword = Translatable.createPasswordField(Messages.OLD_PASSWORD);
        getContent().add(oldPassword);
        Translatable<PasswordField> newPassword = Translatable.createPasswordField(Messages.NEW_PASSWORD);
        getContent().add(newPassword);
        Translatable<PasswordField> repeatPassword = Translatable.createPasswordField(Messages.REPEAT_PASSWORD);
        getContent().add(repeatPassword);
        FlexLayout layout = new FlexLayout(oldPassword, newPassword, repeatPassword, Translatable.createButton(e -> {
            User user = userService.getUser(SecurityUtils.getUsername());
            assert user != null;
            if (newPassword.getContent().getValue().equals(repeatPassword.getContent().getValue())) {
                if (userService.changePassword(user, oldPassword.getContent().getValue(), newPassword.getContent().getValue())) {
                    Notification.show(getTranslation(Messages.SUCCESS));
                    UI.getCurrent().navigate(Overview.class);
                } else {
                    oldPassword.getContent().setErrorMessage(getTranslation(Messages.INCORRECT_PASSWORD));
                    oldPassword.getContent().setInvalid(true);
                    oldPassword.getContent().addValueChangeListener(event -> {
                        oldPassword.getContent().setInvalid(false);
                        event.unregisterListener();
                    });
                }
            } else {
                repeatPassword.getContent().setErrorMessage(getTranslation(Messages.PASSWORDS_NOT_MATCHING));
                repeatPassword.getContent().setInvalid(true);
                repeatPassword.getContent().addValueChangeListener(event -> {
                    repeatPassword.getContent().setInvalid(false);
                    event.unregisterListener();
                });
            }
        }, Messages.CONFIRM));
        layout.setFlexDirection(FlexLayout.FlexDirection.COLUMN);
        getContent().add(layout);
        getContent().setSizeFull();
        getContent().setJustifyContentMode(FlexComponent.JustifyContentMode.CENTER);
        getContent().setAlignItems(FlexComponent.Alignment.CENTER);
    }

    @NonNull
    @Override
    public Path.Element<?> getPathElement() {
        return new Path.Element<>(getClass(), Messages.CHANGE_PASSWORD);
    }

    @Override
    public Parent<?> getLogicalParent() {
        return new Parent<>(Overview.class);
    }
}
