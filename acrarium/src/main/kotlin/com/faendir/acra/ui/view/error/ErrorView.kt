package com.faendir.acra.ui.view.error

import com.faendir.acra.i18n.Messages
import com.faendir.acra.navigation.View
import com.faendir.acra.ui.ext.*
import com.faendir.acra.ui.view.Overview
import com.vaadin.flow.component.HasComponents
import com.vaadin.flow.component.HasSize
import com.vaadin.flow.component.orderedlayout.FlexComponent
import com.vaadin.flow.component.orderedlayout.FlexLayout
import com.vaadin.flow.router.*

@Suppress("LeakingThis")
@View
class ErrorView : RouteNotFoundError(), HasComponents, HasSize {
    init {
        setSizeFull()
        flexLayout {
            setSizeFull()
            setFlexDirection(FlexLayout.FlexDirection.COLUMN)
            alignItems = FlexComponent.Alignment.CENTER
            justifyContentMode = FlexComponent.JustifyContentMode.CENTER
            paragraph("404") {
                style["font-size"] = "200px"
                style["line-height"] = "80%"
                setMargin(0, SizeUnit.PIXEL)
            }
            translatableParagraph(Messages.URL_NOT_FOUND)
            add(RouterLink("", Overview::class.java).apply {
                translatableButton(Messages.GO_HOME)
            })
        }
    }

    override fun setErrorParameter(event: BeforeEnterEvent?, parameter: ErrorParameter<NotFoundException>): Int = 404
}