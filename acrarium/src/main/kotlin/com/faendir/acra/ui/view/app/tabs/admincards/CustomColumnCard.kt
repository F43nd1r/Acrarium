package com.faendir.acra.ui.view.app.tabs.admincards

import com.faendir.acra.i18n.Messages
import com.faendir.acra.model.App
import com.faendir.acra.model.Permission
import com.faendir.acra.navigation.View
import com.faendir.acra.security.SecurityUtils
import com.faendir.acra.service.DataService
import com.faendir.acra.ui.component.AdminCard
import com.faendir.acra.ui.component.Translatable
import com.faendir.acra.ui.component.grid.column
import com.faendir.acra.ui.ext.SizeUnit
import com.faendir.acra.ui.ext.acrariumGrid
import com.faendir.acra.ui.ext.content
import com.faendir.acra.ui.ext.setHeight
import com.faendir.acra.ui.ext.setMarginRight
import com.faendir.acra.ui.ext.setMinHeight
import com.faendir.acra.util.PARAM
import com.vaadin.flow.component.button.Button
import com.vaadin.flow.component.html.Div
import com.vaadin.flow.component.icon.Icon
import com.vaadin.flow.component.icon.VaadinIcon
import com.vaadin.flow.component.textfield.TextField
import com.vaadin.flow.data.binder.Binder
import com.vaadin.flow.data.renderer.ComponentRenderer
import org.springframework.beans.factory.annotation.Qualifier
import java.util.*

@View
class CustomColumnCard(dataService: DataService, @Qualifier(PARAM) app: App) : AdminCard(dataService) {

    init {
        content {
            setHeader(Translatable.createLabel(Messages.CUSTOM_COLUMNS))
            acrariumGrid(app.configuration.customReportColumns) {
                setMinHeight(280, SizeUnit.PIXEL)
                setHeight(100, SizeUnit.PERCENTAGE)
                val column = column({ it }) {
                    isSortable = true
                    captionId = Messages.CUSTOM_COLUMNS
                    setFlexGrow(1)
                }
                if (SecurityUtils.hasPermission(app, Permission.Level.EDIT)) {
                    editor.binder = Binder(String::class.java)
                    editor.isBuffered = true
                    val regex = Regex("[\\w_-]+(\\.[\\w_-]+)*")
                    val field = TextField().apply {
                        isPreventInvalidInput = true
                        pattern = regex.pattern
                    }
                    editor.binder.forField(field)
                        .withValidator({ it.matches(regex) }, { getTranslation(Messages.ERROR_NOT_JSON_PATH) })
                        .bind({ it }, { old, value ->
                            app.configuration.customReportColumns.apply {
                                set(indexOf(old), value)
                            }
                            dataService.store(app)
                        })
                    column.editorComponent = field
                    val editButtons = Collections.newSetFromMap<Button>(WeakHashMap())
                    editor.addOpenListener { editButtons.forEach { it.isEnabled = !editor.isOpen } }
                    editor.addCloseListener { editButtons.forEach { it.isEnabled = !editor.isOpen } }
                    val save = Translatable.createButton(Messages.SAVE) {
                        if (!field.isInvalid) {
                            editor.save()
                            app.configuration.customReportColumns.remove("")
                            dataProvider.refreshAll()
                        }
                    }.with { setMarginRight(5.0, SizeUnit.PIXEL) }
                    val cancel = Translatable.createButton(Messages.CANCEL) {
                        editor.cancel()
                        app.configuration.customReportColumns.remove("")
                        dataProvider.refreshAll()
                    }
                    column(ComponentRenderer { string ->
                        Button(Icon(VaadinIcon.EDIT)) {
                            editor.editItem(string)
                            field.focus()
                            recalculateColumnWidths()
                        }.also { editButtons.add(it) }
                    }) {
                        editorComponent = Div(save, cancel)
                        setFlexGrow(1)
                    }
                    column(ComponentRenderer { string ->
                        Button(Icon(VaadinIcon.TRASH)) {
                            app.configuration.customReportColumns.remove("")
                            app.configuration.customReportColumns.remove(string)
                            dataService.store(app)
                            dataProvider.refreshAll()
                        }
                    })
                    appendFooterRow().getCell(columns[0]).setComponent(
                        Translatable.createButton(Messages.ADD_COLUMN) {
                            if (!app.configuration.customReportColumns.contains("")) {
                                app.configuration.customReportColumns.add("")
                            }
                            dataProvider.refreshAll()
                            editor.editItem("")
                        })
                }
            }
        }
    }
}