package com.faendir.acra.ui.view.user;

import com.faendir.acra.mongod.data.DataManager;
import com.faendir.acra.mongod.model.Permission;
import com.faendir.acra.ui.view.base.MyGrid;
import com.vaadin.data.HasValue;
import com.vaadin.shared.Registration;
import com.vaadin.ui.ComboBox;
import com.vaadin.ui.Grid;
import org.vaadin.hene.popupbutton.PopupButton;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.stream.Collectors;

/**
 * @author Lukas
 * @since 20.05.2017
 */
public class PermissionEditor extends PopupButton implements HasValue<Collection<Permission>> {

    private Collection<Permission> items;
    private MyGrid<Permission> grid;

    public PermissionEditor(DataManager dataManager) {
        super("Editor");
        items = new ArrayList<>();
        grid = new MyGrid<>(null, items);
        grid.addColumn(permission -> dataManager.getApp(permission.getApp()).getName(), "App");
        ComboBox<Permission.Level> levelComboBox = new ComboBox<>("", Arrays.asList(Permission.Level.values()));
        grid.addColumn(permission -> permission.getLevel().name(), "Level")
                .setEditorBinding(grid.getEditor().getBinder().bind(levelComboBox, Permission::getLevel, (permission, level) -> {
                    Collection<Permission> newValue = items.stream().map(p -> new Permission(p.getApp(), p.getLevel())).collect(Collectors.toList());
                    newValue.stream().filter(p -> p.getApp().equals(permission.getApp())).findAny().ifPresent(p -> p.setLevel(level));
                    setValue(newValue, true);
                }));
        grid.getEditor().setEnabled(true).setBuffered(false);
        grid.setSelectionMode(Grid.SelectionMode.NONE);
        setContent(grid);
        setPopupVisible(true);
    }

    @Override
    public void setValue(Collection<Permission> value) {
        setValue(value, false);
    }

    private void setValue(Collection<Permission> value, boolean userOriginated) {
        Collection<Permission> oldValue = items;
        items = value;
        grid.setItems(items);
        grid.setHeightByRows(items.size());
        fireEvent(new ValueChangeEvent<>(this, oldValue, userOriginated));
    }

    @Override
    public Collection<Permission> getValue() {
        return items;
    }

    @Override
    public void setRequiredIndicatorVisible(boolean requiredIndicatorVisible) {
    }

    @Override
    public boolean isRequiredIndicatorVisible() {
        return false;
    }

    @Override
    public Registration addValueChangeListener(ValueChangeListener<Collection<Permission>> listener) {
        return addListener(ValueChangeEvent.class, listener,
                ValueChangeListener.VALUE_CHANGE_METHOD);
    }

    @Override
    public void setReadOnly(boolean readOnly) {

    }

    @Override
    public boolean isReadOnly() {
        return false;
    }
}
