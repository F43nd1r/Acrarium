plugins {
    `kotlin-dsl`
}

repositories {
    mavenCentral()
}

dependencies {
    implementation(libs.kotlinPoet)
    implementation(libs.guava)
}

kotlin {
    jvmToolchain { languageVersion.set(JavaLanguageVersion.of(17)) }
}
