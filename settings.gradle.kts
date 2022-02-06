import com.faendir.gradle.createWithBomSupport
plugins {
    id("com.faendir.gradle.bom-version-catalog") version "1.2.0"
}
dependencyResolutionManagement {
    repositories {
        mavenCentral()
    }
    versionCatalogs {
        createWithBomSupport("libs") {
            fromBomAlias("spring-boot-bom")
            fromBomAlias("vaadin-flow-bom")
        }
    }
}
include(":acrarium")
