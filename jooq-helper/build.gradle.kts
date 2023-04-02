plugins {
    alias(libs.plugins.kotlin.jvm)
}

dependencies {
    libs.bundles.bom.get().map { implementation(platform(it)) }
    implementation(libs.orgLiquibase.liquibaseCore)
    implementation(libs.orgJooq.jooqMeta)
    implementation(libs.orgSlf4j.slf4jApi)
    implementation(libs.orgSlf4j.slf4jSimple)
    implementation(libs.testContainers.mysql)
}

kotlin {
    jvmToolchain(17)
}