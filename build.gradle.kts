plugins {
    alias(libs.plugins.dokka)
    alias(libs.plugins.jgitver)
}

jgitver {
    regexVersionTag = "v([0-9]+(?:\\.[0-9]+){0,2}(?:-[a-zA-Z0-9\\-_]+)?)"
}

tasks.register("build") {
    group = "build"
}

tasks.register<Delete>("clean") {
    group = "build"
    delete = setOf(buildDir)
}