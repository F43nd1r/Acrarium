/*
 * (C) Copyright 2023 Lukas Morawietz (https://github.com/F43nd1r)
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
package com.faendir.acra.common

import com.faendir.acra.annotation.AcrariumTest
import com.github.mvysny.kaributesting.v10.MockVaadin
import com.github.mvysny.kaributesting.v10.Routes
import com.github.mvysny.kaributesting.v10.spring.MockSpringServlet
import com.vaadin.flow.component.UI
import com.vaadin.flow.spring.SpringServlet
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.ApplicationContext


private val routes = Routes().autoDiscoverViews("com.faendir.acra.ui.view")

@AcrariumTest
abstract class UiTest {

    @Autowired
    private lateinit var applicationContext: ApplicationContext

    @BeforeEach
    fun setupMockVaadin() {
        val uiFactory = ::UI
        val servlet: SpringServlet = MockSpringServlet(routes, applicationContext, uiFactory)
        MockVaadin.setup(uiFactory, servlet)
    }

    @AfterEach
    fun tearDownMockVaadin() {
        MockVaadin.tearDown()
    }
}