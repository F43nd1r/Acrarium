package com.faendir.acra.navigation

import com.vaadin.flow.spring.annotation.SpringComponent
import org.springframework.beans.factory.config.ConfigurableBeanFactory
import org.springframework.context.annotation.Scope
import org.springframework.stereotype.Component
import java.lang.annotation.Inherited

@Inherited
@Target(AnnotationTarget.CLASS)
@Retention(AnnotationRetention.RUNTIME)
@Scope(scopeName = ConfigurableBeanFactory.SCOPE_PROTOTYPE)
@SpringComponent
annotation class View(
    /**
     * The value may indicate a suggestion for a logical component name, to be
     * turned into a Spring bean in case of an autodetected component.
     *
     * @see Component.value
     * @return the suggested component name, if any
     */
    val value: String = ""
)
