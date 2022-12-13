import com.faendir.gradle.createWithBomSupport

plugins {
    id("com.faendir.gradle.bom-version-catalog") version "1.3.0"
}
dependencyResolutionManagement {
    repositories {
        mavenCentral()
        maven { setUrl("https://repo.spring.io/milestone") }
        maven { setUrl("https://maven.vaadin.com/vaadin-prereleases/") }
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