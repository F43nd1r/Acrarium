plugins {
    kotlin
    org.jetbrains.kotlin.kapt
    org.jetbrains.kotlin.plugin.allopen
    org.jetbrains.kotlin.plugin.spring
    org.jetbrains.kotlin.plugin.noarg
    org.jetbrains.kotlin.plugin.jpa
    org.springframework.boot
    com.vaadin
    war
    com.palantir.docker
}

val base: Configuration by configurations.creating
configurations {
    implementation { extendsFrom(base) }
    getByName("kapt") { extendsFrom(base) }
    developmentOnly { extendsFrom(base) }
}

fun boot(module: String) = "org.springframework.boot:spring-boot-$module"

dependencies {
    base(platform(libs.spring.boot.bom))
    base(platform(libs.vaadin.flow.bom))
    implementation(boot("starter-security"))
    implementation(boot("starter-mail"))
    implementation(boot("starter-actuator"))
    implementation(boot("starter-data-jpa"))
    kapt(boot("configuration-processor"))
    implementation("com.vaadin:vaadin")
    implementation("com.vaadin:vaadin-spring-boot-starter")
    implementation("mysql:mysql-connector-java")
    implementation("org.liquibase:liquibase-core")
    implementation("com.faendir.liquibase:liquibase-kotlin-dsl:3.1.0")
    implementation("com.querydsl:querydsl-jpa:4.4.0")
    implementation("com.querydsl:querydsl-sql:4.4.0")
    kapt("com.querydsl:querydsl-apt:5.0.0-SNAPSHOT:jpa")
    kapt("com.querydsl:querydsl-kotlin-codegen:5.0.0-SNAPSHOT")
    implementation("org.ektorp:org.ektorp.spring:1.5.0")
    implementation("ch.acra:acra-javacore:5.8.3")
    implementation("org.codeartisans:org.json:20161124")
    implementation("org.apache.commons:commons-text:1.9")
    implementation("org.xbib:time:2.1.0")
    implementation("com.faendir.proguard:retrace:1.3")
    implementation("me.xdrop:fuzzywuzzy:1.3.1")
    implementation("javax.servlet:javax.servlet-api:4.0.1")
    implementation("org.hibernate.validator:hibernate-validator")
    implementation("com.faendir.ziplet:ziplet:2.4.3")
    implementation("com.univocity:univocity-parsers:2.9.1")
    implementation("com.talanlabs:avatar-generator:1.1.0")
    implementation("com.github.appreciated:apexcharts:2.0.0.beta12")
    implementation("com.github.appreciated:vaadin-css-grid:2.0.0")
    implementation("com.github.appreciated:vaadin-paper-menu-button:2.0.2")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.5.0")
    implementation("com.fasterxml.jackson.module:jackson-module-kotlin:2.12.3")
    implementation("io.github.microutils:kotlin-logging-jvm:2.0.8")
    implementation(libs.autoService.annotations)
    kapt(libs.autoService.processor)
    developmentOnly(boot("devtools"))
    testImplementation(boot("starter-test"))
    testImplementation("org.springframework.security:spring-security-test:5.5.0")
    testImplementation("com.vaadin:vaadin-testbench")
    testImplementation("com.ninja-squad:springmockk:3.0.1")
    testImplementation("io.strikt:strikt-core:0.31.0")
    testImplementation("org.testcontainers:junit-jupiter:1.15.3")
    testImplementation("org.testcontainers:selenium:1.15.3")
    testImplementation("org.seleniumhq.selenium:selenium-remote-driver:3.141.59")
    testImplementation("org.seleniumhq.selenium:selenium-chrome-driver:3.141.59")
    testImplementation("org.seleniumhq.selenium:selenium-firefox-driver:3.141.59")
    testImplementation("org.junit.jupiter:junit-jupiter-params:5.7.2")
    testImplementation("ch.vorburger.mariaDB4j:mariaDB4j-springboot:2.4.0")
    testImplementation("org.mariadb.jdbc:mariadb-java-client:2.7.3")
}

val messagesOutput = file("$buildDir/generated/source/gradle/main")

sourceSets {
    main {
        java {
            srcDir(messagesOutput)
        }
    }
}

val generateMessageClasses by tasks.creating(com.faendir.acra.gradle.I18nClassGenerator::class) {
    inputDirectory = file("src/main/resources/i18n/com/faendir/acra")
    outputDirectory = messagesOutput
    packageName = "com.faendir.acra.i18n"
    className = "Messages"
}

tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile> {
    dependsOn(generateMessageClasses)
}

springBoot {
    mainClass.set("com.faendir.acra.BackendApplicationKt")
}

allOpen {
    annotation("com.vaadin.testbench.elementsbase.Element")
}

noArg {
    annotation("com.faendir.acra.util.NoArgConstructor")
}

docker {
    name = "f43nd1r/acrarium"
    tag("hubLatest", "hub.docker.com/$name:latest")
    tag("hubVersion", "hub.docker.com/$name:$version")
    tag("ghLatest", "ghcr.io/$name:latest")
    tag("ghVersion", "ghcr.io/$name:$version")
    files(tasks.getByName<org.springframework.boot.gradle.tasks.bundling.BootJar>("bootJar").outputs)
    copySpec.into("build/libs")
}

gradle.taskGraph.addTaskExecutionGraphListener { graph ->
    if (graph.allTasks.any { it.name == "docker" }) {
        vaadin.productionMode = true
    }
}

tasks.withType<Test> {
    project.properties["vaadinProKey"]?.let { systemProperty("vaadin.proKey", it) }
    useJUnitPlatform()
}
