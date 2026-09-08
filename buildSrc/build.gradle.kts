plugins {
    `kotlin-dsl`
}

repositories {
    mavenCentral()
}

dependencies {
    implementation(libs.kotlinPoet)
}

kotlin {
    jvmToolchain { languageVersion.set(JavaLanguageVersion.of(21)) }
}
