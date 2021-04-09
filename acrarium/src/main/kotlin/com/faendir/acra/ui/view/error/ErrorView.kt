package com.faendir.acra.ui.view.error

import com.faendir.acra.i18n.Messages
import com.faendir.acra.navigation.View
import com.faendir.acra.ui.component.Translatable
import com.faendir.acra.ui.ext.SizeUnit
import com.faendir.acra.ui.ext.flexLayout
import com.faendir.acra.ui.ext.paragraph
import com.faendir.acra.ui.ext.setMargin
import com.faendir.acra.ui.ext.translatableButton
import com.faendir.acra.ui.ext.translatableParagraph
import com.faendir.acra.ui.view.Overview
import com.vaadin.flow.component.HasComponents
import com.vaadin.flow.component.HasSize
import com.vaadin.flow.component.html.Paragraph
import com.vaadin.flow.component.orderedlayout.FlexComponent
import com.vaadin.flow.component.orderedlayout.FlexLayout
import com.vaadin.flow.router.BeforeEnterEvent
import com.vaadin.flow.router.ErrorParameter
import com.vaadin.flow.router.HasErrorParameter
import com.vaadin.flow.router.NotFoundException
import com.vaadin.flow.router.RouterLink
import com.vaadin.flow.spring.annotation.SpringComponent
import com.vaadin.flow.spring.annotation.UIScope
import com.vaadin.flow.spring.router.SpringRouteNotFoundError

@Suppress("LeakingThis")
@View
class ErrorView :
		SpringRouteNotFoundError() /*need to extend RouteNotFoundError due to vaadin-spring bug:
        TODO: Remove when https://github.com/vaadin/spring/issues/661 is fixed*/,
        HasErrorParameter<NotFoundException>, HasComponents, HasSize {
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