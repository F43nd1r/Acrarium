package com.faendir.acra.ui.ext

import com.faendir.acra.dataprovider.AcrariumDataProvider
import com.faendir.acra.domain.AvatarService
import com.faendir.acra.persistence.app.Reporter
import com.faendir.acra.persistence.user.Role
import com.faendir.acra.ui.component.*
import com.faendir.acra.ui.component.grid.BasicCustomColumnGrid
import com.faendir.acra.ui.component.grid.BasicLayoutPersistingFilterableGrid
import com.faendir.acra.ui.component.tabs.Tab
import com.vaadin.flow.component.*
import com.vaadin.flow.component.button.Button
import com.vaadin.flow.component.checkbox.Checkbox
import com.vaadin.flow.component.combobox.ComboBox
import com.vaadin.flow.component.formlayout.FormLayout
import com.vaadin.flow.component.grid.Grid
import com.vaadin.flow.component.html.*
import com.vaadin.flow.component.login.LoginForm
import com.vaadin.flow.component.login.LoginI18n
import com.vaadin.flow.component.orderedlayout.FlexLayout
import com.vaadin.flow.component.select.Select
import com.vaadin.flow.component.tabs.Tabs
import com.vaadin.flow.component.textfield.NumberField
import com.vaadin.flow.component.textfield.TextArea
import com.vaadin.flow.component.textfield.TextField
import com.vaadin.flow.router.RouteParameters
import com.vaadin.flow.router.RouterLink
import com.vaadin.flow.server.AbstractStreamResource

fun HasComponents.gridLayout(initializer: CssGridLayout.() -> Unit = {}) {
    add(CssGridLayout().apply(initializer))
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

fun HasComponents.userEditor(userRepository: com.faendir.acra.persistence.user.UserRepository, grantRoles: Set<Role>, onSuccess: () -> Unit) {
    add(UserEditor(userRepository, null, null, grantRoles, onSuccess))
}

fun <T : Any, F : Any, S : Any> HasComponents.basicLayoutPersistingFilterableGrid(
    dataProvider: AcrariumDataProvider<T, F, S>,
    initializer: BasicLayoutPersistingFilterableGrid<T, F, S>.() -> Unit
): BasicLayoutPersistingFilterableGrid<T, F, S> {
    val grid = BasicLayoutPersistingFilterableGrid(dataProvider)
    grid.initializer()
    add(grid)
    return grid
}

fun HasComponents.translatableButton(
    captionId: String,
    vararg params: Any,
    clickListener: ((ClickEvent<Button>) -> Unit) = {}
): Translatable<Button> {
    return Translatable.createButton(captionId, *params, clickListener = clickListener).also { add(it) }
}

fun HasComponents.formLayout(initializer: FormLayout.() -> Unit = {}) {
    add(FormLayout().apply(initializer))
}

fun HasComponents.translatableCheckbox(
    captionId: String,
    vararg params: Any,
    initializer: Checkbox.() -> Unit = {}
): Translatable.Value<Checkbox, *, Boolean> {
    return Translatable.createCheckbox(captionId, *params).with(initializer).also { add(it) }
}

fun HasComponents.checkbox(initializer: Checkbox.() -> Unit = {}) {
    add(Checkbox().apply(initializer))
}

fun <T> HasComponents.translatableSelect(
    items: Collection<T>,
    getLabel: (T) -> String,
    captionId: String,
    vararg params: Any,
    initializer: Select<T>.() -> Unit = {}
) {
    add(Translatable.createSelect(items, getLabel, captionId, *params).with(initializer))
}

fun HasComponents.tabs(initializer: Tabs.() -> Unit = {}): Tabs {
    return Tabs().apply(initializer).also { add(it) }
}

fun HasComponents.tab(captionId: String, vararg params: Any, initializer: Tab.() -> Unit = {}) {
    add(Tab(captionId, *params).apply(initializer))
}

fun HasComponents.box(titleId: String, detailsId: String, buttonId: String, clickListener: (ClickEvent<Button>) -> Unit = {}): Box {
    return Box(
        Translatable.createLabel(titleId),
        Translatable.createLabel(detailsId),
        Translatable.createButton(buttonId, clickListener = clickListener)
    ).also { add(it) }
}

fun <T> HasComponents.comboBox(items: Collection<T>, captionId: String, vararg params: Any, initializer: ComboBox<T>.() -> Unit = {})
        : Translatable.ValidatedValue<ComboBox<T>, AbstractField.ComponentValueChangeEvent<ComboBox<T>, T>, T> {
    return Translatable.createComboBox(items, captionId, *params).with(initializer).also { add(it) }
}

fun HasComponents.downloadButton(
    resource: AbstractStreamResource,
    captionId: String,
    vararg params: Any,
    initializer: DownloadButton.() -> Unit = {}
) {
    add(DownloadButton(resource, captionId, *params).apply(initializer))
}

fun HasComponents.translatableTextArea(captionId: String, vararg params: Any, initializer: TextArea.() -> Unit = {})
        : Translatable.ValidatedValue<TextArea, AbstractField.ComponentValueChangeEvent<TextArea, String>, String> {
    return Translatable.createTextArea(captionId, *params).with(initializer).also { add(it) }
}

fun <T> HasComponents.acrariumGrid(content: Collection<T>, initializer: BasicCustomColumnGrid<T>.() -> Unit): Grid<T> {
    return BasicCustomColumnGrid<T>().apply { setItems(content) }.apply(initializer).also { add(it) }
}

fun HasComponents.translatableText(captionId: String, vararg params: Any, initializer: Text.() -> Unit = {}) {
    add(Translatable.createText(captionId, *params).with(initializer))
}

fun HasComponents.configurationLabel(user: Reporter) {
    add(ConfigurationLabel.forReporter(user))
}

fun HasComponents.translatableRangeField(
    captionId: String,
    vararg params: Any,
    initializer: RangeField.() -> Unit = {}
): Translatable.ValidatedValue<RangeField, *, Double> {
    return Translatable.createRangeField(captionId, params).with(initializer).also { add(it) }
}

fun HasComponents.translatableNumberField(
    captionId: String,
    vararg params: Any,
    initializer: NumberField.() -> Unit = {}
): Translatable.ValidatedValue<NumberField, *, Double> {
    return Translatable.createNumberField(captionId, params).with(initializer).also { add(it) }
}

fun HasComponents.paragraph(text: String, initializer: Paragraph.() -> Unit = {}) {
    add(Paragraph(text).apply(initializer))
}

fun HasComponents.translatableParagraph(captionId: String, vararg params: Any, initializer: Paragraph.() -> Unit = {}) {
    add(Translatable.createP(captionId, *params).with(initializer))
}

fun HasComponents.translatableTextField(
    captionId: String,
    vararg params: Any,
    initializer: TextField.() -> Unit = {}
): Translatable.ValidatedValue<TextField, *, String> {
    return Translatable.createTextField(captionId, params).with(initializer).also { add(it) }
}

fun HasComponents.routerLink(
    navigationTarget: Class<out Component>,
    parameters: RouteParameters = RouteParameters.empty(),
    initializer: RouterLink.() -> Unit
): RouterLink {
    return RouterLink(navigationTarget, parameters).apply(initializer).also { add(it) }
}