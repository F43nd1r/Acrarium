import com.faendir.gradle.createFromBom
plugins {
    id("com.faendir.gradle.bom-version-catalog") version "1.0.1"
}
dependencyResolutionManagement {
    repositories {
        mavenCentral()
    }
    versionCatalogs {
        createFromBom("libs", "org.springframework.boot:spring-boot-dependencies:2.5.1", "com.vaadin:vaadin-bom:20.0.1") {
            version("querydsl", "5.0.0-SNAPSHOT")
            alias("comQuerydsl-querydslKotlinCodegen").to("com.querydsl", "querydsl-kotlin-codegen").versionRef("querydsl")
        }
    }
}
include(":acrarium")
