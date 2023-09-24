/*
 * (C) Copyright 2021-2023 Lukas Morawietz (https://github.com/F43nd1r)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.faendir.acra.navigation

import com.faendir.acra.persistence.user.Role
import com.faendir.acra.security.RequiresRole
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
@RequiresRole(Role.USER)
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
