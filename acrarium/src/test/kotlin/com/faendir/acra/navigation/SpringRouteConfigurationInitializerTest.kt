package com.faendir.acra.navigation

import com.faendir.acra.annotation.AcrariumTest
import com.faendir.acra.ui.view.bug.tabs.ReportTab
import com.faendir.acra.util.PARAM_APP
import com.faendir.acra.util.PARAM_BUG
import com.vaadin.flow.router.RouteConfiguration
import com.vaadin.flow.router.RouteParameters
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import strikt.api.expectThat
import strikt.assertions.isEqualTo

@AcrariumTest
@SpringBootTest
class SpringRouteConfigurationInitializerTest {

    @Autowired
    lateinit var routeConfiguration: RouteConfiguration

    @Test
    fun `should resolve urls`() {
        expectThat(
            routeConfiguration.getUrl(
                ReportTab::class.java, RouteParameters(
                    mapOf(
                        PARAM_APP to "1",
                        PARAM_BUG to "2"
                    )
                )
            )
        ).isEqualTo("app/1/bug/2/report")
    }
}