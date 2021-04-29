package com.faendir.acra.ui.view.error

import com.faendir.acra.i18n.Messages
import com.faendir.acra.navigation.View
import com.faendir.acra.ui.ext.SizeUnit
import com.faendir.acra.ui.ext.paragraph
import com.faendir.acra.ui.ext.setMargin
import com.faendir.acra.ui.ext.translatableButton
import com.faendir.acra.ui.ext.translatableParagraph
import com.faendir.acra.ui.view.Overview
import com.vaadin.flow.component.orderedlayout.FlexComponent
import com.vaadin.flow.component.orderedlayout.FlexLayout
import com.vaadin.flow.router.BeforeEnterEvent
import com.vaadin.flow.router.ErrorParameter
import com.vaadin.flow.router.HasErrorParameter
import com.vaadin.flow.router.RouterLink
import mu.KotlinLogging

private val logger = KotlinLogging.logger {}

@Suppress("LeakingThis")
@View
class UnexpectedErrorView : FlexLayout(), HasErrorParameter<Exception> {
    init {
        setSizeFull()
        setFlexDirection(FlexDirection.COLUMN)
        alignItems = FlexComponent.Alignment.CENTER
        justifyContentMode = FlexComponent.JustifyContentMode.CENTER
        paragraph("500") {
            style["font-size"] = "200px"
            style["line-height"] = "80%"
            setMargin(0, SizeUnit.PIXEL)
        }
        translatableParagraph(Messages.SOMETHING_WENT_WRONG)
        add(RouterLink("", Overview::class.java).apply {
            translatableButton(Messages.GO_HOME)
        })
    }

    override fun setErrorParameter(event: BeforeEnterEvent?, parameter: ErrorParameter<Exception>): Int {
        logger.warn("Unexpected Error", parameter.exception)
        return 500
    }
}