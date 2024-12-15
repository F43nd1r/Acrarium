/*
 * (C) Copyright 2023-2024 Lukas Morawietz (https://github.com/F43nd1r)
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
import com.faendir.acra.ui.view.error.ErrorView
import com.github.mvysny.fakeservlet.FakeRequest
import com.github.mvysny.kaributesting.v10.MockRouteAccessDeniedError
import com.github.mvysny.kaributesting.v10.MockVaadin
import com.github.mvysny.kaributesting.v10.Routes
import com.github.mvysny.kaributesting.v10._expectOne
import com.github.mvysny.kaributesting.v10.spring.MockSpringServlet
import com.vaadin.flow.component.Component
import com.vaadin.flow.component.UI
import com.vaadin.flow.router.RouteParameters
import org.junit.jupiter.api.*
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.ApplicationContext
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.Authentication
import org.springframework.security.core.GrantedAuthority
import org.springframework.security.core.context.SecurityContextHolder
import java.util.*
import kotlin.reflect.KClass


private val routes = Routes().autoDiscoverViews("com.faendir.acra.ui.view")

@AcrariumTest
abstract class UiTest {
    val TEST_USER = "test_user"

    @Autowired
    private lateinit var applicationContext: ApplicationContext

    private lateinit var uiParams: UiParams

    abstract fun setup(): UiParams

    private val previousAuthentications = Stack<Authentication>()

    private fun setAuthentication(authorities: Collection<GrantedAuthority>) {
        previousAuthentications.push(SecurityContextHolder.getContext().authentication)
        SecurityContextHolder.getContext().authentication = UsernamePasswordAuthenticationToken(TEST_USER, null, authorities)
    }

    private fun resetAuthentication() {
        SecurityContextHolder.getContext().authentication = previousAuthentications.pop()
    }

    private fun navigateTo() {
        UI.getCurrent().navigate(uiParams.route.java, RouteParameters(uiParams.routeParameters))
    }

    fun reload() {
        UI.getCurrent().page.reload()
    }

    @BeforeEach
    fun setup_internal() {
        uiParams = setup()
        val uiFactory = ::UI
        routes.errorRoutes.remove(MockRouteAccessDeniedError::class.java)
        val servlet = MockSpringServlet(routes, applicationContext, uiFactory)
        MockVaadin.setup(uiFactory, servlet)
        MockVaadin.mockRequestFactory = {
            object : FakeRequest(it) {
                override fun getUserPrincipal() = SecurityContextHolder.getContext().authentication

                override fun getProtocolRequestId() = throw UnsupportedOperationException()
                override fun getRequestId() = throw UnsupportedOperationException()
                override fun getServletConnection() = throw UnsupportedOperationException()
            }
        }

        setAuthentication(uiParams.requiredAuthorities)
        navigateTo()
    }

    @AfterEach
    fun teardown_internal() {
        resetAuthentication()
        MockVaadin.tearDown()
    }

    @Test
    fun `should load`() {
        _expectOne(uiParams.route.java)
    }

    @TestFactory
    fun `test required authorities`() = uiParams.requiredAuthorities.map {
        DynamicTest.dynamicTest("should not load without $it") {
            setAuthentication(uiParams.requiredAuthorities - it)
            navigateTo()
            _expectOne<ErrorView>()
            resetAuthentication()
        }
    }

    fun withAuth(vararg extraAuthorities: GrantedAuthority, block: () -> Unit) {
        setAuthentication(uiParams.requiredAuthorities + extraAuthorities)
        reload()
        try {
            block()
        } finally {
            resetAuthentication()
        }
    }
}

data class UiParams(
    val route: KClass<out Component>,
    val routeParameters: Map<String, String> = emptyMap(),
    val requiredAuthorities: Set<GrantedAuthority>
)