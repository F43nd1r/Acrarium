import com.faendir.gradle.createWithBomSupport

pluginManagement {
    repositories {
        gradlePluginPortal()
        mavenCentral()
        maven { setUrl("https://maven.vaadin.com/vaadin-prereleases/") }
    }
}
plugins {
    id("com.faendir.gradle.bom-version-catalog") version "1.4.6"
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
        createWithBomSupport("libs") {
            fromBomAlias("spring-boot-bom")
            fromBomAlias("vaadin-base-bom")
            fromBomAlias("vaadin-flow-bom")
        }
    }
}
include(":acrarium")
include(":jooq-helper")

enableFeaturePreview("TYPESAFE_PROJECT_ACCESSORS")

rootProject.name = "acrarium-root"