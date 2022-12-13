package com.faendir.acra.ui.view.app.tabs.admincards

import com.faendir.acra.i18n.Messages
import com.faendir.acra.navigation.RouteParams
import com.faendir.acra.navigation.View
import com.faendir.acra.persistence.app.AppRepository
import com.faendir.acra.persistence.app.CustomColumn
import com.faendir.acra.persistence.user.Permission
import com.faendir.acra.security.RequiresPermission
import com.faendir.acra.security.SecurityUtils
import com.faendir.acra.ui.component.AdminCard
import com.faendir.acra.ui.component.Translatable
import com.faendir.acra.ui.component.grid.column
import com.faendir.acra.ui.component.grid.renderer.ButtonRenderer
import com.faendir.acra.ui.ext.SizeUnit
import com.faendir.acra.ui.ext.acrariumGrid
import com.faendir.acra.ui.ext.content
import com.faendir.acra.ui.ext.setHeight
import com.faendir.acra.ui.ext.setMarginRight
import com.faendir.acra.ui.ext.setMinHeight
import com.vaadin.flow.component.button.Button
import com.vaadin.flow.component.html.Div
import com.vaadin.flow.component.icon.Icon
import com.vaadin.flow.component.icon.VaadinIcon
import com.vaadin.flow.component.textfield.TextField
import com.vaadin.flow.data.binder.Binder
import com.vaadin.flow.data.renderer.ComponentRenderer
import java.util.*

@View
@RequiresPermission(Permission.Level.EDIT)
class CustomColumnCard(
    appRepository: AppRepository,
    routeParams: RouteParams,
) : AdminCard() {

    private val appId = routeParams.appId()

    init {
        content {
            setHeader(Translatable.createLabel(Messages.CUSTOM_COLUMNS))
            val customColumns = appRepository.getCustomColumns(appId).toMutableList()
            acrariumGrid(customColumns) {
                setMinHeight(280, SizeUnit.PIXEL)
                setHeight(100, SizeUnit.PERCENTAGE)
                val pathColumn = column({ it.path }) {
                    isSortable = true
                    captionId = Messages.PATH
                    setFlexGrow(1)
                }
                val nameColumn = column({ it.name }) {
                    isSortable = true
                    captionId = Messages.NAME
                    setFlexGrow(1)
                }
                if (SecurityUtils.hasPermission(appId, Permission.Level.EDIT)) {
                    editor.binder = Binder(CustomColumn::class.java)
                    editor.isBuffered = true
                    val regex = Regex("[\\w_-]+(\\.[\\w_-]+)*")
                    val pathField = TextField().apply {
                        allowedCharPattern = "[\\w_.-]"
                        pattern = regex.pattern
                    }
                    val nameField = TextField().apply {
                        isRequired = true
                    }
                    editor.binder.forField(pathField)
                        .withValidator({ it.matches(regex) }, { getTranslation(Messages.ERROR_NOT_JSON_PATH) })
                        .bind({ it.path }, { old, value -> customColumns[customColumns.indexOf(old)] = old.copy(path = value) })
                    editor.binder.forField(nameField)
                        .bind({ it.name }, { old, value -> customColumns[customColumns.indexOf(old)] = old.copy(name = value) })
                    pathColumn.editorComponent = pathField
                    nameColumn.editorComponent = nameField
                    val editButtons = Collections.newSetFromMap<Button>(WeakHashMap())
                    editor.addOpenListener { editButtons.forEach { it.isEnabled = !editor.isOpen } }
                    editor.addCloseListener { editButtons.forEach { it.isEnabled = !editor.isOpen } }
                    val save = Translatable.createButton(Messages.SAVE) {
                        if (!pathField.isInvalid && !nameField.isInvalid) {
                            editor.save()
                            appRepository.setCustomColumns(appId, customColumns)
                            dataProvider.refreshAll()
                        }
                    }.with { setMarginRight(5.0, SizeUnit.PIXEL) }
                    val cancel = Translatable.createButton(Messages.CANCEL) {
                        editor.cancel()
                        dataProvider.refreshAll()
                    }
                    column(ButtonRenderer(VaadinIcon.EDIT, { editButtons.add(this) }) {
                        editor.editItem(it)
                        pathField.focus()
                        recalculateColumnWidths()
                    }) {
                        editorComponent = Div(save, cancel)
                        setFlexGrow(1)
                    }
                    column(ComponentRenderer { customColumn ->
                        Button(Icon(VaadinIcon.TRASH)) {
                            customColumns.remove(customColumn)
                            appRepository.setCustomColumns(appId, customColumns)
                            dataProvider.refreshAll()
                        }
                    })
                    appendFooterRow().getCell(columns[0]).component = Translatable.createButton(Messages.ADD_COLUMN) {
                        if (!editor.isOpen) {
                            val customColumn = CustomColumn("", "")
                            customColumns.add(customColumn)
                            dataProvider.refreshAll()
                            editor.editItem(customColumn)
                        }
                    }
                }
            }
        }
    }
}