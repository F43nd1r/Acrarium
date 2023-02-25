package com.faendir.acra.persistence.app

import com.faendir.acra.annotation.PersistenceTest
import com.faendir.acra.dataprovider.AcrariumDataProvider
import com.faendir.acra.dataprovider.AcrariumSort
import com.faendir.acra.persistence.TestDataBuilder
import com.faendir.acra.persistence.user.Permission
import com.faendir.acra.persistence.user.Role
import com.faendir.acra.persistence.user.UserRepository
import com.ninjasquad.springmockk.MockkBean
import com.ninjasquad.springmockk.SpykBean
import com.vaadin.flow.data.provider.SortDirection
import io.mockk.every
import io.mockk.verify
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.security.authentication.TestingAuthenticationToken
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.security.core.context.SecurityContextImpl
import org.springframework.security.crypto.password.PasswordEncoder
import strikt.api.expectThat
import strikt.assertions.*

@PersistenceTest
class AppRepositoryTest(
    @Autowired
    private val appRepository: AppRepository,
    @Autowired
    private val testDataBuilder: TestDataBuilder,
    @Autowired
    @SpykBean
    private val userRepository: UserRepository,
    @Autowired
    @MockkBean
    private val passwordEncoder: PasswordEncoder,
) {
    private val appName = "name"

    @BeforeEach
    fun setup() {
        every { passwordEncoder.encode(any()) } returns ""
    }

    @Nested
    inner class Find {
        @Test
        fun `should find app`() {
            val appId = testDataBuilder.createApp(name = appName)

            expectThat(appRepository.find(appId)).isNotNull()
                .and {
                    get { this.id }.isEqualTo(appId)
                    get { this.name }.isEqualTo(appName)
                    get { this.reporterUsername }.isNotBlank()
                }
        }

        @Test
        fun `should return null for unknown id`() {
            testDataBuilder.createApp()

            expectThat(appRepository.find(AppId(0))).isNull()
        }
    }

    @Nested
    inner class FindName {
        @Test
        fun `should find app name`() {
            val appId = testDataBuilder.createApp(name = appName)

            expectThat(appRepository.findName(appId)).isEqualTo(appName)
        }

        @Test
        fun `should return null for unknown id`() {
            testDataBuilder.createApp()

            expectThat(appRepository.findName(AppId(0))).isNull()
        }
    }

    @Nested
    inner class FindId {
        @Test
        fun `should find app id by reporter`() {
            val reporter = testDataBuilder.createUser()
            val appId = testDataBuilder.createApp(reporter = reporter)

            expectThat(appRepository.findId(reporter)).isEqualTo(appId)
        }

        @Test
        fun `should return null for unknown reporter`() {
            testDataBuilder.createApp()

            expectThat(appRepository.findId("reporter")).isNull()
        }
    }

    @Nested
    inner class Create {
        @Test
        fun `should create app and reporter user`() {
            val reporter = appRepository.create(appName)

            expectThat(appRepository.getAllNames()).hasSize(1).any { get { name }.isEqualTo(appName) }
            verify { userRepository.create(reporter.username, reporter.rawPassword, null, Role.REPORTER) }
        }
    }

    @Nested
    inner class RecreateReporter {
        @Test
        fun `should create new reporter and remove old one`() {
            val oldReporter = testDataBuilder.createUser()
            val appId = testDataBuilder.createApp(oldReporter)

            val newReporter = appRepository.recreateReporter(appId)

            expectThat(appRepository.find(appId)?.reporterUsername).isEqualTo(newReporter.username)
            verify { userRepository.create(newReporter.username, newReporter.rawPassword, null, Role.REPORTER) }
            verify { userRepository.delete(oldReporter) }
        }
    }

    @Nested
    inner class Delete {
        @Test
        fun `should delete app and reporter`() {
            val reporter = testDataBuilder.createUser()
            val appId = testDataBuilder.createApp(reporter)

            appRepository.delete(appId)

            expectThat(appRepository.find(appId)).isNull()
            expectThat(userRepository.find(reporter)).isNull()
        }
    }

    @Nested
    inner class GetVisibleIds {
        @Test
        fun `should return apps with explicit view or higher permission for user`() {
            val app1 = testDataBuilder.createApp()
            val app2 = testDataBuilder.createApp()
            val app3 = testDataBuilder.createApp()
            testDataBuilder.createApp()
            SecurityContextHolder.setContext(
                SecurityContextImpl(
                    TestingAuthenticationToken(
                        null, null, listOf(
                            Role.USER,
                            Permission(app1, Permission.Level.VIEW),
                            Permission(app2, Permission.Level.EDIT),
                            Permission(app3, Permission.Level.ADMIN)
                        )
                    )
                )
            )

            expectThat(appRepository.getVisibleIds()).containsExactlyInAnyOrder(app1, app2, app3)
        }

        @Test
        fun `should return apps with explicit view or higher permission or implicit admin permission for admin`() {
            val app1 = testDataBuilder.createApp()
            val app2 = testDataBuilder.createApp()
            val app3 = testDataBuilder.createApp()
            val app4 = testDataBuilder.createApp()
            SecurityContextHolder.setContext(
                SecurityContextImpl(
                    TestingAuthenticationToken(
                        null, null, listOf(
                            Role.ADMIN,
                            Permission(app1, Permission.Level.VIEW),
                            Permission(app2, Permission.Level.EDIT),
                            Permission(app3, Permission.Level.ADMIN)
                        )
                    )
                )
            )

            expectThat(appRepository.getVisibleIds()).containsExactlyInAnyOrder(app1, app2, app3, app4)
        }

        @Test
        fun `should not return apps with explicit none permission for admin`() {
            val app1 = testDataBuilder.createApp()
            SecurityContextHolder.setContext(
                SecurityContextImpl(
                    TestingAuthenticationToken(
                        null, null, listOf(
                            Role.ADMIN,
                            Permission(app1, Permission.Level.NONE),
                        )
                    )
                )
            )

            expectThat(appRepository.getVisibleIds()).isEmpty()
        }

    }

    @Nested
    inner class GetAllNames {
        @Test
        fun `should return app names`() {
            val app1 = testDataBuilder.createApp(name = "a1")
            val app2 = testDataBuilder.createApp(name = "a2")

            expectThat(appRepository.getAllNames()).containsExactlyInAnyOrder(AppName(app1, "a1"), AppName(app2, "a2"))
        }
    }

    @Nested
    inner class GetCustomColumns {
        @Test
        fun `should return custom columns for app`() {
            val app1 = testDataBuilder.createApp()
            val app2 = testDataBuilder.createApp()
            val c1 = testDataBuilder.createCustomColumn(app1)
            val c2 = testDataBuilder.createCustomColumn(app1)
            testDataBuilder.createCustomColumn(app2)

            expectThat(appRepository.getCustomColumns(app1)).containsExactlyInAnyOrder(c1, c2)
        }
    }

    @Nested
    inner class SetCustomColumns {
        @Test
        fun `should overwrite custom columns for app`() {
            val app1 = testDataBuilder.createApp()
            testDataBuilder.createCustomColumn(app1)

            val columns = listOf(CustomColumn("c1", "p1"), CustomColumn("c2", "p2"))

            appRepository.setCustomColumns(app1, columns)

            expectThat(appRepository.getCustomColumns(app1)).containsExactlyInAnyOrder(columns)
        }
    }

    @Nested
    inner class Provider {
        private lateinit var provider: AcrariumDataProvider<AppStats, Nothing, AppStats.Sort>

        @BeforeEach
        fun setup() {
            provider = appRepository.getProvider()
        }

        @Test
        fun `should return all visible apps`() {
            val app1 = testDataBuilder.createApp(name = "app1")
            val bug1 = testDataBuilder.createBug(app1)
            val bug2 = testDataBuilder.createBug(app1)
            testDataBuilder.createReport(app1, bug1)
            testDataBuilder.createReport(app1, bug1)
            testDataBuilder.createReport(app1, bug2)
            val app2 = testDataBuilder.createApp(name = "app2")
            val app3 = testDataBuilder.createApp()
            SecurityContextHolder.setContext(
                SecurityContextImpl(
                    TestingAuthenticationToken(
                        null,
                        null,
                        listOf(Role.ADMIN, Permission(app3, Permission.Level.NONE))
                    )
                )
            )

            expectThat(provider.size(emptySet())).isEqualTo(2)
            expectThat(provider.fetch(emptySet(), emptyList(), 0, 10).toList()).containsExactlyInAnyOrder(
                AppStats(app1, "app1", 3, 2),
                AppStats(app2, "app2", 0, 0)
            )
        }

        @Test
        fun `should sort returned apps`() {
            SecurityContextHolder.setContext(
                SecurityContextImpl(
                    TestingAuthenticationToken(
                        null,
                        null,
                        listOf(Role.ADMIN)
                    )
                )
            )
            val app1 = testDataBuilder.createApp(name = "app1")
            val app2 = testDataBuilder.createApp(name = "app2")

            expectThat(
                provider.fetch(
                    emptySet(),
                    listOf(AcrariumSort(AppStats.Sort.NAME, SortDirection.ASCENDING)),
                    0,
                    10
                ).toList().map { it.id })
                .containsExactly(app1, app2)
        }

        @Test
        fun `should offset and limit returned apps`() {
            SecurityContextHolder.setContext(
                SecurityContextImpl(
                    TestingAuthenticationToken(
                        null,
                        null,
                        listOf(Role.ADMIN)
                    )
                )
            )
            val app1 = testDataBuilder.createApp(name = "app1")
            val app2 = testDataBuilder.createApp(name = "app2")

            expectThat(
                provider.fetch(
                    emptySet(),
                    listOf(AcrariumSort(AppStats.Sort.NAME, SortDirection.ASCENDING)),
                    0,
                    1
                ).toList().map { it.id })
                .containsExactly(app1)
            expectThat(
                provider.fetch(
                    emptySet(),
                    listOf(AcrariumSort(AppStats.Sort.NAME, SortDirection.ASCENDING)),
                    1,
                    1
                ).toList().map { it.id })
                .containsExactly(app2)
        }
    }
}