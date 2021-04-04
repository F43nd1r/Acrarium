package com.faendir.acra.ui.ext

import com.faendir.acra.dataprovider.QueryDslDataProvider
import com.faendir.acra.model.User
import com.faendir.acra.service.AvatarService
import com.faendir.acra.service.UserService
import com.faendir.acra.ui.component.Box
import com.faendir.acra.ui.component.Card
import com.faendir.acra.ui.component.DownloadButton
import com.faendir.acra.ui.component.InstallationView
import com.faendir.acra.ui.component.Tab
import com.faendir.acra.ui.component.Translatable
import com.faendir.acra.ui.component.UserEditor
import com.faendir.acra.ui.component.grid.AcrariumGrid
import com.faendir.acra.ui.component.grid.QueryDslAcrariumGrid
import com.github.appreciated.layout.GridLayout
import com.vaadin.flow.component.AbstractField
import com.vaadin.flow.component.ClickEvent
import com.vaadin.flow.component.HasComponents
import com.vaadin.flow.component.button.Button
import com.vaadin.flow.component.checkbox.Checkbox
import com.vaadin.flow.component.combobox.ComboBox
import com.vaadin.flow.component.formlayout.FormLayout
import com.vaadin.flow.component.grid.Grid
import com.vaadin.flow.component.html.Anchor
import com.vaadin.flow.component.html.Div
import com.vaadin.flow.component.html.Image
import com.vaadin.flow.component.html.Label
import com.vaadin.flow.component.login.LoginForm
import com.vaadin.flow.component.login.LoginI18n
import com.vaadin.flow.component.orderedlayout.FlexLayout
import com.vaadin.flow.component.select.Select
import com.vaadin.flow.component.tabs.Tabs
import com.vaadin.flow.component.textfield.TextArea
import com.vaadin.flow.server.AbstractStreamResource

fun HasComponents.gridLayout(initializer: GridLayout.() -> Unit = {}) {
    add(GridLayout().apply(initializer))
}

fun HasComponents.translatableLabel(captionId: String, vararg params: Any, initializer: Label.() -> Unit = {}) {
    add(Translatable.createLabel(captionId, *params).with(initializer))
}

fun HasComponents.label(text: String, initializer: Label.() -> Unit = {}) {
    add(Label(text).apply(initializer))
}

fun HasComponents.installationView(avatarService: AvatarService, installationId: String) {
    add(InstallationView(avatarService).apply { setInstallationId(installationId) })
}

fun HasComponents.div(initializer: Div.() -> Unit = {}) {
    add(Div().apply(initializer))
}

fun <T> HasComponents.forEach(iterable: Iterable<T>, initializer: HasComponents.(T) -> Unit = {}) {
    iterable.forEach { initializer(it) }
}

fun HasComponents.anchor(resource: AbstractStreamResource, text: String, initializer: Anchor.() -> Unit = {}) {
    add(Anchor(resource, text).apply(initializer))
}

fun HasComponents.card(initializer: Card.() -> Unit = {}) {
    add(Card().apply(initializer))
}

fun HasComponents.flexLayout(initializer: FlexLayout.() -> Unit = {}): FlexLayout {
    return FlexLayout().apply(initializer).also { add(it) }
}

fun HasComponents.translatableImage(src: String, captionId: String, vararg params: Any, initializer: Image.() -> Unit = {}) {
    add(Translatable.createImage(src, captionId, *params).with(initializer))
}

fun HasComponents.loginForm(loginI18n: LoginI18n, initializer: LoginForm.() -> Unit = {}) {
    add(LoginForm(loginI18n).apply(initializer))
}

fun HasComponents.userEditor(userService: UserService, user: User, isExistingUser: Boolean, onSuccess: () -> Unit) {
    add(UserEditor(userService, user, isExistingUser, onSuccess))
}

fun <T> HasComponents.queryDslAcrariumGrid(dataProvider: QueryDslDataProvider<T>, initializer: QueryDslAcrariumGrid<T>.() -> Unit): QueryDslAcrariumGrid<T> {
    val grid = QueryDslAcrariumGrid(dataProvider)
    grid.initializer()
    add(grid)
    return grid
}

fun HasComponents.translatableButton(captionId: String, vararg params: Any, clickListener: ((ClickEvent<Button>) -> Unit) = {}): Translatable<Button> {
    return Translatable.createButton(captionId, params, clickListener).also { add(it) }
}

fun HasComponents.formLayout(initializer: FormLayout.() -> Unit = {}) {
    add(FormLayout().apply(initializer))
}

fun HasComponents.translatableCheckbox(captionId: String, vararg params: Any, initializer: Checkbox.() -> Unit = {}) {
    add(Translatable.createCheckbox(captionId, *params).with(initializer))
}

fun <T> HasComponents.translatableSelect(items: Collection<T>, captionId: String, vararg params: Any, initializer: Select<T>.() -> Unit = {}) {
    add(Translatable.createSelect(items, captionId, *params).with(initializer))
}

fun HasComponents.tabs(initializer: Tabs.() -> Unit = {}): Tabs {
    return Tabs().apply(initializer).also { add(it) }
}

fun HasComponents.tab(captionId: String, vararg params: Any, initializer: Tab.() -> Unit = {}) {
    add(Tab(captionId, *params).apply(initializer))
}

fun HasComponents.box(titleId: String, detailsId: String, buttonId: String, clickListener: ((ClickEvent<Button>) -> Unit) = {}): Box {
    return Box(Translatable.createLabel(titleId), Translatable.createLabel(detailsId), Translatable.createButton(buttonId, clickListener)).also { add(it) }
}

fun <T> HasComponents.comboBox(items: Collection<T>, captionId: String, vararg params: Any, initializer: ComboBox<T>.() -> Unit = {})
        : Translatable.ValidatedValue<ComboBox<T>, AbstractField.ComponentValueChangeEvent<ComboBox<T>, T>, T> {
    return Translatable.createComboBox(items, captionId, *params).with(initializer).also { add(it) }
}

fun HasComponents.downloadButton(resource: AbstractStreamResource, captionId: String, vararg params: Any, initializer: DownloadButton.() -> Unit = {}) {
    add(DownloadButton(resource, captionId, *params).apply(initializer))
}

fun HasComponents.translatableTextArea(captionId: String, vararg params: Any, initializer: TextArea.() -> Unit = {})
        : Translatable.ValidatedValue<TextArea, AbstractField.ComponentValueChangeEvent<TextArea, String>, String> {
    return Translatable.createTextArea(captionId, *params).with(initializer).also { add(it) }
}

fun <T> HasComponents.acrariumGrid(content: Collection<T>, initializer: AcrariumGrid<T>.() -> Unit): Grid<T> {
    return AcrariumGrid<T>().apply { setItems(content) }.apply(initializer).also { add(it) }
}