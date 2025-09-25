import dev.aga.gradle.versioncatalogs.Generator.generate

pluginManagement {
    repositories {
        gradlePluginPortal()
        mavenCentral()
        maven { setUrl("https://maven.vaadin.com/vaadin-prereleases/") }
    }
}
plugins {
    id("dev.aga.gradle.version-catalog-generator") version ("3.3.1")
}
dependencyResolutionManagement {
    repositories {
        mavenCentral()
        google()
        maven { setUrl("https://maven.vaadin.com/vaadin-addons") }
        maven { setUrl("https://oss.sonatype.org/content/repositories/snapshots") }
        maven { setUrl("https://repo.spring.io/milestone") }
        maven { setUrl("https://maven.vaadin.com/vaadin-prereleases/") }
        maven { setUrl("https://repository.apache.org/content/repositories/snapshots") }
    }
    versionCatalogs {
        generate("springLibs") {
            fromToml("spring-boot-bom")
            using {
                propertyOverrides = mapOf("jooq.version" to versionRef("jooq"))
            }
        }
        generate("vaadinLibs") {
            fromToml("vaadin-base-bom")
        }
    }
}
include(":acrarium")
include(":jooq-helper")

enableFeaturePreview("TYPESAFE_PROJECT_ACCESSORS")

rootProject.name = "acrarium-root"