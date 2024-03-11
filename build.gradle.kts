import com.diffplug.gradle.spotless.SpotlessExtension
import com.diffplug.gradle.spotless.SpotlessPlugin
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    alias(libs.plugins.dokka)
    alias(libs.plugins.jgitver)
    alias(libs.plugins.kotlin.jvm) apply false
    alias(libs.plugins.spotless) apply false
}

jgitver {
    regexVersionTag = "v([0-9]+(?:\\.[0-9]+){0,2}(?:-[a-zA-Z0-9\\-_]+)?)"
}

tasks.register("build") {
    group = "build"
}

tasks.register<Delete>("clean") {
    group = "build"
    delete = setOf(layout.buildDirectory)
}

subprojects {
    fun buildLicenseHeader(firstLine: String, linePrefix: String, lastLine: String) =
        """
            (C) Copyright ${'$'}YEAR Lukas Morawietz (https://github.com/F43nd1r)
            
            Licensed under the Apache License, Version 2.0 (the "License");
            you may not use this file except in compliance with the License.
            You may obtain a copy of the License at
            
                https://www.apache.org/licenses/LICENSE-2.0
            
            Unless required by applicable law or agreed to in writing, software
            distributed under the License is distributed on an "AS IS" BASIS,
            WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
            See the License for the specific language governing permissions and
            limitations under the License.
        """.trimIndent().split("\n").joinToString(prefix = "$firstLine\n", separator = "\n", postfix = "\n$lastLine") { "$linePrefix$it".trimEnd() }

    apply<SpotlessPlugin>()
    extensions.getByType<SpotlessExtension>().apply {
        ratchetFrom("origin/master")
        kotlin {
            target("src/**/*.kt")
            licenseHeader(buildLicenseHeader("/*", " * ", " */"))
        }
        yaml {
            target("src/**/*.yml")
            licenseHeader(buildLicenseHeader("#", "# ", "#"), "[^#].*:.*")
        }
        format("properties") {
            target("src/**/*.properties")
            licenseHeader(buildLicenseHeader("#", "# ", "#"), "[^#].*=.*")
        }
    }
    tasks.withType<KotlinCompile> {
        dependsOn("spotlessKotlinApply")
    }
    tasks.withType<ProcessResources> {
        dependsOn("spotlessYamlApply")
        dependsOn("spotlessPropertiesApply")
    }
}