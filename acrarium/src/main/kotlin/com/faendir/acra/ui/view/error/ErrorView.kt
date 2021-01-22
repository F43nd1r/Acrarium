package com.faendir.acra.ui.view.error

import com.faendir.acra.i18n.Messages
import com.faendir.acra.ui.component.Translatable
import com.faendir.acra.ui.ext.Unit
import com.faendir.acra.ui.ext.setMargin
import com.faendir.acra.ui.view.Overview
import com.vaadin.flow.component.html.Paragraph
import com.vaadin.flow.component.orderedlayout.FlexComponent
import com.vaadin.flow.component.orderedlayout.FlexLayout
import com.vaadin.flow.router.BeforeEnterEvent
import com.vaadin.flow.router.ErrorParameter
import com.vaadin.flow.router.HasErrorParameter
import com.vaadin.flow.router.NotFoundException
import com.vaadin.flow.router.RouteNotFoundError
import com.vaadin.flow.router.RouterLink
import com.vaadin.flow.spring.annotation.SpringComponent
import com.vaadin.flow.spring.annotation.UIScope
import com.vaadin.flow.spring.router.SpringRouteNotFoundError

@Suppress("LeakingThis")
@UIScope
@SpringComponent
class ErrorView :
		SpringRouteNotFoundError() /*need to extend RouteNotFoundError due to vaadin-spring bug:
        TODO: Remove when https://github.com/vaadin/spring/issues/661 is fixed*/,
        HasErrorParameter<NotFoundException> {
    val layout = FlexLayout().apply {
        setSizeFull()
        setFlexDirection(FlexLayout.FlexDirection.COLUMN)
        alignItems = FlexComponent.Alignment.CENTER
        justifyContentMode = FlexComponent.JustifyContentMode.CENTER
    }

    init {
        element.appendChild(layout.element)
    }

    override fun setErrorParameter(event: BeforeEnterEvent?, parameter: ErrorParameter<NotFoundException>): Int {
        layout.removeAll()
        val number = Paragraph("404")
        number.style["font-size"] = "200px"
        number.style["line-height"] = "80%"
        number.setMargin(0, Unit.PIXEL)
        val text = Translatable.createP(Messages.URL_NOT_FOUND)
        val button = RouterLink("", Overview::class.java).apply {
            add(Translatable.createButton(Messages.GO_HOME))
        }
        layout.add(number, text, button)
        return 404
    }
}