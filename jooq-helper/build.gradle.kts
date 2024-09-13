plugins {
    alias(libs.plugins.kotlin.jvm)
}

dependencies {
    listOf(libs.spring.boot.bom).forEach {
        implementation(it)
        testImplementation(it)
    }
    implementation(springLibs.liquibase.liquibaseCore)
    implementation(springLibs.jooq.jooqMeta)
    implementation(springLibs.slf4j.slf4jApi)
    implementation(springLibs.slf4j.slf4jSimple)
    implementation(springLibs.testcontainers.mysql)
}

kotlin {
    jvmToolchain(17)
}