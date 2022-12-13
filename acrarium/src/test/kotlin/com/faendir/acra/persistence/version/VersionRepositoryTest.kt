package com.faendir.acra.persistence.version

import com.faendir.acra.annotation.PersistenceTest
import com.faendir.acra.dataprovider.AcrariumDataProvider
import com.faendir.acra.dataprovider.AcrariumSort
import com.faendir.acra.persistence.TestDataBuilder
import com.faendir.acra.persistence.app.AppId
import com.vaadin.flow.data.provider.SortDirection
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import strikt.api.expectThat
import strikt.assertions.containsExactlyInAnyOrder
import strikt.assertions.isEqualTo
import strikt.assertions.isNotNull
import strikt.assertions.isNull
import kotlin.properties.Delegates

@PersistenceTest
class VersionRepositoryTest(
    @Autowired
    private val versionRepository: VersionRepository,
    @Autowired
    private val testDataBuilder: TestDataBuilder,
) {
    private var appId by Delegates.notNull<AppId>()

    @BeforeEach
    fun setup() {
        appId = testDataBuilder.createApp()
    }

    @Nested
    inner class EnsureExists {
        @Test
        fun `should store new Version`() {
            versionRepository.ensureExists(appId, 1, "", "TestVersion")

            expectThat(versionRepository.find(appId, 1, "")).isNotNull().and {
                get { name }.isEqualTo("TestVersion")
            }
        }

        @Test
        fun `should ignore existing version`() {
            testDataBuilder.createVersion(appId, 1, "", "OldName")

            versionRepository.ensureExists(appId, 1, "", "TestVersion")

            expectThat(versionRepository.find(appId, 1, "")).isNotNull().and {
                get { name }.isEqualTo("OldName")
            }
        }
    }

    @Nested
    inner class SetMappings {
        @Test
        fun `should store mappings on new Version`() {
            versionRepository.setMappings(appId, 1, "", "TestVersion", "Mappings")

            expectThat(versionRepository.find(appId, 1, "")).isNotNull().and {
                get { name }.isEqualTo("TestVersion")
                get { mappings }.isEqualTo("Mappings")
            }
        }

        @Test
        fun `should store mappings and name on existing Version`() {
            testDataBuilder.createVersion(appId, 1, "", "OldName")
            versionRepository.setMappings(appId, 1, "", "TestVersion", "Mappings")

            expectThat(versionRepository.find(appId, 1, "")).isNotNull().and {
                get { name }.isEqualTo("TestVersion")
                get { mappings }.isEqualTo("Mappings")
            }
        }

        @Test
        fun `should set name to code if missing on new Version`() {
            versionRepository.setMappings(appId, 1, "", null, null)

            expectThat(versionRepository.find(appId, 1, "")).isNotNull().and {
                get { name }.isEqualTo("1")
                get { mappings }.isNull()
            }
        }

        @Test
        fun `should ignore name on existing Version`() {
            testDataBuilder.createVersion(appId, 1, "", "OldName")
            versionRepository.setMappings(appId, 1, "", null, null)

            expectThat(versionRepository.find(appId, 1, "")).isNotNull().and {
                get { name }.isEqualTo("OldName")
                get { mappings }.isEqualTo(null)
            }
        }
    }

    @Nested
    inner class Delete {
        @Test
        fun `should delete version`() {
            val id = testDataBuilder.createVersion(appId)
            val version = versionRepository.find(id)

            expectThat(version).isNotNull()

            versionRepository.delete(version!!)

            expectThat(versionRepository.find(id)).isNull()
        }
    }

    @Nested
    inner class Find {

        @Test
        fun `should find version by app, code and flavor`() {
            val versionCode = 1
            val versionName = "TestVersion"
            val flavor = "Flavor"
            testDataBuilder.createVersion(code = versionCode, name = versionName, app = appId, flavor = flavor)

            expectThat(versionRepository.find(appId, versionCode, flavor)).isNotNull().and {
                get { this.code }.isEqualTo(versionCode)
                get { this.name }.isEqualTo(versionName)
                get { this.flavor }.isEqualTo(flavor)
                get { this.appId }.isEqualTo(appId)
            }
        }

        @Test
        fun `should find version by key`() {
            val versionCode = 1
            val versionName = "TestVersion"
            val flavor = "Flavor"
            testDataBuilder.createVersion(code = versionCode, name = versionName, app = appId, flavor = flavor)

            expectThat(versionRepository.find(VersionKey(appId, versionCode, flavor))).isNotNull().and {
                get { this.code }.isEqualTo(versionCode)
                get { this.name }.isEqualTo(versionName)
                get { this.flavor }.isEqualTo(flavor)
                get { this.appId }.isEqualTo(appId)
            }
        }
    }

    @Nested
    inner class Provider {
        private lateinit var provider: AcrariumDataProvider<Version, Nothing, Version.Sort>

        @BeforeEach
        fun setup() {
            provider = versionRepository.getProvider(appId)
        }

        @Test
        fun `should return all versions from app`() {
            val v1 = testDataBuilder.createVersion(appId)
            val v2 = testDataBuilder.createVersion(appId)
            val otherAppId = testDataBuilder.createApp()
            testDataBuilder.createVersion(otherAppId)

            expectThat(provider.size(emptySet())).isEqualTo(2)
            expectThat(provider.fetch(emptySet(), emptyList(), 0, 10).toList().map { it.toVersionKey() }).containsExactlyInAnyOrder(v1, v2)
        }

        @Test
        fun `should sort returned versions`() {
            val v1 = testDataBuilder.createVersion(appId, 1)
            val v2 = testDataBuilder.createVersion(appId, 2)

            expectThat(provider.fetch(emptySet(), listOf(AcrariumSort(Version.Sort.CODE, SortDirection.ASCENDING)), 0, 10).toList().map { it.toVersionKey() })
                .isEqualTo(listOf(v1, v2))
        }

        @Test
        fun `should offset and limit returned versions`() {
            val v1 = testDataBuilder.createVersion(appId, 1)
            val v2 = testDataBuilder.createVersion(appId, 2)

            expectThat(provider.fetch(emptySet(), listOf(AcrariumSort(Version.Sort.CODE, SortDirection.ASCENDING)), 0, 1).toList().map { it.toVersionKey() })
                .isEqualTo(listOf(v1))
            expectThat(provider.fetch(emptySet(), listOf(AcrariumSort(Version.Sort.CODE, SortDirection.ASCENDING)), 1, 1).toList().map { it.toVersionKey() })
                .isEqualTo(listOf(v2))
        }
    }

    @Nested
    inner class MaxVersionCode {
        @Test
        fun `should find max version code by app`() {
            testDataBuilder.createVersion(appId, 1)
            testDataBuilder.createVersion(appId, 2)
            val otherAppId = testDataBuilder.createApp()
            testDataBuilder.createVersion(otherAppId, 3)

            expectThat(versionRepository.getMaxVersionCode(appId)).isEqualTo(2)
        }
    }

    @Nested
    inner class VersionNames {
        @Test
        fun `should get all version names for app`() {
            val v1 = testDataBuilder.createVersion(appId, name = "v1")
            val v2 = testDataBuilder.createVersion(appId, name = "v2")
            val otherAppId = testDataBuilder.createApp()
            testDataBuilder.createVersion(otherAppId, name = "v3")

            expectThat(versionRepository.getVersionNames(appId)).containsExactlyInAnyOrder(VersionName(v1.code, v1.flavor, "v1"), VersionName(v2.code, v2.flavor, "v2"))
        }
    }
}